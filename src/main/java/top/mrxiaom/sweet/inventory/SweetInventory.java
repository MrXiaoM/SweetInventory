package top.mrxiaom.sweet.inventory;
        
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


    @Override
    protected void afterEnable() {
        getLogger().info("SweetInventory 加载完毕");
    }
}
