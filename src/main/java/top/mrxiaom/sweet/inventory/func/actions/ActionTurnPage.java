package top.mrxiaom.sweet.inventory.func.actions;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.api.IAction;
import top.mrxiaom.pluginbase.func.GuiManager;
import top.mrxiaom.pluginbase.gui.IGuiHolder;
import top.mrxiaom.pluginbase.utils.Pair;
import top.mrxiaom.sweet.inventory.func.menus.MenuInstance;

import java.util.List;
import java.util.Objects;

public class ActionTurnPage implements IAction {
    public static final ActionTurnPage PREV = new ActionTurnPage(false);
    public static final ActionTurnPage NEXT = new ActionTurnPage(true);
    private final boolean next;
    private ActionTurnPage(boolean next) {
        this.next = next;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj instanceof ActionTurnPage) {
            return ((ActionTurnPage) obj).next == next;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(next);
    }

    @Override
    public void run(@Nullable Player player, @Nullable List<Pair<String, Object>> list) {
        IGuiHolder gui = GuiManager.inst().getOpeningGui(player);
        if (gui instanceof MenuInstance) {
            MenuInstance menu = (MenuInstance) gui;
            if (next) {
                if (menu.hasNextPage()) {
                    menu.page(menu.page() + 1);
                }
            } else {
                if (menu.hasPrevPage()) {
                    menu.page(menu.page() - 1);
                }
            }
            menu.plugin().getScheduler().runTask(menu::refresh);
        }
    }
}
