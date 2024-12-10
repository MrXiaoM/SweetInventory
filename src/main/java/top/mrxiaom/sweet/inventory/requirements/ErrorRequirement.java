package top.mrxiaom.sweet.inventory.requirements;

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
    public List<String> getDenyCommands() {
        return Collections.emptyList();
    }
}
