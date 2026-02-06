package top.mrxiaom.sweet.inventory.func.menus;

import de.tr7zw.changeme.nbtapi.NBT;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.BukkitPlugin;
import top.mrxiaom.pluginbase.api.IAction;
import top.mrxiaom.pluginbase.func.gui.IModifier;
import top.mrxiaom.pluginbase.func.gui.LoadedIcon;
import top.mrxiaom.pluginbase.utils.AdventureItemStack;
import top.mrxiaom.pluginbase.utils.ItemStackUtil;
import top.mrxiaom.pluginbase.utils.Pair;
import top.mrxiaom.pluginbase.utils.Util;
import top.mrxiaom.pluginbase.utils.depend.PAPI;
import top.mrxiaom.sweet.inventory.fixer.ItemDupeFixer;
import top.mrxiaom.sweet.inventory.requirements.IRequirement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static top.mrxiaom.pluginbase.actions.ActionProviders.loadActions;
import static top.mrxiaom.pluginbase.func.gui.IModifier.fit;
import static top.mrxiaom.sweet.inventory.func.menus.MenuConfig.getBoolean;
import static top.mrxiaom.sweet.inventory.requirements.RequirementsRegistry.loadRequirements;

public class MenuIcon {
    private final boolean adventure = BukkitPlugin.getInstance().options.adventure();
    private final @NotNull ConfigurationSection section;
    private final @NotNull String id;
    private final @NotNull List<Character> slots;
    private final @NotNull String material;
    private final int amount;
    private final @NotNull String display;
    private final @NotNull List<String> lore;
    private final boolean glow;
    private final @Nullable Integer customModelData;
    private final @NotNull Map<String, String> nbtStrings;
    private final @NotNull Map<String, String> nbtInts;
    private final boolean needsUpdate;
    private final int priority;
    private final @NotNull List<IRequirement> viewRequirements;
    private final @NotNull List<IAction> viewDenyCommands;
    private final @Nullable Click leftClick;
    private final @Nullable Click rightClick;
    private final @Nullable Click shiftLeftClick;
    private final @Nullable Click shiftRightClick;
    private final @Nullable Click dropClick;
    private final @Nullable Click ctrlDropClick;

    public MenuIcon(boolean alt, ConfigurationSection config, String id) {
        this.id = id;
        this.section = config;
        this.slots = loadSlots(alt, config);
        if (slots.isEmpty()) {
            throw new IllegalArgumentException("没有将图标 " + id + " 添加到布局的任意格子中");
        }
        ConfigurationSection section;

        String material, materialStr = config.getString(alt ? "物品" : "material");
        if (materialStr != null) {
            if (!materialStr.contains(":") && config.contains(alt ? "子ID" : "data")) { // 兼容旧的选项
                material = materialStr + ":" + config.getInt(alt ? "子ID" : "data");
            } else material = materialStr;
        } else material = "PAPER";
        this.material = material.toUpperCase();

        this.amount = config.getInt(alt ? "数量" : "amount", 1);
        this.display = config.getString(alt ? "名字" : "display", "");
        this.lore = config.getStringList(alt ? "描述" : "lore");
        this.glow = config.getBoolean(alt ? "发光" : "glow");
        this.customModelData = config.contains(alt ? "模型数据" : "custom-model-data") ? config.getInt(alt ? "模型数据" : "custom-model-data") : null;
        this.nbtStrings = new HashMap<>();
        section = config.getConfigurationSection(alt ? "nbt字符串" : "nbt-strings");
        if (section != null) for (String key : section.getKeys(false)) {
            nbtStrings.put(key, section.getString(key, ""));
        }
        this.nbtInts = new HashMap<>();
        section = config.getConfigurationSection(alt ? "nbt整数" : "nbt-ints");
        if (section != null) for (String key : section.getKeys(false)) {
            nbtInts.put(key, section.getString(key, ""));
        }

        this.needsUpdate = getBoolean(alt, config, alt ? "需要更新" : "needs-update");
        this.priority = config.getInt(alt ? "优先级" : "priority");
        this.viewRequirements = loadRequirements(alt, config, alt ? "查看图标" : "view");
        this.viewDenyCommands = loadActions(config, alt ? "查看图标.不满足需求执行" : "view.deny-commands");
        this.leftClick = Click.load(alt, config, alt ? "左键点击" : "left-click");
        this.rightClick = Click.load(alt, config, alt ? "右键点击" : "right-click");
        this.shiftLeftClick = Click.load(alt, config, alt ? "Shift左键点击" : "shift-left-click");
        this.shiftRightClick = Click.load(alt, config, alt ? "Shift右键点击" : "shift-right-click");
        this.dropClick = Click.load(alt, config, alt ? "Q键点击" : "drop-click");
        this.ctrlDropClick = Click.load(alt, config, alt ? "Ctrl+Q键点击" : "ctrl-drop-click");
    }

