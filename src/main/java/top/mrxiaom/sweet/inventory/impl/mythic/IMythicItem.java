package top.mrxiaom.sweet.inventory.impl.mythic;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface IMythicItem {
    @Nullable ItemStack getItem(@NotNull String type);
}
