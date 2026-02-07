package top.mrxiaom.sweet.inventory.func.menus;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class MenuPageGuide {
    private final MenuConfig config;
    private final List<Character> slots;
    private final List<char[]> pages;
    protected MenuPageGuide(MenuConfig parent, boolean alt, ConfigurationSection section) {
        this.config = parent;
        this.slots = loadSlots(alt, section);
        if (slots.isEmpty()) {
            throw new IllegalArgumentException("没有将分页器添加到布局的任意格子中");
        }
        this.pages = new ArrayList<>();
        for (String page : section.getStringList(alt ? "页面" : "pages")) {
            pages.add(page.toCharArray());
        }
    }

    /**
     * 获取这个分页配置属于哪个菜单
     */
    public MenuConfig config() {
        return config;
    }

    /**
     * 获取菜单布局中的哪些格子用于分页内容
     */
    @NotNull
    public List<Character> slots() {
        return slots;
    }

    /**
     * 获取每一页的分页内容
     */
    @NotNull
    public List<char[]> pages() {
        return pages;
    }

    /**
     * 获取某一页的分页内容
     * @param page 第几页，从 <code>1</code> 开始
     */
    public char @Nullable [] page(@Range(from=1, to=Integer.MAX_VALUE) int page) {
        // noinspection ConstantValue
        return page < 1 || page > pages.size() ? null : pages.get(page - 1);
    }

    /**
     * 获取指定页码是否还有上一页可用
     * @param page 第几页，从 <code>1</code> 开始
     */
    public boolean hasPrevPage(@Range(from=1, to=Integer.MAX_VALUE) int page) {
        return page > 1;
    }

    /**
     * 获取指定页码是否还有下一页可用
     * @param page 第几页，从 <code>1</code> 开始
     */
    public boolean hasNextPage(@Range(from=1, to=Integer.MAX_VALUE) int page) {
        return page < pages.size();
    }

    public static MenuPageGuide load(MenuConfig parent, boolean alt, ConfigurationSection section) {
        return new MenuPageGuide(parent, alt, section);
    }

    private static List<Character> loadSlots(boolean alt, ConfigurationSection section) {
        List<Character> list = new ArrayList<>();
        String slotKey = alt ? "内容字符" : "slot";
        String slotsKey = alt ? "内容字符" : "slots";

        if (section.isList(slotsKey)) {
            list.addAll(section.getCharacterList(slotsKey));
        }
        if (section.isString(slotKey) || section.isInt(slotKey)) {
            String slotStr = section.getString(slotKey, "");
            if (!slotStr.isEmpty()) {
                char[] charArray = slotStr.toCharArray();
                for (char c : charArray) {
                    list.add(c);
                }
            }
        }
        return list;
    }
}
