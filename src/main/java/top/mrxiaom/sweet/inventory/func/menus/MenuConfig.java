package top.mrxiaom.sweet.inventory.func.menus;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permissible;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;
import top.mrxiaom.pluginbase.actions.ActionProviders;
import top.mrxiaom.pluginbase.api.IAction;
import top.mrxiaom.pluginbase.utils.CollectionUtils;
import top.mrxiaom.pluginbase.utils.ConfigUtils;
import top.mrxiaom.sweet.inventory.SweetInventory;
import top.mrxiaom.sweet.inventory.func.menus.arguments.MenuArguments;

import java.util.*;

@SuppressWarnings("UnusedReturnValue")
public class MenuConfig {
    private final @NotNull String id;
    private final @NotNull List<String> aliasIds;
    private final @NotNull String title;
    private final char[] inventory;
    private final @NotNull Map<Character, List<MenuIcon>> iconsByChar;
    private final @NotNull Map<String, MenuIcon> iconsByName;
    private final @Nullable String bindCommand;
    private final @NotNull MenuArguments menuArguments;
    private final @NotNull List<IAction> openCommands;
    private final int updateInterval;
    private final @Nullable MenuPageGuide pageGuide;

    protected MenuConfig(boolean alt, @NotNull String id, @NotNull MemoryConfiguration config) {
        SweetInventory plugin = SweetInventory.getInstance();
        this.id = id;
        this.aliasIds = config.getStringList(alt ? "菜单别名" : "alias-ids");
        this.title = config.getString(alt ? "标题" : "title", "");
        this.inventory = String.join("", config.getStringList(alt ? "布局" : "inventory")).toCharArray();
        if (inventory.length == 0 || (inventory.length % 9 != 0)) {
            throw new IllegalArgumentException("菜单布局配置有误，长度应为 9 的倍数 (当前 " + inventory.length + ")" );
        }
        String bindCommandKey = alt ? "绑定界面命令" : "bind-command";
        if (config.isConfigurationSection(bindCommandKey)) {
            ConfigurationSection section = config.getConfigurationSection(bindCommandKey);
            assert section != null;
            this.bindCommand = section.getString(alt ? "名称" : "name", null);
            List<IAction> helpActions = ActionProviders.loadActions(section, alt ? "帮助操作" : "help-actions");
            List<ConfigurationSection> arguments = ConfigUtils.getSectionList(section, alt ? "参数" : "arguments");
            this.menuArguments = MenuArguments.load(alt, helpActions, arguments);
        } else {
            this.bindCommand = config.getString(bindCommandKey, null);
            this.menuArguments = MenuArguments.EMPTY;
        }
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
    @NotNull
    public String id() {
        return id;
    }

    /**
     * 获取界面配置别名
     */
    @NotNull
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
    @NotNull
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
    @NotNull
    public Map<Character, List<MenuIcon>> iconsByChar() {
        return iconsByChar;
    }

    /**
     * 获取按模板字符储存的图标列表
     * @param ch 模板字符
     * @return 图标列表，已经过优先级排序
     */
    @Nullable
    public List<MenuIcon> iconsByChar(char ch) {
        return iconsByChar.get(ch);
    }

    /**
     * 获取按图标名称储存的图标列表
     * @return 图标列表，已经过优先级排序
     */
    @NotNull
    public Map<String, MenuIcon> iconsByName() {
        return iconsByName;
    }

    /**
     * 获取按图标名称储存的图标列表
     * @param name 图标名称
     * @return 图标列表，已经过优先级排序
     */
    @Nullable
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
     * 获取这个菜单的自定义命令参数
     */
    @NotNull
    public MenuArguments menuArguments() {
        return menuArguments;
    }

    /**
     * 获取打开菜单时执行的操作
     */
    @NotNull
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
    @NotNull
    public MenuInstance create(Player player) {
        return MenuInstance.create(this, player);
    }

    /**
     * 创建一个菜单实例，但不打开菜单
     * @param player 要打开菜单的玩家
     * @param args 命令参数
     * @return 当命令参数解析有误时，返回 <code>null</code>
     * @see MenuInstance#create(MenuConfig, Player, String[])
     */
    @Nullable
    public MenuInstance create(Player player, String[] args) {
        return MenuInstance.create(this, player, args);
    }

    /**
     * 创建一个菜单实例，并打开菜单
     * @param player 要打开菜单的玩家
     * @see MenuInstance#create(MenuConfig, Player)
     */
    @NotNull
    public MenuInstance open(Player player) {
        MenuInstance menu = create(player);
        menu.open();
        return menu;
    }

    /**
     * 创建一个菜单实例，并打开菜单
     * @param player 要打开菜单的玩家
     * @param args 命令参数
     * @return 当命令参数解析有误时，返回 <code>null</code>
     * @see MenuInstance#create(MenuConfig, Player, String[])
     */
    @Nullable
    public MenuInstance open(Player player, String[] args) {
        MenuInstance menu = create(player, args);
        if (menu != null) {
            menu.open();
            return menu;
        }
        return null;
    }

    @NotNull
    public static MenuConfig load(boolean alt, @NotNull String id, @NotNull MemoryConfiguration config) {
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
