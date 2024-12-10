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
import top.mrxiaom.pluginbase.gui.IGui;
import top.mrxiaom.sweet.inventory.SweetInventory;
import top.mrxiaom.sweet.inventory.func.menus.MenuConfig;
import top.mrxiaom.sweet.inventory.func.menus.MenuHolder;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;

@AutoRegister
public class Menus extends AbstractModule {
    Map<String, MenuConfig> menus = new HashMap<>();
    File menusFolder;
    public Menus(SweetInventory plugin) {
        super(plugin);
        this.menusFolder = new File(plugin.getDataFolder(), "menus");
        Bukkit.getScheduler().runTaskTimer(plugin, this::onTick, 1L, 1L);
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
        reloadConfig(menusFolder);
    }

    public void reloadConfig(File folder) {
        File[] files = folder.listFiles();
        if (files == null) return;
        for (File file : files) {
            if (file.isDirectory()) {
                reloadConfig(folder);
                continue;
            }
            if (file.getName().endsWith(".yml")) {
                loadConfig(file);
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

    private String removePrefix(File file) {
        String filePath = file.getAbsolutePath();
        String folder = menusFolder.getAbsolutePath();
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

    private void loadConfig(File file) {
        String path = removePrefix(file);
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
