package top.mrxiaom.sweet.inventory.impl.mythic;

import io.lumine.xikage.mythicmobs.MythicMobs;
import io.lumine.xikage.mythicmobs.adapters.bukkit.BukkitAdapter;
import io.lumine.xikage.mythicmobs.items.MythicItem;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class MythicItemV4 implements IMythicItem {
    private final MythicMobs mythic = MythicMobs.inst();

    @Override
    public @Nullable ItemStack getItem(@NonNull String type) {
        MythicItem mythicItem = mythic.getItemManager().getItem(type).orElse(null);
        return mythicItem == null ? null : BukkitAdapter.adapt(mythicItem.generateItemStack(1));
    }
}
