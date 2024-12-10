package top.mrxiaom.sweet.inventory.utils;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public interface InventoryFactory {
    Inventory create(InventoryHolder holder, int size, String title);
}
