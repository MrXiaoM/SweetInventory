package top.mrxiaom.sweet.inventory.func.actions;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.api.IAction;
import top.mrxiaom.pluginbase.api.IActionProvider;
import top.mrxiaom.pluginbase.utils.CollectionUtils;
import top.mrxiaom.pluginbase.utils.Pair;
import top.mrxiaom.pluginbase.utils.depend.PAPI;

import java.util.List;

public class ActionSetVars implements IAction {
    public static final IActionProvider PROVIDER = obj -> {
        if (obj instanceof ConfigurationSection) {
            ConfigurationSection input = (ConfigurationSection) obj;
            String type = input.getString("type");
            if ("set-vars".equalsIgnoreCase(type) || "set-variables".equalsIgnoreCase(type)) {
                return parse(input);
            }
            ConfigurationSection section1 = input.getConfigurationSection("set-vars");
            if (section1 != null) {
                return parse(section1);
            }
            ConfigurationSection section2 = input.getConfigurationSection("set-variables");
            if (section2 != null) {
                return parse(section2);
            }
        } else {
            String input = String.valueOf(obj);
            if (input.startsWith("[set-vars]")) {
                return parse(input.substring(10));
            }
            if (input.startsWith("set-vars:")) {
                return parse(input.substring(9));
            }
            if (input.startsWith("[set-variables]")) {
                return parse(input.substring(15));
            }
            if (input.startsWith("set-variables:")) {
                return parse(input.substring(14));
            }
        }
        return null;
    };
    private static IAction parse(String str) {
        List<String> split = CollectionUtils.split(str, '=', 2);
        if (split.size() == 2) {
            String key = split.get(0);
            String rawValue = split.get(1);
            return new ActionSetVars(key, rawValue);
        }
        return null;
    }
    private static IAction parse(ConfigurationSection section) {
        String key = section.getString("key");
        String rawValue = section.getString("value");
        if (key != null && rawValue != null) {
            return new ActionSetVars(key, rawValue);
        }
        return null;
    }
    public final String key;
    public final String rawValue;
    public ActionSetVars(String key, String rawValue) {
        this.key = key;
        this.rawValue = rawValue;
    }

    @Override
    public void run(@Nullable Player player, @Nullable List<Pair<String, Object>> list) {
        if (list != null) {
            String value;
            if (player == null) {
                value = Pair.replace(rawValue, list);
            } else {
                value = Pair.replace(PAPI.setPlaceholders(player, rawValue), list);
            }
            list.add(Pair.of("${vars." + key + "}", value));
        }
    }
}
