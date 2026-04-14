package top.mrxiaom.sweet.inventory.impl.material;

import github.saukiya.sxitem.SXItem;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.Nullable;
import top.mrxiaom.pluginbase.func.AutoRegister;
import top.mrxiaom.sweet.inventory.SweetInventory;
import top.mrxiaom.sweet.inventory.api.IMaterialProvider;
import top.mrxiaom.sweet.inventory.func.AbstractModule;
import top.mrxiaom.sweet.inventory.func.menus.MenuIcon;

@AutoRegister(priority = 990, requirePlugins = {"SX-Item"})
public class SXItemMaterial extends AbstractModule implements IMaterialProvider {
    public SXItemMaterial(SweetInventory plugin) {
        super(plugin);
        plugin.getMaterialRegistry().register(this);
    }

    @Override
    public @Nullable ItemStack parse(Player player, MenuIcon icon) {
        String material = icon.material();
        if (material.startsWith("sxitem:")) {
            String itemId = material.substring(7);
            return SXItem.getItemManager().getItem(itemId, player);
        }
        if (material.startsWith("sx-item:")) {
            String itemId = material.substring(8);
            return SXItem.getItemManager().getItem(itemId, player);
        }
        return null;
    }
}
