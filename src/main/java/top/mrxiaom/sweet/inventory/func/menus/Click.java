package top.mrxiaom.sweet.inventory.func.menus;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import top.mrxiaom.pluginbase.api.IAction;
import top.mrxiaom.pluginbase.utils.Pair;
import top.mrxiaom.pluginbase.utils.depend.PAPI;
import top.mrxiaom.sweet.inventory.requirements.IRequirement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import static top.mrxiaom.sweet.inventory.requirements.RequirementsRegistry.loadRequirements;
import static top.mrxiaom.sweet.inventory.utils.ActionUtils.loadActions;

public class Click {
    private final List<IRequirement> requirements;
    private final List<IRequirement> fullRequirements;
    private final Map<String, Function<Player, String>> variables;
    private final List<IAction> commands;
    private final List<IAction> denyCommands;

    protected Click(List<IRequirement> requirements, Map<String, Function<Player, String>> variables, List<IAction> commands, List<IAction> denyCommands) {
        this.requirements = requirements;
        this.variables = variables;
        this.commands = commands;
        this.denyCommands = denyCommands;
        this.fullRequirements = new ArrayList<>(requirements);
        for (IAction action : commands) {
            // 如果 IAction 同时是 IRequirement，将它添加到需求列表里进行判定
            if (action instanceof IRequirement) {
                this.fullRequirements.add((IRequirement) action);
            }
        }
    }

    /**
     * 获取点击需求列表
     */
    public List<IRequirement> requirements() {
        return requirements;
    }

    /**
     * 获取完整的点击需求列表，其中包含了从点击操作中收集的点击需求
     */
    public List<IRequirement> fullRequirements() {
        return fullRequirements;
    }

    public void variables(List<Pair<String, Object>> r, Player player) {
        for (Map.Entry<String, Function<Player, String>> entry : variables.entrySet()) {
            String key = "${vars." + entry.getKey() + "}";
            Function<Player, String> value = entry.getValue();
            Supplier<String> supplier = () -> value.apply(player);
            r.add(Pair.of(key, supplier));
        }
    }

    /**
     * 获取执行操作列表
     */
    public List<IAction> commands() {
        return commands;
    }

    /**
     * 获取需求不满足时执行的操作列表
     */
    public List<IAction> denyCommands() {
        return denyCommands;
    }

    public static Click load(boolean alt, ConfigurationSection section, String key) {
        List<IAction> commands = new ArrayList<>();
        if (alt) {
            commands.addAll(loadActions(section, key + ".命令列表"));
            commands.addAll(loadActions(section, key + ".命令"));
            commands.addAll(loadActions(section, key + "命令列表"));
            commands.addAll(loadActions(section, key + "命令"));
        } else {
            commands.addAll(loadActions(section, key + ".commands-pre"));
            commands.addAll(loadActions(section, key + ".command-pre"));
            commands.addAll(loadActions(section, key + "-commands-pre"));
            commands.addAll(loadActions(section, key + "-command-pre"));
            commands.addAll(loadActions(section, key + ".commands"));
            commands.addAll(loadActions(section, key + ".command"));
            commands.addAll(loadActions(section, key + "-commands"));
            commands.addAll(loadActions(section, key + "-command"));
            commands.addAll(loadActions(section, key + ".commands-post"));
            commands.addAll(loadActions(section, key + ".command-post"));
            commands.addAll(loadActions(section, key + "-commands-post"));
            commands.addAll(loadActions(section, key + "-command-post"));
        }
        if (commands.isEmpty()) return null;
        List<IRequirement> requirements = loadRequirements(alt, section, key);

        Map<String, Function<Player, String>> variables = new HashMap<>();
        if (alt) {
            loadMap(variables, section, key + ".变量");
            loadMap(variables, section, key + "变量");
        } else {
            loadMap(variables, section, key + ".variables");
            loadMap(variables, section, key + "-variables");
        }

        List<IAction> denyCommands = new ArrayList<>();
        if (alt) {
            denyCommands.addAll(loadActions(section, key + ".不满足需求执行"));
            denyCommands.addAll(loadActions(section, key + "不满足需求执行"));
        } else {
            denyCommands.addAll(loadActions(section, key + ".deny-commands"));
            denyCommands.addAll(loadActions(section, key + "-deny-commands"));
        }
        return new Click(requirements, variables, commands, denyCommands);
    }

    private static void loadMap(Map<String, Function<Player, String>> map, ConfigurationSection section, String key) {
        ConfigurationSection exists = section.getConfigurationSection(key);
        if (exists != null) for (String configKey : exists.getKeys(false)) {
            String value = exists.getString(configKey);
            if (value != null) {
                map.put(configKey, p -> PAPI.setPlaceholders(p, value));
            }
        }
    }
}
