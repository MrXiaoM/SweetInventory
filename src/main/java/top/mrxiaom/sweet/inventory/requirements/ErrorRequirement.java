package top.mrxiaom.sweet.inventory.requirements;

import top.mrxiaom.pluginbase.api.IAction;
import top.mrxiaom.sweet.inventory.func.menus.MenuInstance;

import java.util.Collections;
import java.util.List;

public class ErrorRequirement implements IRequirement {
    public static final ErrorRequirement INSTANCE = new ErrorRequirement();
    @Override
    public boolean check(MenuInstance instance) {
        return false;
    }

    @Override
    public List<IAction> getDenyCommands() {
        return Collections.emptyList();
    }
}
