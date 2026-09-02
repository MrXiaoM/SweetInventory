package top.mrxiaom.sweet.inventory.matcher;

import net.kyori.adventure.text.Component;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.func.AutoRegister;
import top.mrxiaom.pluginbase.utils.AdventureItemStack;
import top.mrxiaom.pluginbase.utils.AdventureUtil;
import top.mrxiaom.pluginbase.utils.Pair;
import top.mrxiaom.sweet.inventory.SweetInventory;
import top.mrxiaom.sweet.inventory.api.ItemMatchContext;
import top.mrxiaom.sweet.inventory.api.ItemMatcher;
import top.mrxiaom.sweet.inventory.func.AbstractModule;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

@AutoRegister(priority = 990)
public class LoreMatcher extends AbstractModule implements ItemMatcher.Provider {
    public LoreMatcher(SweetInventory plugin) {
        super(plugin);
        plugin.getItemMatcherRegistry().register(this);
    }

    @Override
    public @Nullable ItemMatcher parse(@NotNull ConfigurationSection config) {
        Pair<String, AbstractStringMatcher.Type> pair = AbstractStringMatcher.Type.parse("lore", config::contains);
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
            StringJoiner joiner = new StringJoiner("\n");
            List<Component> list = AdventureItemStack.getItemLore(ctx.item());
            for (Component line : list) {
                joiner.add(AdventureUtil.legacySection(line));
            }
            String legacy = joiner.toString()
                    .replace("&", "&&")
                    .replace("§", "&");
            if (ctx.debug()) {
                ctx.send("\n正在进行 物品描述 的 " + type.debugName() + " 判定，已从物品取得: ['" + legacy.replace("\n", "', '") + "']");
            }
            for (String rawValue : rawValues) {
                String input = parseInputValue(ctx, rawValue);
                if (type.isMatch(input, legacy)) {
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
