package top.mrxiaom.sweet.inventory.func.menus;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MenuPageGuide {
    private final List<Character> slots;
    private final List<char[]> pages;
    MenuPageGuide(MenuConfig parent, boolean alt, ConfigurationSection section) {
        this.slots = loadSlots(alt, section);
        if (slots.isEmpty()) {
            throw new IllegalArgumentException("没有将分页器添加到布局的任意格子中");
        }
        this.pages = new ArrayList<>();
        for (String page : section.getStringList(alt ? "页面" : "pages")) {
            pages.add(page.toCharArray());
        }
    }

    @NotNull
    public List<Character> slots() {
        return slots;
    }

    @NotNull
    public List<char[]> pages() {
        return pages;
    }

    public char @Nullable [] page(int page) {
        return page < 1 || page > pages.size() ? null : pages.get(page - 1);
    }

    public boolean hasPrevPage(int page) {
        return page > 1;
    }

    public boolean hasNextPage(int page) {
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
