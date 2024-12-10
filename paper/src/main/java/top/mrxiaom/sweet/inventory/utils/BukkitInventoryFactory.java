package top.mrxiaom.sweet.inventory.utils;

import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import static top.mrxiaom.sweet.inventory.utils.MiniMessageConvert.miniMessageToLegacy;

public class BukkitInventoryFactory implements InventoryFactory {
    @Override
    public Inventory create(InventoryHolder owner, int size, String title) {
        return Bukkit.createInventory(owner, size, miniMessageToLegacy(title));
    }
}
