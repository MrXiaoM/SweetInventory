package top.mrxiaom.sweet.inventory.matcher;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.api.IRegistry;
import top.mrxiaom.pluginbase.data.SimpleRegistry;
import top.mrxiaom.pluginbase.func.AutoRegister;
import top.mrxiaom.sweet.inventory.SweetInventory;
import top.mrxiaom.sweet.inventory.api.ItemMatchContext;
import top.mrxiaom.sweet.inventory.api.ItemMatcher;
import top.mrxiaom.sweet.inventory.func.AbstractModule;

@AutoRegister(priority = 990)
public class PluginsMatcher extends AbstractModule implements ItemMatcher.Provider {
    private final IRegistry<ItemMatcher.Provider> pluginRegistry = new SimpleRegistry<>();
    public PluginsMatcher(SweetInventory plugin) {
        super(plugin);
        plugin.getItemMatcherRegistry().register(this);
    }

    public IRegistry<ItemMatcher.Provider> pluginRegistry() {
        return pluginRegistry;
    }

    @Override
    public @Nullable ItemMatcher parse(@NotNull ConfigurationSection config) {
        ConfigurationSection section = config.getConfigurationSection("plugins");
        if (section != null) {
            for (ItemMatcher.Provider provider : pluginRegistry.all()) {
                ItemMatcher itemMatcher = provider.parse(section);
                if (itemMatcher != null) {
                    return new Impl(itemMatcher);
                }
            }
        }
        return null;
    }

    public class Impl implements ItemMatcher {
        private final ItemMatcher children;
        public Impl(ItemMatcher children) {
            this.children = children;
        }

        @Override
        public boolean isItemMatch(@NotNull ItemMatchContext ctx) {
            return children.isItemMatch(ctx);
        }
    }
}
