package top.mrxiaom.sweet.inventory.impl.mythic;

import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.items.MythicItem;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class MythicItemV5 implements IMythicItem {
    private final MythicBukkit mythic = MythicBukkit.inst();

    @Override
    public @Nullable ItemStack getItem(@NonNull String type) {
        MythicItem mythicItem = mythic.getItemManager().getItem(type).orElse(null);
        return mythicItem == null ? null : BukkitAdapter.adapt(mythicItem.generateItemStack(1));
    }
}
