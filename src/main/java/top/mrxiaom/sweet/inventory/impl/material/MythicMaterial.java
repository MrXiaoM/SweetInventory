package top.mrxiaom.sweet.inventory.impl.material;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.Nullable;
import top.mrxiaom.pluginbase.func.AutoRegister;
import top.mrxiaom.pluginbase.utils.Util;
import top.mrxiaom.sweet.inventory.SweetInventory;
import top.mrxiaom.sweet.inventory.api.IMaterialProvider;
import top.mrxiaom.sweet.inventory.func.AbstractModule;
import top.mrxiaom.sweet.inventory.func.menus.MenuIcon;
import top.mrxiaom.sweet.inventory.impl.mythic.IMythicItem;
import top.mrxiaom.sweet.inventory.impl.mythic.MythicItemV4;
import top.mrxiaom.sweet.inventory.impl.mythic.MythicItemV5;

@AutoRegister(priority = 990, requirePlugins = {"MythicMobs"})
public class MythicMaterial extends AbstractModule implements IMaterialProvider {
    private final IMythicItem mythicItem;
    public MythicMaterial(SweetInventory plugin) {
        super(plugin);
        if (Util.isPresent("io.lumine.xikage.mythicmobs.MythicMobs")) {
            mythicItem = new MythicItemV4();
        } else if (Util.isPresent("io.lumine.mythic.bukkit.MythicBukkit")) {
            mythicItem = new MythicItemV5();
        } else {
            mythicItem = null;
        }
        if (mythicItem != null) {
            plugin.registerMaterial(this);
            info("已挂钩 MythicMobs 材料");
        }
    }

    @Override
    public @Nullable ItemStack parse(Player player, MenuIcon icon) {
        String material = icon.material();
        if (material.startsWith("mythic:")) {
            return mythicItem.getItem(material.substring(7));
        }
        return null;
    }
}
