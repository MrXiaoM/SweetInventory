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
    private int updateCounter = 0;
    private Map<Integer, MenuIcon> currentIcons = new HashMap<>();
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

    public MenuConfig config() {
        return config;
    }

    public Component title() {
        return title;
    }

    public SweetInventory plugin() {
        return plugin;
    }

    @Override
    public Player getPlayer() {
        return player;
    }

    public boolean hasPrevPage() {
        MenuPageGuide pageGuide = config.pageGuide();
        return pageGuide != null && pageGuide.hasPrevPage(page);
    }

    public boolean hasNextPage() {
        MenuPageGuide pageGuide = config.pageGuide();
        return pageGuide != null && pageGuide.hasNextPage(page);
    }

    public int page() {
        return page;
    }

    public void page(int page) {
        this.page = page;
    }

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

    public void updateInventory(BiConsumer<Integer, ItemStack> setItem) {
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
                    if (checkRequirements(icon)) {
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

    public void updateInventory(Inventory inv) {
        updateInventory(inv::setItem);
    }

    public void updateInventory(InventoryView view) {
        updateInventory(view::setItem);
        player.updateInventory();
    }

    public void updateInventory() {
        updateInventory(inventory::setItem);
    }

    public ListPair<String, Object> newReplacements() {
        ListPair<String, Object> r = new ListPair<>();
        MenuPageGuide pageGuide = config.pageGuide();
        r.add("%page%", page);
        r.add("%max_page%", pageGuide == null ? 1 : pageGuide.pages().size());
        return r;
    }

    @Override
    public Inventory newInventory() {
        String rawTitle = PAPI.setPlaceholders(player, Pair.replace(config.title(), newReplacements()));
        title = AdventureUtil.miniMessage(rawTitle);
        inventory = plugin.createInventory(this, config.inventory().length, rawTitle);
        updateInventory(inventory);
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

    private void handleIconClick(Click click) {
        if (click == null) {
            actionLock = false;
            return;
        }
        plugin.getScheduler().runTask(() -> {
            if (checkRequirements(click.requirements, click.denyCommands)) {
                executeCommands(click.commands);
            }
            actionLock = false;
        });
    }

    public Character getClickedId(int slot) {
        if (slot >= 0 && slot < inventoryTemplate.length) {
            return inventoryTemplate[slot];
        } else {
            return null;
        }
    }

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

    public boolean checkRequirements(MenuIcon icon) {
        return checkRequirements(icon.viewRequirements(), icon.viewDenyCommands());
    }

    public boolean checkRequirements(List<IRequirement> requirements, @Nullable List<IAction> denyCommands) {
        boolean success = true;
        List<IAction> commands = new ArrayList<>();
        for (IRequirement requirement : requirements) {
            if (!requirement.check(this)) {
                success = false;
                commands.addAll(requirement.getDenyCommands());
                break;
            }
        }
        if (!success && denyCommands != null) {
            commands.addAll(denyCommands);
        }
        if (!commands.isEmpty()) {
            executeCommands(commands);
        }
        return success;
    }

    public void executeCommands(List<IAction> commands) {
        ActionProviders.run(plugin, player, commands);
    }

    public static MenuInstance create(MenuConfig config, Player player) {
        return new MenuInstance(config, player);
    }
}
