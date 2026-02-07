package top.mrxiaom.sweet.inventory.requirements;

import org.bukkit.configuration.ConfigurationSection;
import top.mrxiaom.pluginbase.api.IAction;
import top.mrxiaom.sweet.inventory.func.menus.MenuInstance;

import java.util.ArrayList;
import java.util.List;

import static top.mrxiaom.pluginbase.actions.ActionProviders.loadActions;
import static top.mrxiaom.sweet.inventory.func.menus.MenuConfig.getBoolean;

public class PageRequirement implements IRequirement {
    private final boolean reverse;
    private final boolean hasPrevPage, hasNextPage;
    private final List<IAction> denyCommands;

    protected PageRequirement(boolean reverse, boolean hasPrevPage, boolean hasNextPage) {
        this(reverse, hasPrevPage, hasNextPage, new ArrayList<>());
    }
    protected PageRequirement(boolean reverse, boolean hasPrevPage, boolean hasNextPage, List<IAction> denyCommands) {
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
        boolean hasPrevPage = getBoolean(alt, section, key + (alt ? ".有上一页" : ".has-prev-page"), false);
        boolean hasNextPage = getBoolean(alt, section, key + (alt ? ".有上一页" : ".has-next-page"), false);
        List<IAction> denyCommands = loadActions(section, key + (alt ? ".不满足需求执行" : ".deny-commands"));
        return new PageRequirement(reverse, hasPrevPage, hasNextPage, denyCommands);
    }

    protected static IRequirement simpleDeserializer(String str) {
        if (str.equalsIgnoreCase("page has-prev-page")) return new PageRequirement(false, true, false);
        if (str.equalsIgnoreCase("页码 有上一页 ")) return new PageRequirement(false, true, false);
        if (str.equalsIgnoreCase("page has-next-page")) return new PageRequirement(false, false, true);
        if (str.equalsIgnoreCase("页码 有下一页 ")) return new PageRequirement(false, false, true);
        return null;
    }

    @Override
    public boolean check(MenuInstance menu) {
        if (hasPrevPage) {
            return menu.hasPrevPage() != reverse;
        }
        if (hasNextPage) {
            return menu.hasNextPage() != reverse;
        }
        return reverse;
    }

    @Override
    public List<IAction> denyCommands() {
        return denyCommands;
    }
}
