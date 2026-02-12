package top.mrxiaom.sweet.inventory.impl.material;

import com.nexomc.nexo.api.NexoItems;
import com.nexomc.nexo.items.ItemBuilder;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import top.mrxiaom.pluginbase.func.AutoRegister;
import top.mrxiaom.sweet.inventory.SweetInventory;
import top.mrxiaom.sweet.inventory.api.IMaterialProvider;
import top.mrxiaom.sweet.inventory.func.AbstractModule;
import top.mrxiaom.sweet.inventory.func.menus.MenuIcon;

@AutoRegister(priority = 990, requirePlugins = {"Nexo"})
public class NexoMaterial extends AbstractModule implements IMaterialProvider {
    public NexoMaterial(SweetInventory plugin) {
        super(plugin);
        plugin.registerMaterial(this);
    }

    @Override
    public ItemStack parse(Player player, MenuIcon icon) {
        String material = icon.material();
        if (material.startsWith("nexo:")) {
            String itemId = material.substring(5);
            ItemBuilder itemBuilder = NexoItems.itemFromId(itemId);
            if (itemBuilder != null) {
                return itemBuilder.build();
            }
            return null;
        }
        return null;
    }
}
