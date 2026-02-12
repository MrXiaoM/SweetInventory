package top.mrxiaom.sweet.inventory.impl.material;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.Nullable;
import top.mrxiaom.pluginbase.func.AutoRegister;
import top.mrxiaom.pluginbase.utils.depend.IA;
import top.mrxiaom.sweet.inventory.SweetInventory;
import top.mrxiaom.sweet.inventory.api.IMaterialProvider;
import top.mrxiaom.sweet.inventory.func.AbstractModule;
import top.mrxiaom.sweet.inventory.func.menus.MenuIcon;

@AutoRegister(priority = 990, requirePlugins = {"ItemsAdder"})
public class ItemsAdderMaterial extends AbstractModule implements IMaterialProvider {
    public ItemsAdderMaterial(SweetInventory plugin) {
        super(plugin);
        plugin.registerMaterial(this);
    }

    @Override
    public @Nullable ItemStack parse(Player player, MenuIcon icon) {
        String material = icon.material();
        if (material.startsWith("itemsadder:")) {
            return IA.get(material.substring(11)).orElse(null);
        }
        return null;
    }
}
