package top.mrxiaom.sweet.inventory.func;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.BukkitPlugin;
import top.mrxiaom.pluginbase.func.AbstractGuiModule;
import top.mrxiaom.pluginbase.func.AutoRegister;
import top.mrxiaom.pluginbase.func.gui.LoadedIcon;
import top.mrxiaom.pluginbase.func.gui.actions.ActionActionBar;
import top.mrxiaom.pluginbase.func.gui.actions.ActionConsole;
import top.mrxiaom.pluginbase.func.gui.actions.ActionMessageAdventure;
import top.mrxiaom.pluginbase.func.gui.actions.ActionPlayer;
import top.mrxiaom.pluginbase.gui.IGui;
import top.mrxiaom.sweet.inventory.SweetInventory;
import top.mrxiaom.sweet.inventory.func.actions.ActionConnectServer;
import top.mrxiaom.sweet.inventory.func.actions.ActionOpenMenu;
import top.mrxiaom.sweet.inventory.func.menus.MenuConfig;
import top.mrxiaom.sweet.inventory.func.menus.MenuHolder;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;

@AutoRegister
public class Menus extends AbstractModule {
    Map<String, MenuConfig> menus = new HashMap<>();
    File menusFolder;
    public Menus(SweetInventory plugin) {
        super(plugin);
        this.registerAlternativeProvider();
        this.menusFolder = new File(plugin.getDataFolder(), "menus");
        Bukkit.getScheduler().runTaskTimer(plugin, this::onTick, 1L, 1L);
    }

    private void registerAlternativeProvider() {
        AbstractGuiModule.registerActionProvider(s -> {
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
            return null;
        });
    }

    private void onTick() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            InventoryView view = player.getOpenInventory();
            InventoryHolder holder = view.getTopInventory().getHolder();
            if (holder instanceof MenuHolder) {
                ((MenuHolder) holder).getInstance().onTick();
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
            reloadConfig(folder, folder);
        }
        reloadConfig(menusFolder, menusFolder);
    }

    public void reloadConfig(File root, File folder) {
        File[] files = folder.listFiles();
        if (files == null) return;
        for (File file : files) {
            if (file.isDirectory()) {
                reloadConfig(root, folder);
                continue;
            }
            if (file.getName().endsWith(".yml")) {
                loadConfig(root, file);
            }
        }
    }

    public Set<String> getMenusId() {
        return menus.keySet();
    }

    @Nullable
    public MenuConfig getMenu(String id) {
        return menus.get(id);
    }

    private String removePrefix(File root, File file) {
        String filePath = file.getAbsolutePath();
        String folder = root.getAbsolutePath();
        if (filePath.startsWith(folder)) {
            return filePath.substring(folder.length());
        } else {
            return filePath;
        }
    }
    private String removeSuffix(String fileName) {
        int index = fileName.lastIndexOf(".");
        return fileName.substring(0, index);
    }

    private void loadConfig(File root, File file) {
        String path = removePrefix(root, file);
        String id = removeSuffix(path).replace("\\", "/");
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        boolean alt = config.getBoolean("中文配置", false);
        MenuConfig loaded = MenuConfig.load(alt, id, config);
        if (loaded == null) {
            warn("[" + path + "] 菜单加载失败");
        } else {
            menus.put(id, loaded);
        }
    }

    public static Menus inst() {
        return instanceOf(Menus.class);
    }
}