    @NotNull
    public ConfigurationSection section() {
        return section;
    }

    @NotNull
    public String id() {
        return id;
    }

    @NotNull
    public String material() {
        return material;
    }

    public int amount() {
        return amount;
    }

    @NotNull
    public String display() {
        return display;
    }

    @NotNull
    public List<String> lore() {
        return lore;
    }

    public boolean glow() {
        return glow;
    }

    @Nullable
    public Integer customModelData() {
        return customModelData;
    }

    @NotNull
    public Map<String, String> nbtStrings() {
        return nbtStrings;
    }

    @NotNull
    public Map<String, String> nbtInts() {
        return nbtInts;
    }

    @NotNull
    public List<Character> slots() {
        return slots;
    }

    /**
     * 生成一个新的物品
     * @param player 玩家，用于替换 PAPI 变量。使用 <code>null</code> 则不替换 PAPI 变量
     * @see LoadedIcon#generateIcon(ItemStack, Player, IModifier, IModifier)
     */
    @NotNull
    public ItemStack generateIcon(Player player) {
        return generateIcon(player, null, null);
    }

    /**
     * 生成一个新的物品
     * @param player 玩家，用于替换 PAPI 变量。使用 <code>null</code> 则不替换 PAPI 变量
     * @param displayNameModifier 物品名称修饰器
     * @param loreModifier 物品Lore修饰器
     * @see LoadedIcon#generateIcon(ItemStack, Player, IModifier, IModifier)
     */
    @NotNull
    public ItemStack generateIcon(Player player, @Nullable IModifier<String> displayNameModifier, @Nullable IModifier<List<String>> loreModifier) {
        if (material.equals("AIR") || amount == 0) return new ItemStack(Material.AIR);
        Pair<Material, Integer> pair = ItemStackUtil.parseMaterial(this.material);
        ItemStack item = pair == null ? new ItemStack(Material.PAPER) : ItemStackUtil.legacy(pair);
        return generateIcon(item, player, displayNameModifier, loreModifier);
    }

    /**
     * 基于已有物品，覆盖图标配置到该物品上。这个方法会忽略 <code>material</code> 选项。
     * @param item 原物品
     * @param player 玩家，用于替换 PAPI 变量。使用 <code>null</code> 则不替换 PAPI 变量
     * @return <code>item</code> 的引用
     * @see LoadedIcon#generateIcon(ItemStack, Player, IModifier, IModifier)
     */
    @NotNull
    public ItemStack generateIcon(@Nullable ItemStack item, @Nullable Player player) {
        return generateIcon(item, player, null, null);
    }

    /**
     * 基于已有物品，覆盖图标配置到该物品上。这个方法会忽略 <code>material</code> 选项。
     * @param item 原物品
     * @param player 玩家，用于替换 PAPI 变量。使用 <code>null</code> 则不替换 PAPI 变量
     * @param displayNameModifier 物品名称修饰器
     * @param loreModifier 物品Lore修饰器
     * @return 如果 <code>item</code> 不是 <code>null</code>，返回原物品的引用
     */
    @NotNull
    public ItemStack generateIcon(@Nullable ItemStack item, @Nullable Player player, @Nullable IModifier<String> displayNameModifier, @Nullable IModifier<List<String>> loreModifier) {
        if (item == null || amount == 0) return new ItemStack(Material.AIR);
        item.setAmount(amount);
        applyItemMeta(item, player, displayNameModifier, loreModifier);
        return item;
    }

    /**
     * 应用该图标配置中的 物品名、物品Lore、发光、自定义标记… 等元数据到指定物品
     * @param item 原物品
     * @param player 玩家，用于替换 PAPI 变量。使用 <code>null</code> 则不替换 PAPI 变量
     * @see LoadedIcon#applyItemMeta(ItemStack, Player, IModifier, IModifier)
     */
    public void applyItemMeta(@NotNull ItemStack item, @Nullable Player player) {
        applyItemMeta(item, player, null, null);
    }

