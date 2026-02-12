package top.mrxiaom.sweet.inventory.impl.material;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder;
import net.Indyuce.mmoitems.api.item.build.MMOItemBuilder;
import net.Indyuce.mmoitems.api.item.template.MMOItemTemplate;
import net.Indyuce.mmoitems.manager.TemplateManager;
import net.Indyuce.mmoitems.manager.TypeManager;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.Nullable;
import top.mrxiaom.pluginbase.func.AutoRegister;
import top.mrxiaom.sweet.inventory.SweetInventory;
import top.mrxiaom.sweet.inventory.api.IMaterialProvider;
import top.mrxiaom.sweet.inventory.func.AbstractModule;
import top.mrxiaom.sweet.inventory.func.menus.MenuIcon;

@AutoRegister(priority = 990, requirePlugins = {"MMOItems"})
public class MMOItemsMaterial extends AbstractModule implements IMaterialProvider {
    public MMOItemsMaterial(SweetInventory plugin) {
        super(plugin);
        plugin.registerMaterial(this);
    }

    @Override
    public @Nullable ItemStack parse(Player player, MenuIcon icon) {
        String material = icon.material();
        if (material.startsWith("mmoitems:")) {
            return build(player, material.substring(9), true);
        }
        if (material.startsWith("mmoitems-random:")) {
            return build(player, material.substring(16), false);
        }
        return null;
    }

    private ItemStack build(Player player, String input, boolean forDisplay) {
        String[] split = input.split(":", 2);
        if (split.length == 2) {
            TypeManager typeManager = MMOItems.plugin.getTypes();
            TemplateManager templateManager = MMOItems.plugin.getTemplates();
            Type type = typeManager.get(split[0]);
            if (type == null) return null;
            MMOItemTemplate template = templateManager.getTemplate(type, split[1]);
            if (template == null) return null;
            MMOItemBuilder itemBuilder = template.newBuilder(player);
            ItemStackBuilder itemStackBuilder = itemBuilder.build().newBuilder();
            return itemStackBuilder.build(forDisplay);
        }
        return null;
    }
}
