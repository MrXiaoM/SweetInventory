package top.mrxiaom.sweet.inventory.func.menus.arguments;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.sweet.inventory.func.menus.arguments.range.IArgumentRange;

public interface IArgumentType {
    @NotNull String getTypeName();
    @Nullable Object parseValue(@Nullable Player player, @NotNull String input);
    @Nullable IArgumentRange parseRange(@NotNull String input);

}
