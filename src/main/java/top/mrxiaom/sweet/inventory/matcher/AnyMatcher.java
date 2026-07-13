package top.mrxiaom.sweet.inventory.matcher;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.func.AutoRegister;
import top.mrxiaom.pluginbase.utils.ConfigUtils;
import top.mrxiaom.sweet.inventory.SweetInventory;
import top.mrxiaom.sweet.inventory.api.ItemMatchContext;
import top.mrxiaom.sweet.inventory.api.ItemMatcher;
import top.mrxiaom.sweet.inventory.func.AbstractModule;

import java.util.ArrayList;
import java.util.List;

@AutoRegister(priority = 990)
public class AnyMatcher extends AbstractModule implements ItemMatcher.Provider {
    public AnyMatcher(SweetInventory plugin) {
        super(plugin);
        plugin.getItemMatcherRegistry().register(this);
    }

    @Override
    public @Nullable ItemMatcher parse(@NotNull ConfigurationSection config) {
        if (config.contains("any")) {
            List<ItemMatcher> children = new ArrayList<>();
            List<ConfigurationSection> list = ConfigUtils.getSectionList(config, "any");
            for (ConfigurationSection section : list) {
                ItemMatcher itemMatcher = plugin.parseItemMatcher(section);
                if (itemMatcher == null) {
                    warn("'any' matcher 中存在无效的匹配器配置");
                    continue;
                }
                children.add(itemMatcher);
            }
            return new Impl(children);
        }
        return null;
    }

    public static class Impl implements ItemMatcher {
        private final List<ItemMatcher> children;

        public Impl(List<ItemMatcher> children) {
            this.children = children;
        }

        @Override
        public boolean isItemMatch(@NotNull ItemMatchContext ctx) {
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
