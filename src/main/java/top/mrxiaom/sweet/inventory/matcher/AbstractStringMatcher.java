package top.mrxiaom.sweet.inventory.matcher;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.utils.Pair;
import top.mrxiaom.pluginbase.utils.depend.PAPI;
import top.mrxiaom.sweet.inventory.api.ItemMatchContext;
import top.mrxiaom.sweet.inventory.api.ItemMatcher;

import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public abstract class AbstractStringMatcher implements ItemMatcher {
    public enum Type {
        contains("-contains", "包含匹配", (input, itemId) -> itemId.contains(input)),
        startsWith("-starts-with", "开头匹配", (input, itemId) -> itemId.startsWith(input)),
        endsWith("-ends-with", "结尾匹配", (input, itemId) -> itemId.endsWith(input)),
        equals("-equals", "精确匹配", (input, itemId) -> itemId.equals(input)),
        equalsIgnoreCase("-equals-ignore-case", "忽略大小写匹配", (input, itemId) -> itemId.equalsIgnoreCase(input)),
        regex("-regex", "正则表达式匹配", (input, itemId) -> {
            try {
                return Pattern.matches(input, itemId);
            } catch (PatternSyntaxException ignored) {
                return false;
            }
        })

        ;
        private final String suffix;
        private final String debugName;
        private final BiPredicate<String, String> impl;
        Type(String suffix, String debugName, BiPredicate<String, String> impl) {
            this.suffix = suffix;
            this.debugName = debugName;
            this.impl = impl;
        }

        public String debugName() {
            return debugName;
        }

        public boolean isMatch(String input, String itemId) {
            return impl.test(input, itemId);
        }

        @Nullable
        public static Pair<String, Type> parse(String key, Predicate<String> predicate) {
            if (predicate.test(key)) {
                return Pair.of(key, equals);
            }
            for (Type type : values()) {
                String path = key + type.suffix;
                if (predicate.test(path)) {
                    return Pair.of(path, type);
                }
            }
            return null;
        }
    }
    protected final Type type;
    protected AbstractStringMatcher(Type type) {
        this.type = type;
    }

    protected String parseInputValue(@NotNull ItemMatchContext ctx, String rawValue) {
        // 解析 PAPI 后强制替换 § 为 &，以防老版本 PAPI 会自动替换 & 颜色字符
        String value = PAPI.setPlaceholders(ctx.player(), rawValue).replace("§", "&");
        return Pair.replace(value, ctx.r());
    }
}
