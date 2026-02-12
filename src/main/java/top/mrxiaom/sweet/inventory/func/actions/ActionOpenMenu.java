package top.mrxiaom.sweet.inventory.func.actions;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.api.IAction;
import top.mrxiaom.pluginbase.utils.Pair;
import top.mrxiaom.sweet.inventory.SweetInventory;
import top.mrxiaom.sweet.inventory.func.MenuManager;
import top.mrxiaom.sweet.inventory.func.menus.MenuConfig;

import java.util.List;

public class ActionOpenMenu implements IAction {
    public final String menu;
    public final String[] args;
    public ActionOpenMenu(String menu, String[] args) {
        this.menu = menu.trim();
        this.args = args;
    }

    @Override
    public void run(@Nullable Player player, @Nullable List<Pair<String, Object>> list) {
        if (player != null) {
            MenuConfig menu = MenuManager.inst().getMenu(this.menu);
            if (menu == null) {
                SweetInventory.getInstance().warn("找不到菜单 " + this.menu);
                player.closeInventory();
                return;
            }
            menu.open(player, args);
        }
    }

    public static ActionOpenMenu of(String menuAndArgs) {
        int spaceIndex = menuAndArgs.indexOf(' ');
        if (spaceIndex != -1) {
            String menu = menuAndArgs.substring(0, spaceIndex);
            String args = menuAndArgs.substring(spaceIndex + 1);
            return new ActionOpenMenu(menu, args.split(" "));
        }
        return new ActionOpenMenu(menuAndArgs, new String[0]);
    }
}
