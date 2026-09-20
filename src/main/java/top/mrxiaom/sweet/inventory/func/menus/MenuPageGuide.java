package top.mrxiaom.sweet.inventory.func.menus;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MenuPageGuide {
    private final MenuConfig config;
    private final List<Character> slots;
    private final List<SlotChar[]> pages;
    protected MenuPageGuide(MenuConfig parent, boolean alt, ConfigurationSection section) {
        this.config = parent;
        this.slots = loadSlots(alt, section);
        if (slots.isEmpty()) {
            throw new IllegalArgumentException("没有将分页器添加到布局的任意格子中");
        }
        SlotChar[] main = parent.inventoryChars();
        List<Integer> slotIndexes = new ArrayList<>();
        for (int i = 0; i < main.length; i++) {
            // 建立 pages 中每一页的图标位置索引与父菜单的图标索引对应关系
            if (slots.contains(main[i].value())) {
                slotIndexes.add(i);
            }
        }
        this.pages = new ArrayList<>();
        Map<Character, Integer> pageSequenceMap = new HashMap<>();
        for (String page : section.getStringList(alt ? "页面" : "pages")) {
            // 读取格子位置和顺序，限制数量为父菜单中已设置分页的槽位数量
            pages.add(parseInventory(SlotChar.EnumType.PAGE_GUIDE, pageSequenceMap, page.toCharArray(), slotIndexes.size()));
        }
    }

    protected static SlotChar[] parseInventory(SlotChar.EnumType type, char[] input) {
        return parseInventory(type, new HashMap<>(), input, null);
    }

    protected static SlotChar[] parseInventory(SlotChar.EnumType type, Map<Character, Integer> map, char[] input, Integer maxLength) {
        int length = maxLength == null ? input.length : Math.min(maxLength, input.length);
        SlotChar[] page = new SlotChar[length];
        for (int i = 0; i < length; i++) {
            char ch = input[i];
            int sequence = plusSequence(map, ch);
            page[i] = new SlotChar(type, sequence, ch);
        }
        return page;
    }

    private static int plusSequence(Map<Character, Integer> map, Character ch) {
        int sequence = map.getOrDefault(ch, 0) + 1;
        map.put(ch, sequence);
        return sequence;
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
    @Deprecated
    public List<char[]> pages() {
        List<char[]> list = new ArrayList<>();
        for (SlotChar[] input : pages) {
            char[] page = new char[input.length];
            for (int i = 0; i < input.length; i++) {
                page[i] = input[i].value();
            }
            list.add(page);
        }
        return list;
    }

    /**
     * 获取某一页的分页内容
     * @param page 第几页，从 <code>1</code> 开始
     */
    @Deprecated
    public @Nullable char[] page(@Range(from=1, to=Integer.MAX_VALUE) int page) {
        // noinspection ConstantValue
        if (page < 1 || page > pages.size()) {
            return null;
        }
        SlotChar[] input = pages.get(page - 1);
        char[] array = new char[input.length];
        for (int i = 0; i < input.length; i++) {
            array[i] = input[i].value();
        }
        return array;
    }

    /**
     * 获取每一页的分页内容
     */
    @NotNull
    public List<SlotChar[]> allPages() {
        return pages;
    }

    /**
     * 获取某一页的分页内容
     * @param page 第几页，从 <code>1</code> 开始
     */
    public @Nullable SlotChar[] onePage(@Range(from=1, to=Integer.MAX_VALUE) int page) {
        // noinspection ConstantValue
        if (page < 1 || page > pages.size()) {
            return null;
        }
        return pages.get(page - 1);
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
