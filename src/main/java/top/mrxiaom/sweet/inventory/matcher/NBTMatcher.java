package top.mrxiaom.sweet.inventory.matcher;

import de.tr7zw.changeme.nbtapi.NBT;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.func.AutoRegister;
import top.mrxiaom.pluginbase.utils.ListPair;
import top.mrxiaom.pluginbase.utils.Pair;
import top.mrxiaom.sweet.inventory.SweetInventory;
import top.mrxiaom.sweet.inventory.api.ItemMatchContext;
import top.mrxiaom.sweet.inventory.api.ItemMatcher;
import top.mrxiaom.sweet.inventory.func.AbstractModule;

import java.util.ArrayList;
import java.util.List;

@AutoRegister(priority = 990)
public class NBTMatcher extends AbstractModule implements ItemMatcher.Provider {
    public NBTMatcher(SweetInventory plugin) {
        super(plugin);
        plugin.getItemMatcherRegistry().register(this);
    }

    @Override
    public @Nullable ItemMatcher parse(@NotNull ConfigurationSection config) {
        Pair<String, AbstractStringMatcher.Type> pair = AbstractStringMatcher.Type.parse("nbt", config::contains);
        if (pair != null) {
            String path = pair.key();
            AbstractStringMatcher.Type type = pair.value();
            ConfigurationSection section = config.getConfigurationSection(path);
            if (section != null) {
                ListPair<String, List<String>> kv = new ListPair<>();
                for (String key : section.getKeys(false)) {
                    List<String> rawValues = new ArrayList<>();
                    if (config.isList(path)) {
                        rawValues.addAll(section.getStringList(key));
                    } else {
                        rawValues.add(section.getString(key, ""));
                    }
                    kv.add(key, rawValues);
                }
                return new Impl(type, kv);
            }
        }
        return null;
    }

    public static class Impl extends AbstractStringMatcher {
        private final ListPair<String, List<String>> kv;
        public Impl(Type type, ListPair<String, List<String>> kv) {
            super(type);
            this.kv = kv;
        }

        @Override
        public boolean isItemMatch(@NotNull ItemMatchContext ctx) {
            if (kv.isEmpty()) return true;
            if (ctx.debug()) {
                ctx.send("\n正在进行 物品NBT 的 " + type.debugName() + " 判定");
            }
            return NBT.get(ctx.item(), nbt -> {
                for (Pair<String, List<String>> pair : kv) {
                    String key = pair.key();
                    String value = nbt.resolveOrNull(key, String.class);
                    if (ctx.debug()) {
                        ctx.send("    已从物品取得 " + key + " = '" + value + "'");
                    }
                    if (isKeyNotMatch(ctx, pair.value(), value)) {
                        if (ctx.debug()) {
                            ctx.send("        匹配结果为 假，配置输入值为: '" + pair.value() + "'");
                        }
                        return false;
                    }
                    if (ctx.debug()) {
                        ctx.send("        匹配结果为 真，配置输入值为: '" + pair.value() + "'");
                    }
                }
                if (ctx.debug()) {
                    ctx.send("    最终匹配结果为 真");
                }
                return true;
            });
        }

        private boolean isKeyNotMatch(ItemMatchContext ctx, List<String> rawValues, String value) {
            for (String rawValue : rawValues) {
                String input = parseInputValue(ctx, rawValue);
                if (type.isMatch(input, value)) {
                    return false;
                }
            }
            return true;
        }
    }
}
