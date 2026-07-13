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

public class ActionReference implements IAction {
    public static final IActionProvider PROVIDER = obj -> {
        if (obj instanceof ConfigurationSection) {
            ConfigurationSection input = (ConfigurationSection) obj;
            if (!input.contains("type")) {
                String ref1 = input.getString("ref");
                if (ref1 != null) {
                    return new ActionReference(ref1);
                }
                String ref2 = input.getString("reference");
                if (ref2 != null) {
                    return new ActionReference(ref2);
                }
            }
        } else {
            String input = String.valueOf(obj);
            if (input.startsWith("[ref]")) {
                return new ActionReference(input.substring(5));
            }
            if (input.startsWith("ref:")) {
                return new ActionReference(input.substring(4));
            }
            if (input.startsWith("[reference]")) {
                return new ActionReference(input.substring(11));
            }
            if (input.startsWith("reference:")) {
                return new ActionReference(input.substring(10));
            }
        }
        return null;
    };
    public final String reference;
    public ActionReference(String reference) {
        this.reference = reference;
    }

    @Override
    public void run(@Nullable Player player, @Nullable List<Pair<String, Object>> list) {
    }
}
