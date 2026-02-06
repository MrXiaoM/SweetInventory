package top.mrxiaom.sweet.inventory.requirements;

import com.google.common.collect.Lists;
import org.bukkit.configuration.ConfigurationSection;
import top.mrxiaom.pluginbase.func.AutoRegister;
import top.mrxiaom.sweet.inventory.SweetInventory;
import top.mrxiaom.sweet.inventory.func.AbstractModule;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

@AutoRegister
public class RequirementsRegistry extends AbstractModule {
    @FunctionalInterface
    public interface RequirementDeserializer {
        IRequirement apply(boolean alt, boolean reverse, ConfigurationSection section, String key);
    }
    Map<String, RequirementDeserializer> deserializers = new HashMap<>();
    List<Function<String, IRequirement>> simpleDeserializers = new ArrayList<>();
    public RequirementsRegistry(SweetInventory plugin) {
        super(plugin);
        try {
            init();
        } catch (Throwable t) {
            warn(t);
        }
    }

    private void init() {
        deserializers.clear();
        simpleDeserializers.clear();
        NumberRequirement.init(this);
        PageRequirement.init(this);
        PermissionRequirement.init(this);
        EvalRequirement.init(this);
    }

    public static RequirementsRegistry inst() {
        return instanceOf(RequirementsRegistry.class);
    }

    public static List<IRequirement> loadRequirements(boolean alt, ConfigurationSection parent, String key) {
        RequirementsRegistry self = inst();
        AtomicBoolean error = new AtomicBoolean(false);
        List<IRequirement> requirements = new ArrayList<>();
        // 需求简写-单句需求
        String singleRequirementKey = key + (alt ? "需求" : "-requirement");
        if (parent.contains(singleRequirementKey)) {
            String str = parent.getString(singleRequirementKey, null);
            boolean find = false;
            if (str != null && !str.isEmpty()) {
                for (Function<String, IRequirement> deserializer : self.simpleDeserializers) {
                    IRequirement requirement = deserializer.apply(str);
                    if (requirement != null) {
                        requirements.add(requirement);
                        find = true;
                        break;
                    }
                }
            }
            if (!find) {
                self.warn("[需求:" + str + "] 没有简写格式反序列化器支持你键入的需求");
                error.set(true);
            }
        }
        // 需求简写-多句需求
        loadImpl(alt, parent.getConfigurationSection(key + (alt ? "需求列表" : "-requirements")), requirements, error);
        // 支持 deny-commands 的完整写法
        loadImpl(alt, parent.getConfigurationSection(key + (alt ? ".需求列表" : ".requirements")), requirements, error);
        if (error.get()) {
            self.warn("加载的需求中存在错误，已自动替换为恒不通过的需求，请处理配置文件中的错误");
            return Lists.newArrayList(ErrorRequirement.INSTANCE);
        }
        return requirements;
    }

    private static void loadImpl(boolean alt, ConfigurationSection section, List<IRequirement> requirements, AtomicBoolean error) {
        RequirementsRegistry self = inst();
        if (section != null) for (String ignore : section.getKeys(false)) {
            if (section.isConfigurationSection(ignore)) {
                String type = section.getString(ignore + (alt ? ".类型" : ".type"));
                boolean reverse = type.startsWith("!");
                if (reverse) type = type.substring(1);
                RequirementDeserializer deserializer = self.deserializers.get(type);
                if (deserializer == null) {
                    self.warn("[需求:" + ignore + "] 找不到需求类型 " + type);
                    error.set(true);
                    continue;
                }
                IRequirement requirement = deserializer.apply(alt, reverse, section, ignore);
                if (requirement == null) {
                    self.warn("[需求:" + ignore + "] 加载需求 " + type + " 时出错");
                    error.set(true);
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
    }
}
