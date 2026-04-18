package top.mrxiaom.sweet.inventory.requirements;

import top.mrxiaom.pluginbase.api.IAction;
import top.mrxiaom.pluginbase.utils.Pair;
import top.mrxiaom.sweet.inventory.func.menus.MenuInstance;

import java.util.List;

public interface IRequirement {
    boolean check(MenuInstance menu, List<Pair<String, Object>> r);
    List<IAction> denyCommands();
}
