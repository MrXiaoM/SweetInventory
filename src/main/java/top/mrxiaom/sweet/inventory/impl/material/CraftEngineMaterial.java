package top.mrxiaom.sweet.inventory.impl.material;

import net.momirealms.craftengine.bukkit.api.CraftEngineItems;
import net.momirealms.craftengine.bukkit.item.BukkitItemDefinition;
import net.momirealms.craftengine.core.util.Key;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import top.mrxiaom.pluginbase.func.AutoRegister;
import top.mrxiaom.pluginbase.utils.Util;
import top.mrxiaom.sweet.inventory.SweetInventory;
import top.mrxiaom.sweet.inventory.api.IMaterialProvider;
import top.mrxiaom.sweet.inventory.func.AbstractModule;
import top.mrxiaom.sweet.inventory.func.menus.MenuIcon;

@AutoRegister(priority = 990, requirePlugins = {"CraftEngine"})
public class CraftEngineMaterial extends AbstractModule implements IMaterialProvider {
    public CraftEngineMaterial(SweetInventory plugin) {
        super(plugin);
        if (Util.isPresent("net.momirealms.craftengine.bukkit.item.BukkitItemDefinition")) {
            plugin.getMaterialRegistry().register(this);
        } else {
            warn("CraftEngine 版本过低，请升级到 26.5 或以上");
        }
    }

    @Override
    public ItemStack parse(Player player, MenuIcon icon) {
        String material = icon.material();
        if (material.startsWith("craftengine:")) {
            Key id = Key.of(material.substring(12));
            BukkitItemDefinition item = CraftEngineItems.byId(id);
            if (item != null) {
                return item.buildBukkitItem(player);
            }
            return null;
        }
        return null;
    }
}
