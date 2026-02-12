package top.mrxiaom.sweet.inventory.func.menus;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;
import top.mrxiaom.pluginbase.actions.ActionProviders;
import top.mrxiaom.pluginbase.api.IAction;
import top.mrxiaom.pluginbase.func.GuiManager;
import top.mrxiaom.pluginbase.func.gui.IModifier;
import top.mrxiaom.pluginbase.gui.IGuiHolder;
import top.mrxiaom.pluginbase.utils.AdventureUtil;
import top.mrxiaom.pluginbase.utils.ListPair;
import top.mrxiaom.pluginbase.utils.Pair;
import top.mrxiaom.pluginbase.utils.Util;
import top.mrxiaom.pluginbase.utils.depend.PAPI;
import top.mrxiaom.sweet.inventory.SweetInventory;
import top.mrxiaom.sweet.inventory.func.menus.arguments.MenuArguments;
import top.mrxiaom.sweet.inventory.requirements.IRequirement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public class MenuInstance implements IGuiHolder {
    private final SweetInventory plugin = SweetInventory.getInstance();
    private final MenuConfig config;
    private final Player player;
    private final Map<Integer, MenuIcon> currentIcons = new HashMap<>();
    private final Map<String, Object> variables = new HashMap<>();
    private int updateCounter;
    private Component title;
    private Inventory inventory;
    private int page = 1;
    private char[] inventoryTemplate;
    private boolean actionLock = false;
    protected MenuInstance(MenuConfig config, Player player) {
        this.config = config;
        this.player = player;
        this.updateCounter = config.updateInterval();
    }

    public void onTick() {
        if (actionLock || config.updateInterval() == 0) return;
        if (--updateCounter == 0) {
            updateCounter = config.updateInterval();
            // 更新需要更新的图标物品内容
            Map<Integer, ItemStack> commits = new HashMap<>();
            for (Map.Entry<Integer, MenuIcon> entry : currentIcons.entrySet()) {
                MenuIcon icon = entry.getValue();
                if (!icon.needsUpdate()) continue;
                ItemStack item = icon.generateIcon(player);
                commits.put(entry.getKey(), item);
            }
            if (!commits.isEmpty()) {
                InventoryView inv = player.getOpenInventory();
                InventoryHolder holder = Util.getHolder(inv.getTopInventory());
                // 确保玩家打开的还是这个界面再更新
                if (holder == this) {
                    for (Map.Entry<Integer, ItemStack> entry : commits.entrySet()) {
                        inv.setItem(entry.getKey(), entry.getValue());
                    }
                    Util.submitInvUpdate(player);
                }
            }
        }
    }

    /**
     * 获取菜单配置
     */
    public MenuConfig config() {
        return config;
    }

    /**
     * 获取当前已计算的菜单标题
     */
    public Component title() {
        return title;
    }

    /**
     * 获取插件实例
     */
    public SweetInventory plugin() {
        return plugin;
    }

    /**
     * 获取浏览这个菜单的玩家实例
     */
    @Override
    public Player getPlayer() {
        return player;
    }

    /**
     * 获取是否还有上一页可用
     */
    public boolean hasPrevPage() {
        MenuPageGuide pageGuide = config.pageGuide();
        return pageGuide != null && pageGuide.hasPrevPage(page);
    }

    /**
     * 获取是否还有下一页可用
     */
    public boolean hasNextPage() {
        MenuPageGuide pageGuide = config.pageGuide();
        return pageGuide != null && pageGuide.hasNextPage(page);
    }

    /**
     * 获取当前页码
     */
    public int page() {
        return page;
    }

    /**
     * 设置当前页码
     */
    public void page(int page) {
        this.page = page;
    }

    /**
     * 打开或重新打开菜单，会执行打开操作命令
     */
    @Override
    public void open() {
        GuiManager.inst().openGui(this);
        ActionProviders.run(plugin, player, config.openCommands());
    }

    /**
     * 打开或刷新菜单，不会执行打开操作命令
     */
    public void refresh() {
        GuiManager.inst().openGui(this);
    }

    private void updateInventory(BiConsumer<Integer, ItemStack> setItem) {
        currentIcons.clear();
        inventoryTemplate = config.inventory(page);
        ListPair<String, Object> r = newReplacements();
        IModifier<String> displayModifier = str -> Pair.replace(str, r);
        IModifier<List<String>> loreModifier = list -> Pair.replace(list, r);
        for (int i = 0; i < inventoryTemplate.length; i++) {
            char id = inventoryTemplate[i];
            if (id == ' ' || id == '　' || Character.isSpaceChar(id)) { // 忽略空格
                setItem.accept(i, null);
                continue;
            }
            ItemStack item = null;
            List<MenuIcon> list = config.iconsByChar(id); // list 已经过优先级排序
            if (list != null && !list.isEmpty()) {
                for (MenuIcon icon : list) {
                    // 满足条件时，释放图标到界面
                    if (checkRequirements(icon.viewRequirements(), icon.viewDenyCommands(), r)) {
                        item = icon.generateIcon(player, displayModifier, loreModifier);
                        currentIcons.put(i, icon);
                        break;
                    }
                }
            }
            setItem.accept(i, item);
        }
        actionLock = false;
    }

    /**
     * 新建一个替换变量列表并返回
     */
    public ListPair<String, Object> newReplacements() {
        ListPair<String, Object> r = new ListPair<>();
        MenuPageGuide pageGuide = config.pageGuide();
        r.add("%page%", page);
        r.add("%max_page%", pageGuide == null ? 1 : pageGuide.pages().size());
        for (Map.Entry<String, Object> entry : variables.entrySet()) {
            r.add("${" + entry.getKey() + "}", entry.getValue());
        }
        return r;
    }

    /**
     * 获取菜单所有的临时变量
     */
    @NotNull
    public Map<String, Object> variables() {
        return variables;
    }

    /**
     * 获取菜单临时变量的值
     * @param name 变量名
     * @return 如果变量不存在，返回 <code>null</code>
     */
    @Nullable
    public Object variable(@NotNull String name) {
        return variables.get(name);
    }

    /**
     * 设置菜单临时变量的值
     * @param name 变量名
     * @param value 变量值，如果传入 <code>null</code>，则代表删除变量
     */
    public void variable(@NotNull String name, @Nullable Object value) {
        if (value != null) {
            variables.put(name, value);
        } else {
            variables.remove(name);
        }
    }

    /**
     * 设置菜单临时变量的值
     * @param append 多个临时变量
     */
    public void putVariables(@NotNull Map<String, Object> append) {
        variables.putAll(append);
    }

    @Override
    public Inventory newInventory() {
        String rawTitle = PAPI.setPlaceholders(player, Pair.replace(config.title(), newReplacements()));
        title = AdventureUtil.miniMessage(rawTitle);
        inventory = plugin.createInventory(this, config.inventory().length, rawTitle);
        updateInventory(inventory::setItem);
        return inventory;
    }

    @Override
    public @NonNull Inventory getInventory() {
        return inventory;
    }

    @Override
    public void onClick(InventoryAction action, ClickType click, InventoryType.SlotType slotType,
                        int slot, ItemStack currentItem, ItemStack cursor,
                        InventoryView view, InventoryClickEvent event) {
        actionLock = true;
        event.setCancelled(true);
        MenuIcon icon = currentIcons.get(slot);
        // 点击操作
        if (icon != null) switch (click) {
            case LEFT:
                handleIconClick(icon.leftClick());
                return;
            case RIGHT:
                handleIconClick(icon.rightClick());
                return;
            case SHIFT_LEFT:
                handleIconClick(icon.shiftLeftClick());
                return;
            case SHIFT_RIGHT:
                handleIconClick(icon.shiftRightClick());
                return;
            case DROP:
                handleIconClick(icon.dropClick());
                return;
            case CONTROL_DROP:
                handleIconClick(icon.ctrlDropClick());
                return;
        }
        actionLock = false;
    }

    private void handleIconClick(@Nullable Click click) {
        if (click == null) {
            actionLock = false;
            return;
        }
        plugin.getScheduler().runTask(() -> {
            ListPair<String, Object> r = newReplacements();
            if (checkRequirements(click.requirements(), click.denyCommands(), r)) {
                executeCommands(click.commands(), r);
            }
            actionLock = false;
        });
    }

    /**
     * 获取点击的界面物品索引指向的模板字符
     * @param slot 格子索引
     * @return 模板字符，找不到时返回 <code>null</code>
     */
    @Nullable
    public Character getClickedId(int slot) {
        if (slot >= 0 && slot < inventoryTemplate.length) {
            return inventoryTemplate[slot];
        } else {
            return null;
        }
    }

    /**
     * 获取某个模板字符，截至指定界面物品索引出现过多少次
     * @param id 模板字符
     * @param slot 格子索引
     * @return 模板字符出现次数
     */
    public int getAppearTimes(Character id, int slot) {
        int appearTimes = 0;
        for (int i = 0; i < inventoryTemplate.length; i++) {
            if (id.equals(inventoryTemplate[i])) {
                appearTimes++;
            }
            if (i == slot) break;
        }
        return appearTimes;
    }

    /**
     * 检查是否符合指定需求，不满足需求时执行拒绝命令
     * @param requirements 需求列表
     * @param denyCommands 拒绝时必执行的拒绝命令
     * @param r 变量替换列表
     */
    public boolean checkRequirements(@NotNull List<IRequirement> requirements, @Nullable List<IAction> denyCommands, @Nullable ListPair<String, Object> r) {
        boolean success = true;
        List<IAction> commands = new ArrayList<>();
        for (IRequirement requirement : requirements) {
            if (!requirement.check(this)) {
                success = false;
                commands.addAll(requirement.denyCommands());
                break;
            }
        }
        if (!success && denyCommands != null) {
            commands.addAll(denyCommands);
        }
        if (!commands.isEmpty()) {
            executeCommands(commands, r);
        }
        return success;
    }

    /**
     * 使用预览该菜单的玩家作为上下文，执行指定操作
     * @param commands 操作列表
     * @param r 变量替换列表
     */
    public void executeCommands(@NotNull List<IAction> commands, @Nullable ListPair<String, Object> r) {
        ActionProviders.run(plugin, player, commands, r);
    }

    /**
     * 创建菜单实例
     * @param config 菜单配置
     * @param player 要打开菜单的玩家
     */
    @NotNull
    public static MenuInstance create(MenuConfig config, Player player) {
        return new MenuInstance(config, player);
    }

    /**
     * 创建菜单实例
     * @param config 菜单配置
     * @param player 要打开菜单的玩家
     * @param args 命令参数
     * @return 当命令参数解析有误时，返回 <code>null</code>
     */
    @Nullable
    public static MenuInstance create(MenuConfig config, Player player, String[] args) {
        MenuArguments arguments = config.menuArguments();
        if (arguments.isEmpty()) {
            return create(config, player);
        }
        Map<String, Object> variables = arguments.parseVariables(player, args);
        if (variables == null) return null;
        MenuInstance inst = create(config, player);
        inst.putVariables(variables);
        return inst;
    }

    @Nullable
    public static MenuInstance get(Player player) {
        if (player == null) return null;
        IGuiHolder menu = GuiManager.inst().getOpeningGui(player);
        if (menu instanceof MenuInstance) {
            return (MenuInstance) menu;
        }
        return null;
    }
}
