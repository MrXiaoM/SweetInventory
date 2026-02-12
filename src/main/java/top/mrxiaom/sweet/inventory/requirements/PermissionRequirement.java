package top.mrxiaom.sweet.inventory.requirements;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import top.mrxiaom.pluginbase.api.IAction;
import top.mrxiaom.pluginbase.utils.ListPair;
import top.mrxiaom.pluginbase.utils.Pair;
import top.mrxiaom.pluginbase.utils.depend.PAPI;
import top.mrxiaom.sweet.inventory.func.menus.MenuInstance;

import java.util.ArrayList;
import java.util.List;

import static top.mrxiaom.pluginbase.actions.ActionProviders.loadActions;

public class PermissionRequirement implements IRequirement {
    final boolean reverse;
    final String permission;
    final List<IAction> denyCommands;

    PermissionRequirement(boolean reverse, String permission) {
        this(reverse, permission, new ArrayList<>());
    }
    PermissionRequirement(boolean reverse, String permission, List<IAction> denyCommands) {
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
        List<IAction> denyCommands = loadActions(section, key + (alt ? ".不满足需求执行" : ".deny-commands"));
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
    public boolean check(MenuInstance menu) {
        Player player = menu.getPlayer();
        ListPair<String, Object> r = menu.newReplacements();
        String permission = PAPI.setPlaceholders(player, Pair.replace(this.permission, r));
        return player.hasPermission(permission) != reverse;
    }

    @Override
    public List<IAction> denyCommands() {
        return denyCommands;
    }
}
