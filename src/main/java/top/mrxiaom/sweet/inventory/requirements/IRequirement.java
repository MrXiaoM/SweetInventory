package top.mrxiaom.sweet.inventory.requirements;

import org.bukkit.entity.Player;
import top.mrxiaom.pluginbase.func.gui.actions.IAction;
import top.mrxiaom.sweet.inventory.func.menus.MenuInstance;

import java.util.List;

public interface IRequirement {
    boolean check(MenuInstance instance);
    List<IAction> getDenyCommands();
}
