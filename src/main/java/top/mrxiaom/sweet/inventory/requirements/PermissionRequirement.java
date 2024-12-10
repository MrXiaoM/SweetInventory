package top.mrxiaom.sweet.inventory.requirements;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import top.mrxiaom.pluginbase.utils.PAPI;
import top.mrxiaom.sweet.inventory.func.menus.MenuInstance;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PermissionRequirement implements IRequirement {
    final boolean reverse;
    final String permission;
    final List<String> denyCommands;

    PermissionRequirement(boolean reverse, String permission) {
        this(reverse, permission, new ArrayList<>());
    }
    PermissionRequirement(boolean reverse, String permission, List<String> denyCommands) {
        this.reverse = reverse;
        this.permission = permission;
        this.denyCommands = denyCommands;
    }

    protected static void init(RequirementsRegistry registry) {
        registry.deserializers.put("permission", PermissionRequirement::deserializer);
        registry.deserializers.put("权限", PermissionRequirement::deserializer);
        registry.simpleDeserializers.add(PermissionRequirement::simpleDeserializer);
    }

    protected static IRequirement deserializer(boolean alt, boolean reverse, ConfigurationSection section, String key) {
        String permission = section.getString(key + (alt ? ".权限" : ".permission"));
        if (permission == null) return null;
        List<String> denyCommands = section.getStringList(key + (alt ? ".不满足需求执行" : ".deny-commands"));
        return new PermissionRequirement(reverse, permission, denyCommands);
    }
    protected static IRequirement simpleDeserializer(String str) {
        if (str.startsWith("perm ")) return new PermissionRequirement(false, str.substring(5));
        if (str.startsWith("权限 ")) return new PermissionRequirement(false, str.substring(3));
        if (str.startsWith("!perm ")) return new PermissionRequirement(true, str.substring(6));
        if (str.startsWith("!权限 ")) return new PermissionRequirement(true, str.substring(4));
        return null;
    }

    @Override
    public boolean check(MenuInstance instance) {
        Player player = instance.getPlayer();
        String permission = PAPI.setPlaceholders(player, this.permission);
        return player.hasPermission(permission) != reverse;
    }

    @Override
    public List<String> getDenyCommands() {
        return denyCommands;
    }
}
