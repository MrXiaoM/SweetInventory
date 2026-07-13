package top.mrxiaom.sweet.inventory.api;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import top.mrxiaom.pluginbase.utils.Pair;

import java.util.List;

public class ItemMatchContext {
    private final @NotNull Player player;
    private final @NotNull ItemStack item;
    private final @NotNull List<Pair<String, Object>> r;

    public ItemMatchContext(@NotNull Player player, @NotNull ItemStack item, @NotNull List<Pair<String, Object>> r) {
        this.player = player;
        this.item = item;
        this.r = r;
    }

    @NotNull
    public Player player() {
        return player;
    }

    @NotNull
    public ItemStack item() {
        return item;
    }

    @NotNull
    public List<Pair<String, Object>> r() {
        return r;
    }

    @NotNull
    public List<Pair<String, Object>> replacements() {
        return r;
    }
}
