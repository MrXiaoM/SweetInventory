package top.mrxiaom.sweet.inventory.func;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.func.AutoRegister;
import top.mrxiaom.pluginbase.utils.Pair;
import top.mrxiaom.sweet.inventory.SweetInventory;
import top.mrxiaom.sweet.inventory.func.menus.MenuCommand;
import top.mrxiaom.sweet.inventory.func.menus.MenuConfig;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

/**
 * 菜单命令动态注册管理器
 */
@AutoRegister(priority = 990)
public class MenuCommandManager extends AbstractModule {
    private final Map<String, MenuCommand> menusByCommand = new HashMap<>();
    public MenuCommandManager(SweetInventory plugin) {
        super(plugin);
        this.initReflection();
    }

    /**
     * 通过根命令获取菜单命令配置
     * @param label 根命令
     */
    @Nullable
    public MenuCommand getCommand(@NotNull String label) {
        return menusByCommand.get(label);
    }

    private boolean addChild(MenuCommand command, MenuConfig menu, String label, List<String> childArguments) {
        // 注意: 注册成功返回 false，注册失败返回 true
        MenuCommand.ChildCommand existsChild = command.getChild(childArguments);
        if (existsChild != null) {
            warn("菜单 " + menu.id() + " 在根命令 /" + label + " 绑定的子命令与 " + existsChild.menu().id() + " 绑定的子命令冲突，保留后者");
            return true;
        }
        return !command.addChild(childArguments, menu);
    }

    protected void refreshCommands(Collection<MenuConfig> menus) {
        Pair<SimpleCommandMap, Map<String, Command>> pair = getCommandMap();
        cleanupMenuCommands(pair);
        if (pair == null) return;

        SimpleCommandMap commandMap = pair.key();
        Map<String, Command> knownCommands = pair.value();
        for (MenuConfig menu : menus) {
            String label = menu.bindCommand();
            if (label == null) continue;
            List<String> childArguments;
            if (label.contains(" ")) {
                String[] split = label.trim().split(" ");
                label = split[0];
                childArguments = Arrays.asList(split).subList(1, split.length);
            } else {
                childArguments = Collections.emptyList();
            }
            MenuCommand existsMenuCommand = menusByCommand.get(label);
            if (existsMenuCommand != null) {
                // 冲突处理: 已有的其它菜单命令
                if (!childArguments.isEmpty()) {
                    // 子命令不为空时，注册子命令
                    if (addChild(existsMenuCommand, menu, label, childArguments)) {
                        continue;
                    }
                } else {
                    // 子命令为空时
                    MenuConfig existsMenu = existsMenuCommand.menu();
                    if (existsMenu != null) {
                        // 冲突处理: 已有的菜单命令
                        warn("菜单 " + menu.id() + " 绑定的命令与 " + existsMenu.id() + " 绑定的命令冲突，保留后者");
                        continue;
                    } else {
                        // 如果这个命令还没有根菜单，则设为当前菜单
                        existsMenuCommand.menu(menu);
                    }
                }
            }
            Command existsCommand = knownCommands.get(label);
            if (existsCommand != null) {
                // 冲突处理: 已有的其他插件命令
                if (existsCommand instanceof PluginCommand) {
                    String pluginName = ((PluginCommand) existsCommand).getPlugin().getDescription().getName();
                    warn("菜单 " + menu.id() + " 要绑定的命令 /" + label + " 已经与现有插件 " + pluginName + " 的命令冲突");
                } else {
                    warn("菜单 " + menu.id() + " 要绑定的命令 /" + label + " 已经与现有命令冲突");
                }
                continue;
            }
            // 均没有冲突时，才注册命令
            MenuCommand command;
            if (childArguments.isEmpty()) {
                // 子命令为空，注册根命令
                command = new MenuCommand(label, menu);
            } else {
                // 子命令不为空，注册子命令
                command = new MenuCommand(label, null);
                if (addChild(command, menu, label, childArguments)) {
                    continue;
                }
            }
            knownCommands.put(label, command);
            command.register(commandMap);
            menusByCommand.put(label, command);
        }
        // 如果服务端支持，提交刷新命令操作
        if (syncCommands()) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.updateCommands();
            }
        }
    }

    private Method methodCommandMap, methodSyncCommand;
    private void initReflection() {
        Method method1, method2;
        try {
            method1 = Bukkit.getServer().getClass().getDeclaredMethod("getCommandMap");
            method1.setAccessible(true);
            method1.invoke(Bukkit.getServer());
        } catch (ReflectiveOperationException e) {
            method1 = null;
        }
        try {
            method2 = Bukkit.getServer().getClass().getDeclaredMethod("syncCommands");
            method2.setAccessible(true);
        } catch (ReflectiveOperationException e) {
            method2 = null;
        }
        methodCommandMap = method1;
        methodSyncCommand = method2;
    }

    private Pair<SimpleCommandMap, Map<String, Command>> getCommandMap() {
        if (methodCommandMap == null) {
            return null;
        }
        try {
            SimpleCommandMap commandMap = (SimpleCommandMap) methodCommandMap.invoke(Bukkit.getServer());
            Field field = SimpleCommandMap.class.getDeclaredField("knownCommands");
            field.setAccessible(true);
            // noinspection unchecked
            Map<String, Command> knownCommands = (Map<String, Command>) field.get(commandMap);
            return Pair.of(commandMap, knownCommands);
        } catch (Throwable ignored) {
            return null;
        }
    }

    private boolean syncCommands() {
        if (methodSyncCommand == null) return false;
        try {
            methodSyncCommand.invoke(Bukkit.getServer());
            return true;
        } catch (ReflectiveOperationException ignored) {
        }
        return false;
    }

    private void cleanupMenuCommands(Pair<SimpleCommandMap, Map<String, Command>> pair) {
        menusByCommand.clear();
        if (pair == null) return;
        SimpleCommandMap commandMap = pair.key();
        Map<String, Command> knownCommands = pair.value();
        for (Object obj : commandMap.getCommands().toArray()) {
            // 注销所有 MenuCommand
            if (obj instanceof MenuCommand) {
                MenuCommand command = (MenuCommand) obj;
                command.unregister(commandMap);
                String[] keys = knownCommands.entrySet().stream()
                        .filter(it -> it.getValue().equals(command))
                        .map(Map.Entry::getKey)
                        .toArray(String[]::new);
                for (String key : keys) {
                    knownCommands.remove(key);
                }
            }
        }
    }

    @Override
    public void onDisable() {
        cleanupMenuCommands(getCommandMap());
    }
}
