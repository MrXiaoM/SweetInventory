package top.mrxiaom.sweet.inventory.func.menus;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.func.AbstractGuiModule;
import top.mrxiaom.pluginbase.gui.IGui;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public class MenuInstance implements IGui {
    private final MenuConfig config;
    private final Player player;
    private int updateCounter = 0;
    protected MenuInstance(MenuConfig config, Player player) {
        this.config = config;
        this.player = player;
    }

    public void onTick() {
        if (config.updateInterval == 0) return;
        if (++updateCounter == config.updateInterval) {
            updateCounter = 0;
            // TODO: 更新需要更新的物品
        }
    }

    public MenuConfig getConfig() {
        return config;
    }

    @Override
    public Player getPlayer() {
        return player;
    }

    public void updateInventory(BiConsumer<Integer, ItemStack> setItem) {
        Map<Character, Integer> appearMap = new HashMap<>();
        for (int i = 0; i < config.inventory.length; i++) {
            char id = config.inventory[i];
            if (id == ' ' || id == '　' || Character.isSpaceChar(id)) { // 忽略空格
                setItem.accept(i, null);
                continue;
            }
            int appearTimes = appearMap.getOrDefault(id, 0) + 1;
            appearMap.put(id, appearTimes);
            List<MenuIcon> list = config.iconsByChar.get(id);
            if (list != null && !list.isEmpty()) {
                // TODO: 释放图标到界面
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
        MenuHolder holder = new MenuHolder(this);
        Inventory inv = holder.setInventory(Bukkit.createInventory(holder, config.inventory.length, config.title));
        updateInventory(inv);
        return inv;
    }

    @Override
    public void onClick(InventoryAction action, ClickType click, InventoryType.SlotType slotType,
                        int slot, ItemStack currentItem, ItemStack cursor,
                        InventoryView view, InventoryClickEvent event) {
        Character id = getClickedId(slot);
        List<MenuIcon> list = id == null ? null : config.iconsByChar.get(id);
        if (list != null && !list.isEmpty()) {
            // TODO: 点击操作
        }
    }

    public Character getClickedId(int slot) {
        return AbstractGuiModule.getClickedId(config.inventory, slot);
    }

    public int getAppearTimes(Character id, int slot) {
        return AbstractGuiModule.getAppearTimes(config.inventory, id, slot);
    }

    public static MenuInstance create(MenuConfig config, Player player) {
        return new MenuInstance(config, player);
    }
}
