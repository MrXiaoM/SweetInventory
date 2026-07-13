package top.mrxiaom.sweet.inventory.impl.material;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.func.AutoRegister;
import top.mrxiaom.pluginbase.utils.Util;
import top.mrxiaom.sweet.inventory.SweetInventory;
import top.mrxiaom.sweet.inventory.api.IMaterialProvider;
import top.mrxiaom.sweet.inventory.func.AbstractModule;
import top.mrxiaom.sweet.inventory.func.menus.MenuIcon;

@AutoRegister(priority = 990)
public class EquipmentSlotMaterial extends AbstractModule implements IMaterialProvider {
    public EquipmentSlotMaterial(SweetInventory plugin) {
        super(plugin);
        plugin.getMaterialRegistry().register(this);
    }

    @Override
    public @Nullable ItemStack parse(Player player, MenuIcon icon) {
        String material = icon.material();
        if (material.startsWith("equipment-slot:")) {
            return parse(player, material.substring(15));
        }
        if (material.startsWith("slot:")) {
            return parse(player, material.substring(5));
        }
        return null;
    }

    private ItemStack parse(Player player, String input) {
        EquipmentSlot slot = Util.valueOrNull(EquipmentSlot.class, input);
        if (slot != null) {
            EntityEquipment equipment = player.getEquipment();
            ItemStack item = equipment == null ? null : equipment.getItem(slot);
            if (item == null) {
                return new ItemStack(Material.AIR);
            }
            return item;
        }
        return null;
    }
}
