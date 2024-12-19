package top.mrxiaom.sweet.inventory.func.actions;

import org.bukkit.entity.Player;
import top.mrxiaom.pluginbase.func.gui.actions.IAction;
import top.mrxiaom.pluginbase.utils.Pair;
import top.mrxiaom.sweet.inventory.SweetInventory;
import top.mrxiaom.sweet.inventory.func.Menus;
import top.mrxiaom.sweet.inventory.func.menus.MenuConfig;

public class ActionConnectServer implements IAction {
    public final String server;
    public ActionConnectServer(String menu) {
        this.server = menu.trim();
    }

    @Override
    public void run(Player player, Pair<String, Object>[] pairs) {
        SweetInventory.getInstance().connect(player, server);
    }
}
