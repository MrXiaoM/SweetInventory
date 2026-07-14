package top.mrxiaom.sweet.inventory.impl.mythic;

import io.lumine.xikage.mythicmobs.MythicMobs;
import io.lumine.xikage.mythicmobs.adapters.bukkit.BukkitAdapter;
import io.lumine.xikage.mythicmobs.items.MythicItem;
import io.lumine.xikage.mythicmobs.util.jnbt.CompoundTag;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MythicItemV4 implements IMythicItem {
    private final MythicMobs mythic = MythicMobs.inst();

    @Override
    public @Nullable ItemStack getItem(@NotNull String type) {
        MythicItem mythicItem = mythic.getItemManager().getItem(type).orElse(null);
        return mythicItem == null ? null : BukkitAdapter.adapt(mythicItem.generateItemStack(1));
    }

    @Nullable
    @Override
    public String getId(@NotNull ItemStack item) {
        CompoundTag nbt = mythic.getVolatileCodeHandler().getItemHandler().getNBTData(item);
        if (nbt.containsKey("MYTHIC_TYPE")) {
            return nbt.getString("MYTHIC_TYPE");
        } else {
            return null;
        }
    }
}
