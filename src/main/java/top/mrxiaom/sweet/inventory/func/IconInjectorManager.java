package top.mrxiaom.sweet.inventory.func;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.configuration.file.FileConfiguration;
import top.mrxiaom.pluginbase.func.AutoRegister;
import top.mrxiaom.pluginbase.utils.ConfigUtils;
import top.mrxiaom.sweet.inventory.SweetInventory;
import top.mrxiaom.sweet.inventory.func.menus.IconInjector;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@AutoRegister
public class IconInjectorManager extends AbstractModule {
    private final List<IconInjector> iconInjectorList = new ArrayList<>();
    public IconInjectorManager(SweetInventory plugin) {
        super(plugin);
    }

    @Override
    public int priority() {
        return 990;
    }

    @Override
    public void reloadConfig(MemoryConfiguration pluginConfig) {
        File file = new File(plugin.getDataFolder(), "icon-injector.yml");
        if (!file.exists()) {
            plugin.saveResource("icon-injector.yml", file);
        }
        iconInjectorList.clear();
        FileConfiguration config = plugin.resolveGotoFlag(ConfigUtils.load(file));
        for (ConfigurationSection section : ConfigUtils.getSectionList(config, "icon-injectors")) {
            iconInjectorList.add(IconInjector.load(this, section));
        }
        info("加载了 " + iconInjectorList.size() + " 个图标格式注入配置");
    }

    public void applyInjects(ConfigurationSection fromIcon) {
        for (IconInjector injector : iconInjectorList) {
            injector.merge(fromIcon);
        }
    }

    public static IconInjectorManager inst() {
        return instanceOf(IconInjectorManager.class);
    }
}
