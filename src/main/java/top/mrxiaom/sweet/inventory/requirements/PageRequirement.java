package top.mrxiaom.sweet.inventory.requirements;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import top.mrxiaom.pluginbase.api.IAction;
import top.mrxiaom.pluginbase.utils.depend.PAPI;
import top.mrxiaom.sweet.inventory.func.menus.MenuInstance;

import java.util.ArrayList;
import java.util.List;

import static top.mrxiaom.pluginbase.actions.ActionProviders.loadActions;

public class PageRequirement implements IRequirement {
    final boolean reverse;
    final boolean hasPrevPage, hasNextPage;
    final List<IAction> denyCommands;

    PageRequirement(boolean reverse, boolean hasPrevPage, boolean hasNextPage) {
        this(reverse, hasPrevPage, hasNextPage, new ArrayList<>());
    }
    PageRequirement(boolean reverse, boolean hasPrevPage, boolean hasNextPage, List<IAction> denyCommands) {
        this.reverse = reverse;
        this.hasPrevPage = hasPrevPage;
        this.hasNextPage = hasNextPage;
        this.denyCommands = denyCommands;
    }

    protected static void init(RequirementsRegistry registry) {
        registry.deserializers.put("page", PageRequirement::deserializer);
        registry.deserializers.put("页码", PageRequirement::deserializer);
        registry.simpleDeserializers.add(PageRequirement::simpleDeserializer);
    }

    protected static IRequirement deserializer(boolean alt, boolean reverse, ConfigurationSection section, String key) {
        boolean hasPrevPage = section.getBoolean(key + (alt ? ".有上一页" : ".has-prev-page"), false);
        boolean hasNextPage = section.getBoolean(key + (alt ? ".有上一页" : ".has-next-page"), false);
        List<IAction> denyCommands = loadActions(section, key + (alt ? ".不满足需求执行" : ".deny-commands"));
        return new PageRequirement(reverse, hasPrevPage, hasPrevPage, denyCommands);
    }

    protected static IRequirement simpleDeserializer(String str) {
        if (str.equalsIgnoreCase("page has-prev-page")) return new PageRequirement(false, true, false);
        if (str.equalsIgnoreCase("页码 有上一页 ")) return new PageRequirement(false, true, false);
        if (str.equalsIgnoreCase("page has-next-page")) return new PageRequirement(false, false, true);
        if (str.equalsIgnoreCase("页码 有下一页 ")) return new PageRequirement(false, false, true);
        return null;
    }

    @Override
    public boolean check(MenuInstance instance) {
        if (hasPrevPage) {
            return instance.hasPrevPage() != reverse;
        }
        if (hasNextPage) {
            return instance.hasNextPage() != reverse;
        }
        return false != reverse;
    }

    @Override
    public List<IAction> getDenyCommands() {
        return denyCommands;
    }
}
