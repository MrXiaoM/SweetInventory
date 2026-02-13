package top.mrxiaom.sweet.inventory.func.menus.arguments;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;
import top.mrxiaom.pluginbase.func.language.Message;
import top.mrxiaom.pluginbase.utils.Util;
import top.mrxiaom.sweet.inventory.Messages;
import top.mrxiaom.sweet.inventory.func.menus.arguments.range.IArgumentRange;
import top.mrxiaom.sweet.inventory.func.menus.arguments.range.IntegerRange;
import top.mrxiaom.sweet.inventory.func.menus.arguments.range.NumberRange;

import java.util.function.BiFunction;
import java.util.function.Function;

@ApiStatus.Internal
public enum EnumArgumentType implements IArgumentType {
    STRING(it -> it,
            it -> null,
            null),
    INTEGER(it -> Util.parseInt(it).orElse(null),
            parseRange(it -> Util.parseInt(it).orElse(null), Integer.MIN_VALUE, Integer.MAX_VALUE, IntegerRange::new),
            Messages.Arguments.no_integer),
    NUMBER(it -> Util.parseDouble(it).orElse(null),
            parseRange(it -> Util.parseDouble(it).orElse(null), Double.MIN_VALUE, Double.MAX_VALUE, NumberRange::new),
            Messages.Arguments.no_number),
    BOOLEAN(it -> {
                if (it.equals("true") || it.equals("yes") || it.equals("on") || it.equals("开") || it.equals("是") || it.equals("好")) return true;
                if (it.equals("false") || it.equals("no") || it.equals("off") || it.equals("关") || it.equals("否") || it.equals("不")) return false;
                return null;
            },
            it -> null,
            Messages.Arguments.no_boolean),
    MATERIAL(
            it -> Util.valueOr(Material.class, it, null),
            it -> null,
            Messages.Arguments.no_material),

    ;
    private final Function<String, Object> implValue;
    private final Function<String, IArgumentRange> implRange;
    private final Message failedMessage;
    EnumArgumentType(Function<String, Object> implValue, Function<String, IArgumentRange> implRange, Message failedMessage) {
        this.implValue = implValue;
        this.implRange = implRange;
        this.failedMessage = failedMessage;
    }

    @Override
    public @NonNull String getTypeName() {
        return name().toLowerCase();
    }

    @Nullable
    @Override
    public Object parseValue(@Nullable Player player, @NotNull String input) {
        Object value = implValue.apply(input);
        if (value == null && failedMessage != null && player != null) {
            failedMessage.tm(player);
        }
        return value;
    }

    @Nullable
    public IArgumentRange parseRange(@NotNull String input) {
        return implRange.apply(input);
    }

    private static <T, R extends IArgumentRange> Function<String, R> parseRange(Function<String, T> parser, T minimum, T maximum, BiFunction<T, T, R> funcMinMaxResult) {
        return input -> {
            String[] split = input.split(",", 2);
            T min, max;
            if (split.length == 1) {
                T value = parser.apply(split[0].trim());
                if (value == null) {
                    return null;
                }
                min = max = value;
            } else {
                String minStr = split[0].trim();
                String maxStr = split[1].trim();
                if (minStr.equalsIgnoreCase("MIN")) {
                    min = minimum;
                } else {
                    min = parser.apply(minStr);
                }
                if (maxStr.equalsIgnoreCase("MAX")) {
                    max = maximum;
                } else {
                    max = parser.apply(maxStr);
                }
                if (min == null || max == null) {
                    return null;
                }
            }
            return funcMinMaxResult.apply(min, max);
        };
    }
}
