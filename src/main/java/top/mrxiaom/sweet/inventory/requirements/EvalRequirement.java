package top.mrxiaom.sweet.inventory.requirements;

import com.ezylang.evalex.BaseException;
import com.ezylang.evalex.Expression;
import com.ezylang.evalex.data.EvaluationValue;
import org.bukkit.configuration.ConfigurationSection;
import top.mrxiaom.pluginbase.func.gui.actions.IAction;
import top.mrxiaom.pluginbase.utils.PAPI;
import top.mrxiaom.sweet.inventory.SweetInventory;
import top.mrxiaom.sweet.inventory.func.menus.MenuInstance;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class EvalRequirement implements IRequirement {
    final boolean reverse;
    final String expression;
    final List<String> denyCommands;

    EvalRequirement(boolean reverse, String expression) {
        this(reverse, expression, new ArrayList<>());
    }
    EvalRequirement(boolean reverse, String expression, List<String> denyCommands) {
        this.reverse = reverse;
        this.expression = expression;
        this.denyCommands = denyCommands;
    }
    protected static void init(RequirementsRegistry registry) {
        registry.deserializers.put("eval", EvalRequirement::deserializer);
        registry.deserializers.put("计算", EvalRequirement::deserializer);
        registry.simpleDeserializers.add(EvalRequirement::simpleDeserializer);
    }

    protected static IRequirement deserializer(boolean alt, boolean reverse, ConfigurationSection section, String key) {
        String expression = section.getString(key + (alt ? ".表达式" : ".expression"));
        if (expression == null) return null;
        List<String> denyCommands = section.getStringList(key + (alt ? ".不满足需求执行" : ".deny-commands"));
        return new EvalRequirement(reverse, expression, denyCommands);
    }
    protected static IRequirement simpleDeserializer(String str) {
        if (str.startsWith("eval ")) return new EvalRequirement(false, str.substring(5));
        if (str.startsWith("运算 ")) return new EvalRequirement(false, str.substring(3));
        if (str.startsWith("!eval ")) return new EvalRequirement(true, str.substring(6));
        if (str.startsWith("!运算 ")) return new EvalRequirement(true, str.substring(4));
        return null;
    }

    @Override
    public boolean check(MenuInstance instance) {
        String str = PAPI.setPlaceholders(instance.getPlayer(), this.expression);
        try {
            Expression expression = new Expression(str);
            EvaluationValue result = expression.evaluate();
            Boolean booleanValue = result.getBooleanValue();
            if (booleanValue == null) throw new NullPointerException("evaluate result is null");
            return !booleanValue.equals(reverse);
        } catch (Throwable e) {
            SweetInventory.getInstance().warn(
                    String.format("计算表达式 `%s` 时出现一个异常: %s", str, e.toString())
            );
        }
        return reverse;
    }

    @Override
    public List<IAction> getDenyCommands() {
        return Collections.emptyList();
    }
}
