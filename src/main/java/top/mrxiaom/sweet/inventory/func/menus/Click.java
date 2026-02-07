package top.mrxiaom.sweet.inventory.func.menus;

import com.google.common.collect.Lists;
import org.bukkit.configuration.ConfigurationSection;
import top.mrxiaom.pluginbase.actions.ActionProviders;
import top.mrxiaom.pluginbase.api.IAction;
import top.mrxiaom.sweet.inventory.requirements.IRequirement;

import java.util.ArrayList;
import java.util.List;

import static top.mrxiaom.sweet.inventory.requirements.RequirementsRegistry.loadRequirements;

public class Click {
    private final List<IRequirement> requirements;
    private final List<IAction> commands;
    private final List<IAction> denyCommands;

    protected Click(List<IRequirement> requirements, List<IAction> commands, List<IAction> denyCommands) {
        this.requirements = requirements;
        this.commands = commands;
        this.denyCommands = denyCommands;
    }

    /**
     * 获取点击需求列表
     */
    public List<IRequirement> requirements() {
        return requirements;
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
            commands.addAll(loadActions(section, key + ".commands"));
            commands.addAll(loadActions(section, key + ".command"));
            commands.addAll(loadActions(section, key + "-commands"));
            commands.addAll(loadActions(section, key + "-command"));
        }
        if (commands.isEmpty()) return null;
        List<IRequirement> requirements = loadRequirements(alt, section, key);

        List<IAction> denyCommands = new ArrayList<>();
        if (alt) {
            denyCommands.addAll(loadActions(section, key + ".不满足需求执行"));
            denyCommands.addAll(loadActions(section, key + "不满足需求执行"));
        } else {
            denyCommands.addAll(loadActions(section, key + ".deny-commands"));
            denyCommands.addAll(loadActions(section, key + "-deny-commands"));
        }
        return new Click(requirements, commands, denyCommands);
    }

    private static List<IAction> loadActions(ConfigurationSection section, String key) {
        if (section.contains(key)) {
            if (section.isList(key)) {
                return ActionProviders.loadActions(section.getStringList(key));
            } else {
                String line = section.getString(key);
                if (line != null) {
                    return Lists.newArrayList(ActionProviders.loadAction(line));
                }
            }
        }
        return new ArrayList<>();
    }
}
