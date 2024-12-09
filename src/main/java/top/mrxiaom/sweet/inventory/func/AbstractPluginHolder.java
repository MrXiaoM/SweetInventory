package top.mrxiaom.sweet.inventory.func;
        
import top.mrxiaom.sweet.inventory.SweetInventory;

@SuppressWarnings({"unused"})
public abstract class AbstractPluginHolder extends top.mrxiaom.pluginbase.func.AbstractPluginHolder<SweetInventory> {
    public AbstractPluginHolder(SweetInventory plugin) {
        super(plugin);
    }

    public AbstractPluginHolder(SweetInventory plugin, boolean register) {
        super(plugin, register);
    }
}
