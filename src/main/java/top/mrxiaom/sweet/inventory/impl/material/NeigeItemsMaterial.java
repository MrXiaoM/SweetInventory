package top.mrxiaom.sweet.inventory.impl.material;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.Nullable;
import pers.neige.neigeitems.manager.ItemManager;
import top.mrxiaom.pluginbase.func.AutoRegister;
import top.mrxiaom.sweet.inventory.SweetInventory;
import top.mrxiaom.sweet.inventory.api.IMaterialProvider;
import top.mrxiaom.sweet.inventory.func.AbstractModule;
import top.mrxiaom.sweet.inventory.func.menus.MenuIcon;

@AutoRegister(priority = 990, requirePlugins = {"NeigeItems"})
public class NeigeItemsMaterial extends AbstractModule implements IMaterialProvider {
    public NeigeItemsMaterial(SweetInventory plugin) {
        super(plugin);
        plugin.registerMaterial(this);
    }

    @Override
    public @Nullable ItemStack parse(Player player, MenuIcon icon) {
        String material = icon.material();
        if (material.startsWith("neigeitems:")) {
            String argument = material.substring(11);
            if (argument.contains(";")) {
                String[] split = argument.split(";", 2);
                return ItemManager.INSTANCE.getItemStack(split[0], player, split[1]);
            } else {
                return ItemManager.INSTANCE.getItemStack(argument, player);
            }
        }
        return null;
    }
}
