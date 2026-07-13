package top.mrxiaom.sweet.inventory.matcher;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.func.AutoRegister;
import top.mrxiaom.sweet.inventory.SweetInventory;
import top.mrxiaom.sweet.inventory.api.ItemMatchContext;
import top.mrxiaom.sweet.inventory.api.ItemMatcher;
import top.mrxiaom.sweet.inventory.func.AbstractModule;

import java.util.ArrayList;
import java.util.List;

@AutoRegister(priority = 990)
public class PluginsMatcher extends AbstractModule implements ItemMatcher.Provider {
    public PluginsMatcher(SweetInventory plugin) {
        super(plugin);
        plugin.getItemMatcherRegistry().register(this);
    }

    @Override
    public @Nullable ItemMatcher parse(@NotNull ConfigurationSection config) {
        ConfigurationSection section = config.getConfigurationSection("plugins");
        if (section != null) {
            List<ItemMatcher> children = new ArrayList<>();
            // TODO: 实现第三方插件支持
            return new Impl(children);
        }
        return null;
    }

    public class Impl implements ItemMatcher {
        private final List<ItemMatcher> children;
        public Impl(List<ItemMatcher> children) {
            this.children = children;
        }

        @Override
        public boolean isItemMatch(@NotNull ItemMatchContext ctx) {
            // 实现与 AnyMatcher 一致
            if (children.isEmpty()) return true;
            for (ItemMatcher child : children) {
                if (child.isItemMatch(ctx)) {
                    return true;
                }
            }
            return false;
        }
    }
}
