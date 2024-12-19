package top.mrxiaom.sweet.inventory.func.actions;

import org.bukkit.entity.Player;
import top.mrxiaom.pluginbase.func.gui.actions.IAction;
import top.mrxiaom.pluginbase.utils.Pair;
import top.mrxiaom.sweet.inventory.SweetInventory;
import top.mrxiaom.sweet.inventory.func.Menus;
import top.mrxiaom.sweet.inventory.func.menus.MenuConfig;

public class ActionOpenMenu implements IAction {
    public final String menu;
    public ActionOpenMenu(String menu) {
        this.menu = menu.trim();
    }

    @Override
    public void run(Player player, Pair<String, Object>[] pairs) {
        MenuConfig menu = Menus.inst().getMenu(this.menu);
        if (menu == null) {
            SweetInventory.getInstance().warn("找不到菜单 " + this.menu);
            player.closeInventory();
            return;
        }
        menu.create(player).open();
    }
}
