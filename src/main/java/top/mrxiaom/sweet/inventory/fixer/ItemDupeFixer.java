package top.mrxiaom.sweet.inventory.fixer;

import de.tr7zw.changeme.nbtapi.NBT;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDropItemEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Contract;
import top.mrxiaom.pluginbase.func.AutoRegister;
import top.mrxiaom.sweet.inventory.SweetInventory;
import top.mrxiaom.sweet.inventory.func.AbstractModule;
import top.mrxiaom.sweet.inventory.func.menus.MenuInstance;

/**
 * 阻止玩家使用菜单图标物品
 */
@AutoRegister
public class ItemDupeFixer extends AbstractModule implements Listener {
    public static final String TAG = "SweetInventoryIcon";
    public ItemDupeFixer(SweetInventory plugin) {
        super(plugin);
        registerEvents();
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        for (ItemStack item : e.getPlayer().getInventory()) {
            if (isMarked(item)) {
                removeItem(item);
            }
        }
    }

    @EventHandler
    public void onDropItem(EntityDropItemEvent e) {
        if (isOpenedMenu(e.getEntity())) return;
        Item item = e.getItemDrop();
        ItemStack itemStack = item.getItemStack();
        if (isMarked(itemStack)) {
            removeItem(itemStack);
            item.remove();
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onDropItem(PlayerDropItemEvent e) {
        if (isOpenedMenu(e.getPlayer())) return;
        Item item = e.getItemDrop();
        ItemStack itemStack = item.getItemStack();
        if (isMarked(itemStack)) {
            removeItem(itemStack);
            item.remove();
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onPickupItem(EntityPickupItemEvent e) {
        Item item = e.getItem();
        ItemStack itemStack = item.getItemStack();
        if (isMarked(itemStack)) {
            removeItem(itemStack);
            item.remove();
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (isOpenedMenu(e.getWhoClicked())) return;
        ItemStack item = e.getCurrentItem();
        if (isMarked(item)) {
            removeItem(item);
            e.setCurrentItem(null);
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if (isOpenedMenu(e.getPlayer())) return;
        ItemStack item = e.getItem();
        if (isMarked(item)) {
            removeItem(item);
            e.setCancelled(true);
        }
    }

    private static void removeItem(ItemStack item) {
        item.setAmount(0);
        item.setType(Material.AIR);
    }

    public static boolean isOpenedMenu(Entity entity) {
        if (entity instanceof Player) {
            return MenuInstance.get((Player) entity) != null;
        }
        return false;
    }

    @Contract("null -> false")
    public static boolean isMarked(ItemStack item) {
        if (item == null || item.getType().equals(Material.AIR)) return false;
        return NBT.get(item, nbt -> {
            return nbt.hasTag(TAG);
        });
    }
}
