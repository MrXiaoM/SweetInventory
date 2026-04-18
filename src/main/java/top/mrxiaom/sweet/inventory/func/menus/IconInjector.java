package top.mrxiaom.sweet.inventory.func.menus;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.utils.ConfigUtils;
import top.mrxiaom.sweet.inventory.func.IconInjectorManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class IconInjector {
    private final @NotNull IconInjectorManager manager;
    private final @NotNull List<String> requireKeys;
    private final @Nullable ConfigurationSection merge;
    private IconInjector(@NotNull IconInjectorManager manager, @NotNull ConfigurationSection config) {
        this.manager = manager;
        this.requireKeys = config.getStringList("require-keys");
        this.merge = config.getConfigurationSection("merge");
    }

    @NotNull
    public IconInjectorManager getManager() {
        return manager;
    }

    @NotNull
    public List<String> getRequireKeys() {
        return requireKeys;
    }

    @Nullable
    public ConfigurationSection getMerge() {
        return merge;
    }

    public void merge(ConfigurationSection config) {
        if (merge != null) {
            for (String requireKey : requireKeys) {
                if (!config.contains(requireKey)) {
                    return;
                }
            }
            merge(config, merge);
        }
    }

    private void merge(ConfigurationSection fromIcon, ConfigurationSection toAdd) {
        for (String key : toAdd.getKeys(false)) {
            Object value = toAdd.get(key);
            if (fromIcon.contains(key)) {
                if (value instanceof List<?>) {
                    List<?> fromList = fromIcon.getList(key);
                    if (fromList != null) {
                        List<Object> newList = new ArrayList<>();
                        newList.addAll(fromList);
                        newList.addAll((List<?>) value);
                        fromIcon.set(key, newList);
                    }
                }
                if (value instanceof Map<?, ?>) {
                    ConfigurationSection fromSection = fromIcon.getConfigurationSection(key);
                    if (fromSection != null) {
                        // TODO: merge(fromSection, ConfigUtils.toSection((Map<?, ?>) value));
                        fromIcon.set(key, fromSection);
                    }
                }
                if (value instanceof ConfigurationSection) {
                    ConfigurationSection fromSection = fromIcon.getConfigurationSection(key);
                    if (fromSection != null) {
                        merge(fromSection, (ConfigurationSection) value);
                        fromIcon.set(key, fromSection);
                    }
                }
            } else {
                fromIcon.set(key, value);
            }
        }
    }

    public static IconInjector load(IconInjectorManager manager, ConfigurationSection config) {
        return new IconInjector(manager, config);
    }
}
