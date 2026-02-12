package top.mrxiaom.sweet.inventory.impl.material;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.jspecify.annotations.Nullable;
import top.mrxiaom.pluginbase.func.AutoRegister;
import top.mrxiaom.pluginbase.utils.SkullsUtil;
import top.mrxiaom.sweet.inventory.SweetInventory;
import top.mrxiaom.sweet.inventory.api.IMaterialProvider;
import top.mrxiaom.sweet.inventory.func.AbstractModule;
import top.mrxiaom.sweet.inventory.func.menus.MenuIcon;

@AutoRegister(priority = 990)
public class Base64HeadMaterial extends AbstractModule implements IMaterialProvider {
    public Base64HeadMaterial(SweetInventory plugin) {
        super(plugin);
        plugin.registerMaterial(this);
    }

    @Override
    public @Nullable ItemStack parse(Player player, MenuIcon icon) {
        String material = icon.material();
        if (material.startsWith("head:")) {
            ItemStack item = SkullsUtil.createHeadItem();
            ItemMeta meta = item.getItemMeta();
            if (meta instanceof SkullMeta) {
                String base64 = material.substring(5);
                SkullsUtil.Skull skull = SkullsUtil.getOrCreateSkull(base64);
                if (skull != null) {
                    skull.setSkull((SkullMeta) meta);
                    item.setItemMeta(meta);
                }
            }
            return item;
        }
        return null;
    }
}
