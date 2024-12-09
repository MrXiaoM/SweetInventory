package top.mrxiaom.sweet.inventory.func.menus;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

public class MenuHolder implements InventoryHolder {
    private final MenuInstance instance;
    private Inventory inventory;

    public MenuHolder(MenuInstance instance) {
        this.instance = instance;
    }


    @NotNull
    public MenuInstance getInstance() {
        return instance;
    }

    protected Inventory setInventory(@NotNull Inventory inventory) {
        this.inventory = inventory;
        return inventory;
    }

    @NotNull
    @Override
    public Inventory getInventory() {
        return inventory;
    }
}
