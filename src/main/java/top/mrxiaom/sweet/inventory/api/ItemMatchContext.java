package top.mrxiaom.sweet.inventory.api;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;
import top.mrxiaom.pluginbase.utils.Pair;

import java.util.List;

public class ItemMatchContext {
    private final @NotNull Player player;
    private final @NotNull ItemStack item;
    private final @Range(from=1, to=99) int amount;
    private final @NotNull List<Pair<String, Object>> r;
    private final boolean debug;

    public ItemMatchContext(@NotNull Player player, @NotNull ItemStack item, int amount, @NotNull List<Pair<String, Object>> r) {
        this(player, item, amount, r, false);
    }

    public ItemMatchContext(@NotNull Player player, @NotNull ItemStack item, int amount, @NotNull List<Pair<String, Object>> r, boolean debug) {
        this.player = player;
        this.item = item;
        this.amount = amount;
        this.r = r;
        this.debug = debug;
    }

    @NotNull
    public Player player() {
        return player;
    }

    @NotNull
    public ItemStack item() {
        return item;
    }

    @Range(from=1, to=99)
    public int amount() {
        return amount;
    }

    @NotNull
    public List<Pair<String, Object>> r() {
        return r;
    }

    @NotNull
    public List<Pair<String, Object>> replacements() {
        return r;
    }

    public boolean debug() {
        return debug;
    }

    @ApiStatus.Experimental
    public void send(String message) {
        player.sendMessage(message);
    }
}
