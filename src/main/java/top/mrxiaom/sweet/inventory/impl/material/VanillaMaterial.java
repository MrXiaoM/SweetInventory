package top.mrxiaom.sweet.inventory.impl.material;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.Nullable;
import top.mrxiaom.pluginbase.func.AutoRegister;
import top.mrxiaom.pluginbase.utils.ItemStackUtil;
import top.mrxiaom.pluginbase.utils.Pair;
import top.mrxiaom.sweet.inventory.SweetInventory;
import top.mrxiaom.sweet.inventory.api.IMaterialProvider;
import top.mrxiaom.sweet.inventory.func.AbstractModule;
import top.mrxiaom.sweet.inventory.func.menus.MenuIcon;

@AutoRegister(priority = 990)
public class VanillaMaterial extends AbstractModule implements IMaterialProvider {
    public VanillaMaterial(SweetInventory plugin) {
        super(plugin);
        plugin.registerMaterial(this);
    }

    @Override
    public int providerPriority() {
        return 2000;
    }

    @Override
    public @Nullable ItemStack parse(Player player, MenuIcon icon) {
        Pair<Material, Integer> pair = ItemStackUtil.parseMaterial(icon.material());
        return pair == null ? null : ItemStackUtil.legacy(pair);
    }
}
