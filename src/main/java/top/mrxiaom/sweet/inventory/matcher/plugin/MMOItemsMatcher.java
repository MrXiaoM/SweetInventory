package top.mrxiaom.sweet.inventory.matcher.plugin;

import net.Indyuce.mmoitems.api.item.mmoitem.LiveMMOItem;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.func.AutoRegister;
import top.mrxiaom.pluginbase.utils.Pair;
import top.mrxiaom.sweet.inventory.SweetInventory;
import top.mrxiaom.sweet.inventory.api.ItemMatchContext;
import top.mrxiaom.sweet.inventory.api.ItemMatcher;
import top.mrxiaom.sweet.inventory.func.AbstractModule;
import top.mrxiaom.sweet.inventory.matcher.AbstractStringMatcher;
import top.mrxiaom.sweet.inventory.matcher.PluginsMatcher;

import java.util.ArrayList;
import java.util.List;

@AutoRegister(priority = 995, requirePlugins = "MMOItems")
public class MMOItemsMatcher extends AbstractModule implements ItemMatcher.Provider {
    public MMOItemsMatcher(SweetInventory plugin) {
        super(plugin);
        instanceOf(PluginsMatcher.class).pluginRegistry().register(this);
    }

    @Nullable
    @Override
    public ItemMatcher parse(@NotNull ConfigurationSection config) {
        Pair<String, AbstractStringMatcher.Type> pair = AbstractStringMatcher.Type.parse("mmoitems", config::contains);
        if (pair != null) {
            String path = pair.key();
            AbstractStringMatcher.Type type = pair.value();
            List<String> rawValues = new ArrayList<>();
            if (config.isList(path)) {
                rawValues.addAll(config.getStringList(path));
            } else {
                rawValues.add(config.getString(path, ""));
            }
            return new Impl(type, rawValues);
        }
        return null;
    }

    public class Impl extends AbstractStringMatcher {
        private final List<String> rawValues;
        protected Impl(Type type, List<String> rawValues) {
            super(type);
            this.rawValues = rawValues;
        }

        @Override
        public boolean isItemMatch(@NotNull ItemMatchContext ctx) {
            ItemStack item = ctx.item();
            if (item.getType().equals(Material.AIR) || item.getAmount() == 0) {
                return false;
            }
            LiveMMOItem mmoItem = new LiveMMOItem(item);
            if (mmoItem.getType() == null || mmoItem.getId() == null || mmoItem.getId().isEmpty()) {
                return false;
            }
            String itemId = mmoItem.getType().getId() + ":" + mmoItem.getId();
            for (String rawValue : rawValues) {
                String input = parseInputValue(ctx, rawValue);
                if (type.isMatch(input, itemId)) {
                    return true;
                }
            }
            return false;
        }
    }
}
