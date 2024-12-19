package top.mrxiaom.sweet.inventory.func.menus;

import org.bukkit.configuration.ConfigurationSection;
import top.mrxiaom.pluginbase.func.gui.actions.IAction;
import top.mrxiaom.sweet.inventory.requirements.IRequirement;

import java.util.ArrayList;
import java.util.List;

import static top.mrxiaom.pluginbase.func.AbstractGuiModule.loadActions;
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

    public List<IRequirement> getRequirements() {
        return requirements;
    }

    public List<IAction> getCommands() {
        return commands;
    }

    public List<IAction> getDenyCommands() {
        return denyCommands;
    }

    public static Click load(boolean alt, ConfigurationSection section, String key) {
        List<IAction> commands = loadActions(section, key + (alt ? ".命令列表" : ".commands"));
        if (commands.isEmpty()) return null;
        List<IRequirement> requirements = loadRequirements(alt, section, key);
        List<IAction> denyCommands = loadActions(section, key + (alt ? ".不满足需求执行" : ".deny-commands"));
        return new Click(requirements, commands, denyCommands);
    }
}
