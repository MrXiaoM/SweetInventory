package top.mrxiaom.sweet.inventory.func.menus.arguments;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.actions.ActionProviders;
import top.mrxiaom.pluginbase.api.IAction;
import top.mrxiaom.pluginbase.utils.Util;
import top.mrxiaom.sweet.inventory.Messages;
import top.mrxiaom.sweet.inventory.SweetInventory;
import top.mrxiaom.sweet.inventory.func.menus.arguments.range.IArgumentRange;

import java.util.*;

/**
 * 菜单命令参数集合
 */
public class MenuArguments {
    public static class Argument {
        private final IArgumentType type;
        private final String name;
        private final IArgumentRange range;
        private final Object defaultValue;
        Argument(IArgumentType type, String name, IArgumentRange range, Object defaultValue) {
            this.type = type;
            this.name = name;
            this.range = range;
            this.defaultValue = defaultValue;
        }

        /**
         * 获取参数类型
         */
        @NotNull
        public IArgumentType type() {
            return type;
        }

        /**
         * 获取参数代表的变量名
         */
        @NotNull
        public String name() {
            return name;
        }

        /**
         * 获取输入的值是否在允许范围内
         */
        public boolean isInRange(@NotNull Object value) {
            return range == null || range.isInRange(value);
        }

        /**
         * 获取参数默认值
         */
        @Nullable
        public Object defaultValue() {
            return defaultValue;
        }

        /**
         * 获取该参数是否为必选参数，当 {@link Argument#defaultValue()} 为 <code>null</code> 时，返回 <code>true</code>
         */
        public boolean required() {
            return defaultValue == null;
        }
    }
    public static final MenuArguments EMPTY = new MenuArguments(false, Collections.emptyList(), Collections.emptyList());
    private final List<IAction> helpActions;
    private final List<Argument> arguments;
    protected MenuArguments(boolean alt, List<IAction> helpActions, List<ConfigurationSection> sectionList) {
        this.helpActions = helpActions;
        this.arguments = new ArrayList<>();
        for (ConfigurationSection section : sectionList) {
            String typeStr = section.getString(alt ? "类型" : "type");
            IArgumentType type = Util.valueOr(EnumArgumentType.class, typeStr, null);
            String name = section.getString(alt ? "名称" : "name");
            String rangeStr = section.getString(alt ? "范围" : "range", null);
            String defaultValueStr = section.getString(alt ? "默认值" : "default-value", null);
            if (type == null) {
                throw new IllegalArgumentException("未知的命令参数类型 " + typeStr);
            }
            if (name == null) {
                throw new IllegalArgumentException("未输入命令参数变量名");
            }
            IArgumentRange range;
            if (rangeStr != null) {
                range = type.parseRange(rangeStr);
                if (range == null) {
                    throw new IllegalArgumentException("命令参数范围 '" + rangeStr + "' 不适配 " + type.getTypeName() + " 类型");
                }
            } else {
                range = null;
            }
            Object defaultValue;
            if (defaultValueStr != null) {
                defaultValue = type.parseValue(null, defaultValueStr);
                if (defaultValue == null) {
                    throw new IllegalArgumentException("命令参数默认值 '" + defaultValueStr + "' 无法解析为 " + type.getTypeName() + " 类型");
                }
            } else {
                defaultValue = null;
            }
            this.arguments.add(new Argument(type, name, range, defaultValue));
        }
        boolean flag = false;
        for (Argument argument : arguments) {
            if (!argument.required()) {
                flag = true;
            } else if (flag) {
                throw new IllegalArgumentException("命令参数必选标记顺序异常，在“非必选参数”的后面不允许出现“必选参数”（没有默认值的参数视为“必选参数”）");
            }
        }
    }

    /**
     * 获取命令参数列表是否为空
     */
    public boolean isEmpty() {
        return arguments.isEmpty();
    }

    /**
     * 从命令参数解析变量，如果解析失败，返回 <code>null</code>
     * @param player 发送命令的玩家
     * @param args 输入的命令参数
     */
    @Nullable
    public Map<String, Object> parseVariables(@NotNull Player player, @NotNull String[] args) {
        Map<String, Object> variables = new HashMap<>();
        int length = args.length;
        for (int i = 0; i < arguments.size(); i++) {
            Argument argument = arguments.get(i);
            if (i < length) {
                // 参数还没用完
                String input = args[i];
                Object value = argument.type().parseValue(player, input);
                if (value == null) {
                    // 如果参数解析出错，结束解析参数
                    return null;
                }
                if (argument.isInRange(value)) {
                    // 参数在允许范围内，添加变量
                    variables.put(argument.name(), value);
                } else {
                    // 参数不在允许范围内，结束解析参数
                    Messages.Arguments.out_of_range.tm(player);
                    return null;
                }
            } else {
                // 参数已经用完了
                if (argument.required()) {
                    // 参数用完了，还存在必选参数，则结束参数解析，提示帮助命令
                    ActionProviders.run(SweetInventory.getInstance(), player, helpActions);
                    return null;
                } else {
                    // 可选参数添加默认值
                    variables.put(argument.name(), argument.defaultValue());
                }
            }
        }
        return variables;
    }

    public static @NotNull MenuArguments load(boolean alt, @NotNull List<IAction> helpActions, @NotNull List<ConfigurationSection> sectionList) {
        return new MenuArguments(alt, helpActions, sectionList);
    }
}
