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
        contains("-contains", (input, itemId) -> itemId.contains(input)),
        startsWith("-starts-with", (input, itemId) -> itemId.startsWith(input)),
        endsWith("-ends-with", (input, itemId) -> itemId.endsWith(input)),
        equals("-equals", (input, itemId) -> itemId.equals(input)),
        equalsIgnoreCase("-equals-ignore-case", (input, itemId) -> itemId.equalsIgnoreCase(input)),
        regex("-regex", (input, itemId) -> {
            try {
                return Pattern.matches(input, itemId);
            } catch (PatternSyntaxException ignored) {
                return false;
            }
        })

        ;
        private final String suffix;
        private final BiPredicate<String, String> impl;
        Type(String suffix, BiPredicate<String, String> impl) {
            this.suffix = suffix;
            this.impl = impl;
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
        String value = PAPI.setPlaceholders(ctx.player(), rawValue);
        return Pair.replace(value, ctx.r());
    }
}
