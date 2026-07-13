package top.mrxiaom.sweet.inventory.api;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.api.WithPriority;

public interface ItemMatcher {
    boolean isItemMatch(@NotNull ItemMatchContext ctx);

    interface Provider extends WithPriority {
        @Nullable ItemMatcher parse(@NotNull ConfigurationSection config);
    }
}
