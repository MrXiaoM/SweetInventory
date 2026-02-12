package top.mrxiaom.sweet.inventory.func;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.func.AutoRegister;
import top.mrxiaom.pluginbase.utils.Pair;
import top.mrxiaom.sweet.inventory.Messages;
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
    private final Map<String, MenuConfig> menusByCommand = new HashMap<>();
    public MenuCommandManager(SweetInventory plugin) {
        super(plugin);
        this.initReflection();
    }

    /**
     * 通过根命令获取菜单配置
     * @param label 根命令
     */
    @Nullable
    public MenuConfig getByCommand(@NotNull String label) {
        return menusByCommand.get(label);
    }

    public void onCommand(
            @NotNull CommandSender sender,
            @NotNull String label,
            @NotNull String[] args
    ) {
        if (!(sender instanceof Player)) {
            Messages.player__only.tm(sender);
            return;
        }
        Player player = (Player) sender;
        MenuConfig menu = menusByCommand.get(label);
        if (menu != null) {
            menu.open(player, args);
        }
    }

    @Nullable
    public List<String> onTabComplete(
            @NotNull CommandSender sender,
            @NotNull String label,
            @NotNull String[] args
    ) {
        return Collections.emptyList();
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
            MenuConfig existsMenu = menusByCommand.get(label);
            if (existsMenu != null) {
                // 冲突处理: 已有的菜单命令
                warn("菜单 " + menu.id() + " 绑定的命令与 " + existsMenu.id() + " 绑定的命令冲突，保留后者");
                continue;
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
            MenuCommand command = new MenuCommand(this, label);
            knownCommands.put(label, command);
            command.register(commandMap);
            menusByCommand.put(label, menu);
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
