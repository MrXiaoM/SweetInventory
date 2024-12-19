package top.mrxiaom.sweet.inventory.func.menus;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
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
import top.mrxiaom.pluginbase.func.AbstractGuiModule;
import top.mrxiaom.pluginbase.func.gui.actions.IAction;
import top.mrxiaom.pluginbase.gui.IGui;
import top.mrxiaom.pluginbase.utils.AdventureUtil;
import top.mrxiaom.pluginbase.utils.PAPI;
import top.mrxiaom.pluginbase.utils.Pair;
import top.mrxiaom.sweet.inventory.SweetInventory;
import top.mrxiaom.sweet.inventory.func.Menus;
import top.mrxiaom.sweet.inventory.requirements.IRequirement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public class MenuInstance implements IGui {
    private final SweetInventory plugin = SweetInventory.getInstance();
    private final MenuConfig config;
    private final Player player;
    private int updateCounter = 0;
    private Map<Integer, MenuIcon> currentIcons = new HashMap<>();
    private Component title;
    protected MenuInstance(MenuConfig config, Player player) {
        this.config = config;
        this.player = player;
    }

    public void onTick() {
        if (config.updateInterval == 0) return;
        if (++updateCounter == config.updateInterval) {
            updateCounter = 0;
            // 更新需要更新的图标物品内容
            Map<Integer, ItemStack> commits = new HashMap<>();
            for (Map.Entry<Integer, MenuIcon> entry : currentIcons.entrySet()) {
                MenuIcon icon = entry.getValue();
                if (!icon.isNeedsUpdate()) continue;
                ItemStack item = icon.getIcon().generateIcon(player);
                commits.put(entry.getKey(), item);
            }
            if (!commits.isEmpty()) {
                InventoryView inv = player.getOpenInventory();
                InventoryHolder holder = inv.getTopInventory().getHolder();
                // 确保玩家打开的还是这个界面再更新
                if (holder instanceof MenuHolder && ((MenuHolder) holder).getInstance() == this) {
                    for (Map.Entry<Integer, ItemStack> entry : commits.entrySet()) {
                        inv.setItem(entry.getKey(), entry.getValue());
                    }
                    player.updateInventory();
                }
            }
        }
    }

    public MenuConfig getConfig() {
        return config;
    }

    public Component getTitle() {
        return title;
    }

    @Override
    public Player getPlayer() {
        return player;
    }

    public void updateInventory(BiConsumer<Integer, ItemStack> setItem) {
        currentIcons.clear();
        Map<Character, Integer> appearMap = new HashMap<>();
        for (int i = 0; i < config.inventory.length; i++) {
            char id = config.inventory[i];
            if (id == ' ' || id == '　' || Character.isSpaceChar(id)) { // 忽略空格
                setItem.accept(i, null);
                continue;
            }
            int appearTimes = appearMap.getOrDefault(id, 0) + 1;
            appearMap.put(id, appearTimes);
            List<MenuIcon> list = config.iconsByChar.get(id); // list 已经过优先级排序
            if (list != null && !list.isEmpty()) for (MenuIcon icon : list) {
                // 满足条件时，释放图标到界面
                if (checkRequirements(icon)) {
                    ItemStack item = icon.getIcon().generateIcon(player);
                    setItem.accept(i, item);
                    currentIcons.put(i, icon);
                    break;
                }
            }
            setItem.accept(i, null);
        }
    }

    public void updateInventory(Inventory inv) {
        updateInventory(inv::setItem);
    }

    public void updateInventory(InventoryView view) {
        updateInventory(view::setItem);
        player.updateInventory();
    }

    @Override
    public Inventory newInventory() {
        String rawTitle = PAPI.setPlaceholders(player, config.title);
        title = AdventureUtil.miniMessage(rawTitle);
        MenuHolder holder = new MenuHolder(this);
        Inventory inv = holder.setInventory(plugin.getInventoryFactory().create(holder, config.inventory.length, rawTitle));
        updateInventory(inv);
        return inv;
    }

    @Override
    public void onClick(InventoryAction action, ClickType click, InventoryType.SlotType slotType,
                        int slot, ItemStack currentItem, ItemStack cursor,
                        InventoryView view, InventoryClickEvent event) {
        event.setCancelled(true);
        MenuIcon icon = currentIcons.get(slot);
        // 点击操作
        if (icon != null) switch (click) {
            case LEFT:
                handleIconClick(icon.getLeftClick());
                break;
            case RIGHT:
                handleIconClick(icon.getRightClick());
                break;
            case SHIFT_LEFT:
                handleIconClick(icon.getShiftLeftClick());
                break;
            case SHIFT_RIGHT:
                handleIconClick(icon.getShiftRightClick());
                break;
            case DROP:
                handleIconClick(icon.getDropClick());
                break;
            case CONTROL_DROP:
                handleIconClick(icon.getCtrlDropClick());
                break;
        }
    }

    private void handleIconClick(Click click) {
        if (checkRequirements(click.requirements, click.denyCommands)) {
            executeCommands(click.commands);
        }
    }

    public Character getClickedId(int slot) {
        return AbstractGuiModule.getClickedId(config.inventory, slot);
    }

    public int getAppearTimes(Character id, int slot) {
        return AbstractGuiModule.getAppearTimes(config.inventory, id, slot);
    }

    public boolean checkRequirements(MenuIcon icon) {
        return checkRequirements(icon.viewRequirements, icon.viewDenyCommands);
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
        Pair<String, Object>[] args = Pair.array(0);
        for (IAction action : commands) {
            action.run(player, args);
        }
    }

    private void executeConsole(String cmd) {
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
    }
    private void executePlayer(String cmd) {
        Bukkit.dispatchCommand(getPlayer(), cmd);
    }
    private void executeMessage(String msg) {
        AdventureUtil.sendMessage(getPlayer(), msg);
    }
    private void executeActionBar(String msg) {
        AdventureUtil.sendActionBar(getPlayer(), msg);
    }
    private void executeConnect(String server) {
        plugin.connect(getPlayer(), server);
    }
    private void executeOpen(String menuId) {
        MenuConfig menu = Menus.inst().getMenu(menuId);
        if (menu != null) {
            menu.create(getPlayer()).open();
        }
    }

    public static MenuInstance create(MenuConfig config, Player player) {
        return new MenuInstance(config, player);
    }
}
