package top.mrxiaom.sweet.inventory.func.menus;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.func.gui.LoadedIcon;
import top.mrxiaom.sweet.inventory.SweetInventory;

import java.util.*;

public class MenuConfig {
    final String title;
    final char[] inventory;
    final Map<Character, List<MenuIcon>> iconsByChar;
    final Map<String, MenuIcon> iconsByName;
    final @Nullable String bindCommand;
    final List<String> openCommands;
    final int updateInterval;

    MenuConfig(String title, char[] inventory, Map<Character, List<MenuIcon>> iconsByChar, Map<String, MenuIcon> iconsByName, String bindCommand, List<String> openCommands, int updateInterval) {
        this.title = title;
        this.inventory = inventory;
        this.iconsByChar = iconsByChar;
        this.iconsByName = iconsByName;
        this.bindCommand = bindCommand;
        this.openCommands = openCommands;
        this.updateInterval = updateInterval;
    }

    public String getTitle() {
        return title;
    }

    public char[] getInventory() {
        return inventory;
    }

    public Map<Character, List<MenuIcon>> getIconsByChar() {
        return iconsByChar;
    }

    public Map<String, MenuIcon> getIconsByName() {
        return iconsByName;
    }

    @Nullable
    public String getBindCommand() {
        return bindCommand;
    }

    public List<String> getOpenCommands() {
        return openCommands;
    }

    public int getUpdateInterval() {
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
        SweetInventory plugin = SweetInventory.getInstance();
        String title = config.getString(alt ? "标题" : "title", "");
        char[] inventory = getInventory(config,  alt ? "布局" : "inventory");
        String bindCommand = config.getString(alt ? "绑定界面命令" : "bind-command", null);
        List<String> openCommands = config.getStringList(alt ? "打开界面执行命令" : "open-commands");
        int updateInterval = Math.max(0, config.getInt(alt ? "更新周期" : "update-interval", 0));
        MenuPageGuide pageGuide = MenuPageGuide.load(alt, config.getConfigurationSection(alt ? "分页向导" : "page-guide"));

        Map<Character, List<MenuIcon>> iconsByChar = new HashMap<>();
        Map<String, MenuIcon> iconsByName = new HashMap<>();
        ConfigurationSection section = config.getConfigurationSection(alt ? "图标列表" : "items");
        if (section != null) for (String key : section.getKeys(false)) {
            ConfigurationSection s = section.getConfigurationSection(key);
            MenuIcon icon;
            try {
                if (s == null) throw new IllegalArgumentException("找不到配置");
                icon = MenuIcon.load(alt, s, key);
            } catch (Throwable t) {
                plugin.error(String.format("[%s.yml] 加载图标 %s 失败", id, key) + t.getMessage());
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
        return new MenuConfig(title, inventory, iconsByChar, iconsByName, bindCommand, openCommands, updateInterval, pageGuide);
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
