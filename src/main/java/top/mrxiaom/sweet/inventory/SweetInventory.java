package top.mrxiaom.sweet.inventory;
        
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import top.mrxiaom.pluginbase.BukkitPlugin;
import top.mrxiaom.pluginbase.EconomyHolder;
import top.mrxiaom.pluginbase.utils.Util;
import top.mrxiaom.sweet.inventory.utils.BukkitInventoryFactory;
import top.mrxiaom.sweet.inventory.utils.InventoryFactory;
import top.mrxiaom.sweet.inventory.utils.MiniMessageConvert;
import top.mrxiaom.sweet.inventory.utils.PaperInventoryFactory;

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
    private InventoryFactory inventoryFactory;
    @NotNull
    public EconomyHolder getEconomy() {
        return options.economy();
    }

    public InventoryFactory getInventoryFactory() {
        return inventoryFactory;
    }

    @SuppressWarnings({"all"})
    public void connect(Player player, String server) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("Connect");
        out.writeUTF(server);

        player.sendPluginMessage(this, "BungeeCord", out.toByteArray());
    }

    @Override
    protected void beforeEnable() {
        MiniMessageConvert.init();
        if (Util.isPresent("com.destroystokyo.paper.utils.PaperPluginLogger")) {
            inventoryFactory = new PaperInventoryFactory();
        } else {
            inventoryFactory = new BukkitInventoryFactory();
            if (Bukkit.getPluginManager().isPluginEnabled("ProtocolLib")) {

            }
        }
    }

    @Override
    protected void afterEnable() {
        getLogger().info("SweetInventory 加载完毕");
    }
}
