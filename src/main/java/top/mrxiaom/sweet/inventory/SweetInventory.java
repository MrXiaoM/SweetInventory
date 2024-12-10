package top.mrxiaom.sweet.inventory;
        
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import top.mrxiaom.pluginbase.BukkitPlugin;
import top.mrxiaom.pluginbase.EconomyHolder;

public class SweetInventory extends BukkitPlugin {
    public static SweetInventory getInstance() {
        return (SweetInventory) BukkitPlugin.getInstance();
    }

    public SweetInventory() {
        super(options()
                .bungee(true)
                .adventure(true)
                .database(false)
                .reconnectDatabaseWhenReloadConfig(false)
                .vaultEconomy(true)
                .scanIgnore("top.mrxiaom.sweet.inventory.libs")
        );
    }
    @NotNull
    public EconomyHolder getEconomy() {
        return options.economy();
    }

    @SuppressWarnings({"all"})
    public void connect(Player player, String server) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("Connect");
        out.writeUTF(server);

        player.sendPluginMessage(this, "BungeeCord", out.toByteArray());
    }

    @Override
    protected void afterEnable() {
        getLogger().info("SweetInventory 加载完毕");
    }
}
