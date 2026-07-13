package top.mrxiaom.sweet.inventory.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.bukkit.configuration.ConfigurationSection;
import top.mrxiaom.pluginbase.actions.ActionProviders;
import top.mrxiaom.pluginbase.api.IAction;
import top.mrxiaom.pluginbase.utils.ConfigUtils;
import top.mrxiaom.sweet.inventory.SweetInventory;
import top.mrxiaom.sweet.inventory.func.actions.ActionReference;

import java.util.ArrayList;
import java.util.List;

public class ActionUtils {

    public static List<IAction> loadActions(ConfigurationSection config, String key) {
        List<IAction> result = new ArrayList<>();
        loadActions(result, config, key, 0);
        return result;
    }

    private static void loadActions(List<IAction> result, ConfigurationSection config, String key, int indent) {
        if (config.contains(key)) {
            if (config.isList(key)) {
                List<Object> list = ConfigUtils.getList(config, key);
                if (list.isEmpty()) return;
                for (Object obj : list) {
                    IAction action = ActionProviders.loadAction(obj);
                    if (action != null) {
                        resolveReference(result, config, action, indent);
                    } else {
                        String line;
                        if (obj instanceof ConfigurationSection) {
                            ConfigurationSection section = (ConfigurationSection) obj;
                            String type = section.getString("type");
                            if (type != null) {
                                line = "type: " + type;
                            } else {
                                line = toJson(section).toString();
                            }
                        } else {
                            line = String.valueOf(obj);
                        }
                        SweetInventory.getInstance().warn("配置中存在无效的操作 " + line);
                    }
                }
            } else {
                String line = config.getString(key);
                if (line != null) {
                    IAction action = ActionProviders.loadAction(line);
                    if (action != null) {
                        resolveReference(result, config, action, indent);
                    } else {
                        SweetInventory.getInstance().warn("配置中存在无效的操作" + line);
                    }
                }
            }
        }
    }

    private static void resolveReference(List<IAction> result, ConfigurationSection config, IAction action, int indent) {
        if (action instanceof ActionReference) {
            String newKey = ((ActionReference) action).reference;
            if (indent < 32) {
                loadActions(result, config, newKey, indent + 1);
            } else {
                SweetInventory.getInstance().warn("操作 reference 指向的 " + newKey + " 配置，出现递归次数过多的情况，可能存在循环引用，请检查你的配置是否正确");
            }
        } else {
            result.add(action);
        }
    }

    private static JsonObject toJson(ConfigurationSection section) {
        JsonObject object = new JsonObject();
        for (String key : section.getKeys(false)) {
            Object value = section.get(key);
            if (value instanceof String) {
                object.addProperty(key, (String) value);
            }
            if (value instanceof Character) {
                object.addProperty(key, (Character) value);
            }
            if (value instanceof Number) {
                object.addProperty(key, (Number) value);
            }
            if (value instanceof Boolean) {
                object.addProperty(key, (Boolean) value);
            }
            if (value instanceof ConfigurationSection) {
                object.add(key, toJson((ConfigurationSection) value));
            }
            if (value instanceof List<?>) {
                object.add(key, toJson((List<?>) value));
            }
        }
        return object;
    }

    private static JsonArray toJson(List<?> list) {
        JsonArray array = new JsonArray();
        for (Object value : list) {
            if (value instanceof String) {
                array.add((String) value);
            }
            if (value instanceof Character) {
                array.add((Character) value);
            }
            if (value instanceof Number) {
                array.add((Number) value);
            }
            if (value instanceof Boolean) {
                array.add((Boolean) value);
            }
            if (value instanceof ConfigurationSection) {
                array.add(toJson((ConfigurationSection) value));
            }
            if (value instanceof List<?>) {
                array.add(toJson((List<?>) value));
            }
        }
        return array;
    }

}
