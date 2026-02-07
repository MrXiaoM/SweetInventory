package top.mrxiaom.sweet.inventory.func.menus;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permissible;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;
import top.mrxiaom.pluginbase.actions.ActionProviders;
import top.mrxiaom.pluginbase.api.IAction;
import top.mrxiaom.pluginbase.utils.CollectionUtils;
import top.mrxiaom.sweet.inventory.SweetInventory;

import java.util.*;

@SuppressWarnings("UnusedReturnValue")
public class MenuConfig {
    private final String id;
    private final List<String> aliasIds;
    private final String title;
    private final char[] inventory;
    private final Map<Character, List<MenuIcon>> iconsByChar;
    private final Map<String, MenuIcon> iconsByName;
    private final @Nullable String bindCommand;
    private final List<IAction> openCommands;
    private final int updateInterval;
    private final @Nullable MenuPageGuide pageGuide;

    protected MenuConfig(boolean alt, String id, MemoryConfiguration config) {
        SweetInventory plugin = SweetInventory.getInstance();
        this.id = id;
        this.aliasIds = config.getStringList(alt ? "菜单别名" : "alias-ids");
        this.title = config.getString(alt ? "标题" : "title", "");
        this.inventory = String.join("", config.getStringList(alt ? "布局" : "inventory")).toCharArray();
        if (inventory.length == 0 || (inventory.length % 9 != 0)) {
            throw new IllegalArgumentException("菜单布局配置有误，长度应为 9 的倍数 (当前 " + inventory.length + ")" );
        }
        this.bindCommand = config.getString(alt ? "绑定界面命令" : "bind-command", null);
        this.openCommands = ActionProviders.loadActions(config, alt ? "打开界面执行命令" : "open-commands");
        this.updateInterval = Math.max(0, config.getInt(alt ? "更新周期" : "update-interval", 0));
        ConfigurationSection pageGuideSection = config.getConfigurationSection(alt ? "分页向导" : "page-guide");
        if (pageGuideSection != null) {
            this.pageGuide = MenuPageGuide.load(this, alt, pageGuideSection);
        } else {
            this.pageGuide = null;
        }

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
                List<MenuIcon> list = CollectionUtils.getOrPut(iconsByChar, c, () -> new ArrayList<>());
                list.add(icon);
            }
        }
        for (List<MenuIcon> list : iconsByChar.values()) {
            list.sort(Comparator.comparingInt(MenuIcon::priority));
        }
    }

    /**
     * 获取界面配置 ID
     */
    public String id() {
        return id;
    }

    /**
     * 获取界面配置别名
     */
    public List<String> aliasIds() {
        return aliasIds;
    }

    /**
     * 检查目标是否有权限可以打开这个菜单
     * @param p 目标
     */
    public boolean hasPermission(Permissible p) {
        return p.hasPermission("sweet.inventory.open.menu." + id);
    }

    /**
     * 获取菜单标题
     */
    public String title() {
        return title;
    }

    /**
     * 获取菜单布局字符数组
     */
    public char[] inventory() {
        return inventory;
    }

    /**
     * 获取某一页的菜单布局字符数组
     * @param page 页码
     */
    public char[] inventory(@Range(from = 1, to = Integer.MAX_VALUE) int page) {
        if (pageGuide != null) {
            char[] pageInv = pageGuide.page(page);
            if (pageInv != null) {
                char[] inv = new char[inventory.length];
                int length = pageInv.length;
                for (int i = 0, j = 0; i < inventory.length; i++) {
                    char ch = inventory[i];
                    if (j < length && pageGuide.slots().contains(ch)) {
                        inv[i] = pageInv[j];
                        j++;
                        continue;
                    }
                    inv[i] = ch;
                }
                return inv;
            }
        }
        return inventory;
    }

    /**
     * 获取按模板字符储存的图标列表
     * @return 图标列表，已经过优先级排序
     */
    public Map<Character, List<MenuIcon>> iconsByChar() {
        return iconsByChar;
    }

    /**
     * 获取按模板字符储存的图标列表
     * @param ch 模板字符
     * @return 图标列表，已经过优先级排序
     */
    public List<MenuIcon> iconsByChar(char ch) {
        return iconsByChar.get(ch);
    }

    /**
     * 获取按图标名称储存的图标列表
     * @return 图标列表，已经过优先级排序
     */
    public Map<String, MenuIcon> iconsByName() {
        return iconsByName;
    }

    /**
     * 获取按图标名称储存的图标列表
     * @param name 图标名称
     * @return 图标列表，已经过优先级排序
     */
    public MenuIcon iconByName(String name) {
        return iconsByName.get(name);
    }

    /**
     * 获取这个菜单绑定的自定义命令
     */
    @Nullable
    public String bindCommand() {
        return bindCommand;
    }

    /**
     * 获取打开菜单时执行的操作
     */
    public List<IAction> openCommands() {
        return openCommands;
    }

    /**
     * 获取菜单刷新周期 (ticks)
     */
    public int updateInterval() {
        return updateInterval;
    }

    /**
     * 获取菜单分页配置
     */
    @Nullable
    public MenuPageGuide pageGuide() {
        return pageGuide;
    }

    /**
     * 创建一个菜单实例，但不打开菜单
     * @param player 要打开菜单的玩家
     * @see MenuInstance#create(MenuConfig, Player)
     */
    public MenuInstance create(Player player) {
        return MenuInstance.create(this, player);
    }

    /**
     * 创建一个菜单实例，并打开菜单
     * @param player 要打开菜单的玩家
     * @see MenuInstance#create(MenuConfig, Player)
     */
    public MenuInstance open(Player player) {
        MenuInstance menu = create(player);
        menu.open();
        return menu;
    }

    public static MenuConfig load(boolean alt, String id, MemoryConfiguration config) {
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
}
