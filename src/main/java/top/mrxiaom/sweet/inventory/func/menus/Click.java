package top.mrxiaom.sweet.inventory.func.menus;

import org.bukkit.configuration.ConfigurationSection;
import top.mrxiaom.sweet.inventory.requirements.IRequirement;

import java.util.ArrayList;
import java.util.List;

import static top.mrxiaom.sweet.inventory.requirements.RequirementsRegistry.loadRequirements;

public class Click {
    final List<IRequirement> requirements;
    final List<String> commands;
    final List<String> denyCommands;

    Click(List<IRequirement> requirements, List<String> commands, List<String> denyCommands) {
        this.requirements = requirements;
        this.commands = commands;
        this.denyCommands = denyCommands;
    }

    public List<IRequirement> getRequirements() {
        return requirements;
    }

    public List<String> getCommands() {
        return commands;
    }

    public List<String> getDenyCommands() {
        return denyCommands;
    }

    public static Click load(boolean alt, ConfigurationSection section, String key) {
        List<String> commands = section.getStringList(key + (alt ? ".命令列表" : ".commands"));
        if (commands.isEmpty()) return null;
        List<IRequirement> requirements = loadRequirements(alt, section, key);
        List<String> denyCommands = section.getStringList(key + (alt ? ".不满足需求执行" : ".deny-commands"));
        return new Click(requirements, commands, denyCommands);
    }
}
