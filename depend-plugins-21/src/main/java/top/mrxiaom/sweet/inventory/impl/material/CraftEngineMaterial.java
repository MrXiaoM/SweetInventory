package top.mrxiaom.sweet.inventory.impl.material;

import net.momirealms.craftengine.bukkit.api.BukkitAdaptors;
import net.momirealms.craftengine.bukkit.api.CraftEngineItems;
import net.momirealms.craftengine.core.item.CustomItem;
import net.momirealms.craftengine.core.util.Key;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import top.mrxiaom.pluginbase.func.AutoRegister;
import top.mrxiaom.sweet.inventory.SweetInventory;
import top.mrxiaom.sweet.inventory.api.IMaterialProvider;
import top.mrxiaom.sweet.inventory.func.AbstractModule;
import top.mrxiaom.sweet.inventory.func.menus.MenuIcon;

@AutoRegister(priority = 990, requirePlugins = {"CraftEngine"})
public class CraftEngineMaterial extends AbstractModule implements IMaterialProvider {
    public CraftEngineMaterial(SweetInventory plugin) {
        super(plugin);
        plugin.registerMaterial(this);
    }

    @Override
    public ItemStack parse(Player player, MenuIcon icon) {
        String material = icon.material();
        if (material.startsWith("craftengine:")) {
            Key id = Key.of(material.substring(12));
            CustomItem<ItemStack> item = CraftEngineItems.byId(id);
            if (item != null) {
                return item.buildItemStack(BukkitAdaptors.adapt(player));
            }
            return null;
        }
        return null;
    }
}