    /**
     * 应用该图标配置中的 物品名、物品Lore、发光、自定义标记… 等元数据到指定物品
     * @param item 原物品
     * @param player 玩家，用于替换 PAPI 变量。使用 <code>null</code> 则不替换 PAPI 变量
     * @param displayNameModifier 物品名称修饰器
     * @param loreModifier 物品Lore修饰器
     */
    public void applyItemMeta(@NotNull ItemStack item, @Nullable Player player, @Nullable IModifier<String> displayNameModifier, @Nullable IModifier<List<String>> loreModifier) {
        if (!display.isEmpty()) {
            String displayName = PAPI.setPlaceholders(player, fit(displayNameModifier, display));
            if (adventure) AdventureItemStack.setItemDisplayName(item, displayName);
            else ItemStackUtil.setItemDisplayName(item, displayName);
        }
        if (!lore.isEmpty()) {
            List<String> loreList = PAPI.setPlaceholders(player, fit(loreModifier, lore));
            if (adventure) AdventureItemStack.setItemLoreMiniMessage(item, loreList);
            else ItemStackUtil.setItemLore(item, loreList);
        }
        if (glow) ItemStackUtil.setGlow(item);
        if (customModelData != null) ItemStackUtil.setCustomModelData(item, customModelData);
        NBT.modify(item, nbt -> {
            nbt.setString(ItemDupeFixer.TAG, id);
            if (!nbtStrings.isEmpty() || !nbtInts.isEmpty()) {
                for (Map.Entry<String, String> entry : nbtStrings.entrySet()) {
                    String value = PAPI.setPlaceholders(player, entry.getValue());
                    nbt.setString(entry.getKey(), value);
                }
                for (Map.Entry<String, String> entry : nbtInts.entrySet()) {
                    String value = PAPI.setPlaceholders(player, entry.getValue());
                    Integer i = Util.parseInt(value).orElse(null);
                    if (i == null) continue;
                    nbt.setInteger(entry.getKey(), i);
                }
            }
        });
    }

    /**
     * 是否需要在 tick 循环中更新物品信息
     */
    public boolean needsUpdate() {
        return needsUpdate;
    }

    /**
     * 图标显示优先级，数值越小越先显示
     */
    public int priority() {
        return priority;
    }

    /**
     * 图标查看需求，不满足需求时不添加图标
     */
    @NotNull
    public List<IRequirement> viewRequirements() {
        return viewRequirements;
    }

    /**
     * 图标查看需求不满足时执行的操作
     */
    @NotNull
    public List<IAction> viewDenyCommands() {
        return viewDenyCommands;
    }

    /**
     * 获取左键点击需求与执行操作
     */
    @Nullable
    public Click leftClick() {
        return leftClick;
    }

    /**
     * 获取右键点击需求与执行操作
     */
    @Nullable
    public Click rightClick() {
        return rightClick;
    }

    /**
     * 获取Shift+左键点击需求与执行操作
     */
    @Nullable
    public Click shiftLeftClick() {
        return shiftLeftClick;
    }

    /**
     * 获取Shift+右键点击需求与执行操作
     */
    @Nullable
    public Click shiftRightClick() {
        return shiftRightClick;
    }

    /**
     * 获取Q键点击需求与执行操作
     */
    @Nullable
    public Click dropClick() {
        return dropClick;
    }

    /**
     * 获取Ctrl+Q键点击需求与执行操作
     */
    @Nullable
    public Click ctrlDropClick() {
        return ctrlDropClick;
    }

    /**
     * 从配置中加载菜单图标配置
     * @param alt 是否使用中文配置
     * @param section 图标配置
     * @param id 图标ID
     * @throws IllegalArgumentException 当图标配置错误时抛出
     */
    @NotNull
    public static MenuIcon load(boolean alt, @NotNull ConfigurationSection section, @NotNull String id) {
        return new MenuIcon(alt, section, id);
    }

    @NotNull
    private static List<Character> loadSlots(boolean alt, @NotNull ConfigurationSection section) {
        List<Character> list = new ArrayList<>();
        String slotKey = alt ? "格子" : "slot";
        String slotsKey = alt ? "格子" : "slots";

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
