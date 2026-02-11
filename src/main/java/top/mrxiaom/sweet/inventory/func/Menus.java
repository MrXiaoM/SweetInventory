package top.mrxiaom.sweet.inventory.func;

import org.bukkit.Bukkit;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.InventoryView;
import org.bukkit.permissions.Permissible;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.actions.*;
import top.mrxiaom.pluginbase.func.AutoRegister;
import top.mrxiaom.pluginbase.func.GuiManager;
import top.mrxiaom.pluginbase.gui.IGuiHolder;
import top.mrxiaom.pluginbase.utils.ConfigUtils;
import top.mrxiaom.pluginbase.utils.Util;
import top.mrxiaom.sweet.inventory.SweetInventory;
import top.mrxiaom.sweet.inventory.func.actions.ActionConnectServer;
import top.mrxiaom.sweet.inventory.func.actions.ActionOpenMenu;
import top.mrxiaom.sweet.inventory.func.actions.ActionRefresh;
import top.mrxiaom.sweet.inventory.func.menus.MenuConfig;
import top.mrxiaom.sweet.inventory.func.menus.MenuInstance;

import java.io.File;
import java.util.*;
import java.util.function.BiConsumer;

import static top.mrxiaom.sweet.inventory.func.actions.ActionTurnPage.NEXT;
import static top.mrxiaom.sweet.inventory.func.actions.ActionTurnPage.PREV;
import static top.mrxiaom.sweet.inventory.func.menus.MenuConfig.getBoolean;

/**
 * 菜单配置管理器
 */
@AutoRegister
public class Menus extends AbstractModule {
    private final MenuCommandManager commandManager;
    private final Map<String, MenuConfig> menus = new HashMap<>();
    private final Map<String, MenuConfig> menusById = new HashMap<>();
    private final File menusFolder;
    private final List<File> menuFolders = new ArrayList<>();
    public Menus(SweetInventory plugin) {
        super(plugin);
        this.registerAlternativeProvider();
        this.commandManager = instanceOf(MenuCommandManager.class);
        this.menusFolder = new File(plugin.getDataFolder(), "menus");
        Bukkit.getScheduler().runTaskTimer(plugin, this::onTick, 1L, 1L);
    }

    private void registerAlternativeProvider() {
        ActionProviders.registerActionProvider(s -> {
            if (s.startsWith("[控制台执行]")) return new ActionConsole(s.substring(7));
            if (s.startsWith("控制台执行:")) return new ActionConsole(s.substring(6));
            if (s.startsWith("[玩家执行]")) return new ActionPlayer(s.substring(6));
            if (s.startsWith("玩家执行:")) return new ActionPlayer(s.substring(5));
            if (s.startsWith("[聊天消息]")) return new ActionMessageAdventure(s.substring(6));
            if (s.startsWith("聊天消息:")) return new ActionMessageAdventure(s.substring(5));
            if (s.startsWith("[动作消息]")) return new ActionActionBar(s.substring(6));
            if (s.startsWith("动作消息:")) return new ActionActionBar(s.substring(5));
            if (s.startsWith("[打开菜单]")) return new ActionOpenMenu(s.substring(6));
            if (s.startsWith("打开菜单:")) return new ActionOpenMenu(s.substring(5));
            if (s.startsWith("[open]")) return new ActionOpenMenu(s.substring(6));
            if (s.startsWith("open:")) return new ActionOpenMenu(s.substring(5));
            if (s.startsWith("[连接子服]")) return new ActionConnectServer(s.substring(6));
            if (s.startsWith("连接子服:")) return new ActionConnectServer(s.substring(5));
            if (s.startsWith("[connect]")) return new ActionConnectServer(s.substring(9));
            if (s.startsWith("connect:")) return new ActionConnectServer(s.substring(8));
            //noinspection IfCanBeSwitch
            if (s.equals("[上一页]") || s.equals("上一页") || s.equals("[prev]") || s.equals("prev")) return PREV;
            if (s.equals("[下一页]") || s.equals("下一页") || s.equals("[next]") || s.equals("next")) return NEXT;
            if (s.equals("[刷新]") || s.equals("刷新") || s.equals("[refresh]") || s.equals("refresh")) return ActionRefresh.INSTANCE;
            return null;
        });
    }

