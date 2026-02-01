package top.mrxiaom.sweet.inventory.func.menus;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.actions.ActionProviders;
import top.mrxiaom.pluginbase.api.IAction;
import top.mrxiaom.sweet.inventory.SweetInventory;

import java.util.*;

public class MenuConfig {
    final String id;
    final String title;
    final char[] inventory;
    final Map<Character, List<MenuIcon>> iconsByChar;
    final Map<String, MenuIcon> iconsByName;
    final @Nullable String bindCommand;
    final List<IAction> openCommands;
    final int updateInterval;

    MenuConfig(boolean alt, String id, YamlConfiguration config) {
        SweetInventory plugin = SweetInventory.getInstance();
        this.id = id;
        this.title = config.getString(alt ? "标题" : "title", "");
        this.inventory = getInventory(config,  alt ? "布局" : "inventory");
        this.bindCommand = config.getString(alt ? "绑定界面命令" : "bind-command", null);
        this.openCommands = ActionProviders.loadActions(config, alt ? "打开界面执行命令" : "open-commands");
        this.updateInterval = Math.max(0, config.getInt(alt ? "更新周期" : "update-interval", 0));

        this.iconsByChar = new HashMap<>();
        this.iconsByName = new HashMap<>();
        ConfigurationSection section = config.getConfigurationSection(alt ? "图标列表" : "items");
        if (section != null) for (String key : section.getKeys(false)) {
            ConfigurationSection s = section.getConfigurationSection(key);
            MenuIcon icon;
            try {
                if (s == null) throw new IllegalArgumentException("找不到配置");
                icon = MenuIcon.load(alt, s, key);
            } catch (Throwable t) {
                plugin.error("[" + id + ".yml] 加载图标 " + key + " 失败" + t.getMessage());
                continue;
            }
            iconsByName.put(key, icon);
            for (Character c : icon.slots()) {
                List<MenuIcon> list = getIconsList(iconsByChar, c);
                list.add(icon);
            }
        }
        for (List<MenuIcon> list : iconsByChar.values()) {
            list.sort(Comparator.comparingInt(MenuIcon::priority));
        }
    }

    public String id() {
        return id;
    }

    public String title() {
        return title;
    }

    public char[] inventory() {
        return inventory;
    }

    public Map<Character, List<MenuIcon>> iconsByChar() {
        return iconsByChar;
    }

    public Map<String, MenuIcon> iconsByName() {
        return iconsByName;
    }

    @Nullable
    public String bindCommand() {
        return bindCommand;
    }

    public List<IAction> openCommands() {
        return openCommands;
    }

    public int updateInterval() {
        return updateInterval;
    }

    public MenuInstance create(Player player) {
        return MenuInstance.create(this, player);
    }

    private static List<MenuIcon> getIconsList(Map<Character, List<MenuIcon>> map, Character c) {
        List<MenuIcon> list = map.get(c);
        if (list == null) {
            list = new ArrayList<>();
            map.put(c, list);
        }
        return list;
    }

    public static MenuConfig load(boolean alt, String id, YamlConfiguration config) {
        return new MenuConfig(alt, id, config);
    }

    public static boolean getBoolean(boolean alt, ConfigurationSection section, String key) {
        return getBoolean(alt, section, key, false);
    }

    public static boolean getBoolean(boolean alt, ConfigurationSection section, String key, boolean def) {
        if (!alt) return section.getBoolean(key, def);
        String s = section.getString(key, String.valueOf(def));
        if (s.equals("true") || s.equals("yes") || s.equals("是") || s.equals("真") || s.equals("开")) return true;
        if (s.equals("false") || s.equals("no") || s.equals("否") || s.equals("假") || s.equals("关")) return false;
        return def;
    }

    public static char[] getInventory(MemorySection config, String key) {
        return String.join("", config.getStringList(key)).toCharArray();
    }
}
