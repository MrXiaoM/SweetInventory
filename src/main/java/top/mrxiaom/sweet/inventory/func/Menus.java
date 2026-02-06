package top.mrxiaom.sweet.inventory.func;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import org.bukkit.Bukkit;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.InventoryView;
import org.bukkit.permissions.Permissible;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.actions.*;
import top.mrxiaom.pluginbase.func.AutoRegister;
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

import static top.mrxiaom.sweet.inventory.func.actions.ActionTurnPage.NEXT;
import static top.mrxiaom.sweet.inventory.func.actions.ActionTurnPage.PREV;
import static top.mrxiaom.sweet.inventory.func.menus.MenuConfig.getBoolean;

@AutoRegister
public class Menus extends AbstractModule {
    private final Map<String, MenuConfig> menus = new HashMap<>();
    private final Map<String, MenuConfig> menusById = new HashMap<>();
    private final File menusFolder;
    public Menus(SweetInventory plugin) {
        super(plugin);
        this.registerAlternativeProvider();
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

    @CanIgnoreReturnValue
    private static boolean mkdirs(File folder) {
        return folder.mkdirs();
    }

    @Override
    public void reloadConfig(MemoryConfiguration cfg) {
        if (!menusFolder.exists()) {
            mkdirs(menusFolder);
            plugin.saveResource("menus/example.yml", new File(menusFolder, "example.yml"));
        }
        menus.clear();
        menusById.clear();
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
            Util.reloadFolder(folder, false, (id, file) -> {
                loadConfig(id.replace("\\", "/"), file);
            });
        }
        Util.reloadFolder(menusFolder, false, (id, file) -> {
            loadConfig(id.replace("\\", "/"), file);
        });
        menus.putAll(menusById);
        for (MenuConfig config : menusById.values()) {
            for (String aliasId : config.aliasIds()) {
                MenuConfig exists = menus.get(aliasId);
                if (exists != null) {
                    warn("菜单 " + config.id() + " 的别名 " + aliasId + " 已被其它菜单占用 (" + exists.id() + ")");
                } else {
                    menus.put(aliasId, config);
                }
            }
        }
    }

    private void loadConfig(String id, File file) {
        FileConfiguration config = plugin.resolveGotoFlag(ConfigUtils.load(file));
        boolean alt = getBoolean(true, config, "中文配置", false);
        MenuConfig loaded = MenuConfig.load(alt, id, config);
        menusById.put(id, loaded);
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

    public static Menus inst() {
        return instanceOf(Menus.class);
    }
}