    private void onTick() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            InventoryView view = player.getOpenInventory();
            InventoryHolder holder = view.getTopInventory().getHolder();
            if (holder instanceof MenuInstance) {
                ((MenuInstance) holder).onTick();
            }
        }
    }

    @Override
    public void reloadConfig(MemoryConfiguration cfg) {
        if (!menusFolder.exists()) {
            Util.mkdirs(menusFolder);
            plugin.saveResource("menus/example.yml", new File(menusFolder, "example.yml"));
        }
        menus.clear();
        menusById.clear();
        menuFolders.clear();
        List<String> pathList = cfg.getStringList("extra-menus-folders");
        for (String path : pathList) {
            File folder = new File(path);
            if (!folder.exists()) {
                warn("目录 " + path + " 不存在");
                continue;
            }
            if (!folder.isDirectory()) {
                warn("路径 " + path + " 指向的不是一个目录");
                continue;
            }
            menuFolders.add(folder);
            reloadFolder(folder, this::loadConfig);
        }
        menuFolders.add(menusFolder);
        reloadFolder(menusFolder, this::loadConfig);
        menus.putAll(menusById);
        for (MenuConfig menu : menusById.values()) {
            updateAlias(menu);
        }
        refreshCommands();
        info("加载了 " + menusById.size() + " 个菜单");
    }

    public void refreshCommands() {
        commandManager.refreshCommands(menusById.values());
    }

    private void updateAlias(MenuConfig menu) {
        for (String aliasId : menu.aliasIds()) {
            MenuConfig exists = menus.get(aliasId);
            if (exists != null) {
                warn("菜单 " + menu.id() + " 的别名 " + aliasId + " 已被其它菜单占用 (" + exists.id() + ")");
            } else {
                menus.put(aliasId, menu);
            }
        }
    }

    private MenuConfig loadConfig(String id, File file) {
        FileConfiguration config = plugin.resolveGotoFlag(ConfigUtils.load(file));
        boolean alt = getBoolean(true, config, "中文配置", false);
        MenuConfig loaded = MenuConfig.load(alt, id.replace("\\", "/"), config);
        menusById.put(loaded.id(), loaded);
        return loaded;
    }

    protected void updateConfig(String id, File file) {
        removeConfig(id, false);
        MenuConfig menu = loadConfig(id, file);
        menus.put(menu.id(), menu);
        updateAlias(menu);
        refreshCommands();
    }

    protected void removeConfig(String id, boolean refreshCommands) {
        MenuConfig exists = menusById.get(id);
        if (exists != null) {
            for (String aliasId : exists.aliasIds()) {
                if (menus.containsKey(aliasId)) continue;
                menus.remove(aliasId);
            }
            menus.remove(id);
            menusById.remove(id);
            if (refreshCommands) {
                refreshCommands();
            }
            plugin.getScheduler().runTask(() -> {
                GuiManager manager = GuiManager.inst();
                for (Player p : Bukkit.getOnlinePlayers()) {
                    IGuiHolder gui = manager.getOpeningGui(p);
                    if (gui instanceof MenuInstance) {
                        if (((MenuInstance) gui).config().id().equals(id)) {
                            p.closeInventory();
                        }
                    }
                }
            });
        }
    }

    public Set<String> getMenuIds() {
        return menusById.keySet();
    }

    public Set<String> getMenuKeys() {
        return menus.keySet();
    }

    public Set<String> getMenuKeys(Permissible p) {
        Set<String> sets = new HashSet<>();
        for (MenuConfig config : menusById.values()) {
            if (config.hasPermission(p)) {
                sets.add(config.id());
                sets.addAll(config.aliasIds());
            }
        }
        return sets;
    }

    @Nullable
    public MenuConfig getMenuById(String id) {
        return menusById.get(id);
    }

    @Nullable
    public MenuConfig getMenu(String key) {
        return menus.get(key);
    }

    @NotNull
    public List<File> getMenuFolders() {
        return Collections.unmodifiableList(menuFolders);
    }

    protected static void reloadFolder(File folder, BiConsumer<String, File> reloadConfig) {
        reloadFolder(folder, null, reloadConfig);
    }

    private static void reloadFolder(File root, File folder, BiConsumer<String, File> reloadConfig) {
        File[] files = (folder == null ? root : folder).listFiles();
        if (files != null) for (File file : files) {
            if (file.isDirectory()) {
                reloadFolder(root, file, reloadConfig);
                continue;
            }
            String id = getRelationPath(root, file);
            if (id != null) {
                reloadConfig.accept(id, file);
            }
        }
    }

    @Nullable
    protected static String getRelationPath(File folder, File file) {
        String parentPath = folder.getAbsolutePath();
        String path = file.getAbsolutePath();
        if (path.startsWith(parentPath)) {
            if (path.endsWith(".yml") || path.endsWith(".yaml")) {
                String s = path.substring(parentPath.length()).replace("\\", "/");
                String relation = s.startsWith("/") ? s.substring(1) : s;
                return Util.nameWithoutSuffix(relation);
            }
        }
        return null;
    }

    @Override
    public void onDisable() {
        menus.clear();
        menusById.clear();
    }

    public static Menus inst() {
        return instanceOf(Menus.class);
    }
}
