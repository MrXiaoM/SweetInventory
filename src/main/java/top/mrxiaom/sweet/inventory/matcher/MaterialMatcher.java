package top.mrxiaom.sweet.inventory.matcher;

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

import java.util.ArrayList;
import java.util.List;

@AutoRegister(priority = 990)
public class MaterialMatcher extends AbstractModule implements ItemMatcher.Provider {
    public MaterialMatcher(SweetInventory plugin) {
        super(plugin);
        plugin.getItemMatcherRegistry().register(this);
    }

    @Override
    public @Nullable ItemMatcher parse(@NotNull ConfigurationSection config) {
        Pair<String, AbstractStringMatcher.Type> pair = AbstractStringMatcher.Type.parse("material", config::contains);
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

    public static class Impl extends AbstractStringMatcher {
        private final List<String> rawValues;
        public Impl(Type type, List<String> rawValues) {
            super(type);
            this.rawValues = rawValues;
        }

        @Override
        public boolean isItemMatch(@NotNull ItemMatchContext ctx) {
            ItemStack item = ctx.item();
            String name = item.getType().name();
            if (ctx.debug()) {
                ctx.send("\n正在进行 物品类型 的 " + type.debugName() + " 判定，已从物品取得: " + name);
            }
            for (String rawValue : rawValues) {
                String input = parseInputValue(ctx, rawValue);
                if (type.isMatch(input, name)) {
                    if (ctx.debug()) {
                        ctx.send("    匹配结果为 真，配置输入值为: '" + input + "'");
                    }
                    return true;
                }
                if (ctx.debug()) {
                    ctx.send("    匹配结果为 假，配置输入值为: '" + input + "'");
                }
            }
            return false;
        }
    }
}
