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
    final List<IRequirement> requirements;
    final List<IAction> commands;
    final List<IAction> denyCommands;

    Click(List<IRequirement> requirements, List<IAction> commands, List<IAction> denyCommands) {
        this.requirements = requirements;
        this.commands = commands;
        this.denyCommands = denyCommands;
    }

    public List<IRequirement> requirements() {
        return requirements;
    }

    public List<IAction> commands() {
        return commands;
    }

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
        List<IAction> denyCommands = loadActions(section, key + (alt ? ".不满足需求执行" : ".deny-commands"));
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
