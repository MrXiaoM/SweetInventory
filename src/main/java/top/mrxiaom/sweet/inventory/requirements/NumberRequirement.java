package top.mrxiaom.sweet.inventory.requirements;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.api.IAction;
import top.mrxiaom.pluginbase.utils.ListPair;
import top.mrxiaom.pluginbase.utils.Pair;
import top.mrxiaom.pluginbase.utils.Util;
import top.mrxiaom.pluginbase.utils.depend.PAPI;
import top.mrxiaom.sweet.inventory.func.menus.MenuInstance;

import java.util.List;

import static top.mrxiaom.pluginbase.actions.ActionProviders.loadActions;

public class NumberRequirement implements IRequirement {
    public enum Operator {
        EQUALS("=", "==", "equals", "等于", "相等"),
        LARGE_THAN(">", "large than", "大于"),
        LARGE_THAN_OR_EUALS(">=", "large than or equals", "大于等于", "大于或等于"),
        LESS_THAN("<", "less than", "smaller than", "小于"),
        LESS_THAN_OR_EUALS("<=", "less than or equals", "smaller than or equals", "小于等于", "小于或等于");

        public final String[] types;
        Operator(String... types) {
            this.types = types;
        }
        @Nullable
        public static Operator fromString(String s) {
            if (s == null) return null;
            for (Operator value : values()) {
                for (String type : value.types) {
                    if (type.equalsIgnoreCase(s)) {
                        return value;
                    }
                }
            }
            return null;
        }
    }
    final boolean reverse;
    final Operator type;
    final String input;
    final String output;
    final List<IAction> denyCommands;

    NumberRequirement(boolean reverse, Operator type, String input, String output, List<IAction> denyCommands) {
        this.reverse = reverse;
        this.type = type;
        this.input = input;
        this.output = output;
        this.denyCommands = denyCommands;
    }

    protected static void init(RequirementsRegistry registry) {
        for (NumberRequirement.Operator value : NumberRequirement.Operator.values()) {
            for (String type : value.types) {
                registry.deserializers.put(type, NumberRequirement::deserializer);
            }
        }
    }

    protected static IRequirement deserializer(boolean alt, boolean reverse, ConfigurationSection section, String key) {
        String type = section.getString(key + (alt ? ".类型" : ".type"));
        String input = section.getString(key + (alt ? ".输入" : ".input"));
        String output = section.getString(key + (alt ? ".输出" : ".output"));
        Operator operator = Operator.fromString(type);
        if (input == null || output == null || operator == null) return null;
        List<IAction> denyCommands = loadActions(section, key + (alt ? ".不满足需求执行" : ".deny-commands"));
        return new NumberRequirement(reverse, operator, input, output, denyCommands);
    }

    @Override
    public boolean check(MenuInstance menu) {
        ListPair<String, Object> r = menu.newReplacements();
        String input = PAPI.setPlaceholders(menu.getPlayer(), Pair.replace(this.input, r));
        String output = PAPI.setPlaceholders(menu.getPlayer(), Pair.replace(this.output, r));
        if (type.equals(Operator.EQUALS)) {
            if (input.equals(output)) return true;
        }
        Double temp1 = Util.parseDouble(input).orElse(null);
        Double temp2 = Util.parseDouble(output).orElse(null);
        if (temp1 == null || temp2 == null) return false;
        double num1 = temp1, num2 = temp2;
        switch (type) {
            case EQUALS:
                return num1 == num2;
            case LESS_THAN:
                return num1 < num2;
            case LESS_THAN_OR_EUALS:
                return num1 <= num2;
            case LARGE_THAN:
                return num1 > num2;
            case LARGE_THAN_OR_EUALS:
                return num1 >= num2;
        }
        // 未添加的运算类型返回 false
        return false;
    }

    @Override
    public List<IAction> denyCommands() {
        return denyCommands;
    }
}
