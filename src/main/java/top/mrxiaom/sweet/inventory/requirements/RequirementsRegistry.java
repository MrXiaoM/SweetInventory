package top.mrxiaom.sweet.inventory.requirements;

import com.google.common.collect.Lists;
import org.bukkit.configuration.ConfigurationSection;
import top.mrxiaom.pluginbase.func.AutoRegister;
import top.mrxiaom.sweet.inventory.SweetInventory;
import top.mrxiaom.sweet.inventory.func.AbstractModule;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

@AutoRegister
public class RequirementsRegistry extends AbstractModule {
    Map<String, BiFunction<ConfigurationSection, String, IRequirement>> deserializers = new HashMap<>();
    List<Function<String, IRequirement>> simpleDeserializers = new ArrayList<>();
    public RequirementsRegistry(SweetInventory plugin) {
        super(plugin);
    }

    public static RequirementsRegistry inst() {
        return instanceOf(RequirementsRegistry.class);
    }

    public static List<IRequirement> loadRequirements(boolean alt, ConfigurationSection parent, String key) {
        RequirementsRegistry self = inst();
        boolean error = false;
        List<IRequirement> requirements = new ArrayList<>();
        ConfigurationSection section = parent.getConfigurationSection(key + (alt ? ".需求列表" : ".requirements"));
        if (section != null) for (String ignore : section.getKeys(false)) {
            if (section.isConfigurationSection(ignore)) {
                String type = section.getString(ignore + (alt ? ".类型" : ".type"));
                BiFunction<ConfigurationSection, String, IRequirement> deserializer = self.deserializers.get(type);
                if (deserializer == null) {
                    self.warn("[需求:" + ignore + "] 找不到需求类型 " + type);
                    error = true;
                    continue;
                }
                IRequirement requirement = deserializer.apply(section, ignore);
                if (requirement == null) {
                    self.warn("[需求:" + ignore + "] 加载需求 " + type + " 时出错");
                    error = true;
                    continue;
                }
                requirements.add(requirement);
                continue;
            }
            String str = section.getString(ignore);
            if (str != null && !str.isEmpty()) {
                boolean find = false;
                for (Function<String, IRequirement> deserializer : self.simpleDeserializers) {
                    IRequirement requirement = deserializer.apply(str);
                    if (requirement != null) {
                        requirements.add(requirement);
                        find = true;
                        break;
                    }
                }
                if (find) continue;
                self.warn("[需求:" + ignore + "] 没有简写格式反序列化器支持你键入的需求");
            }
            self.warn("[需求:" + ignore + "] 无法解析这个需求，不符合完整格式和简写格式");
        }
        if (error) {
            self.warn("加载的需求中存在错误，已自动替换为恒不通过的需求，请处理配置文件中的错误");
            return Lists.newArrayList(ErrorRequirement.INSTANCE);
        }
        return requirements;
    }
}
