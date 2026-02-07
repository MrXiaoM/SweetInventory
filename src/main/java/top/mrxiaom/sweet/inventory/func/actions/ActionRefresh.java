package top.mrxiaom.sweet.inventory.func.actions;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.api.IAction;
import top.mrxiaom.pluginbase.func.GuiManager;
import top.mrxiaom.pluginbase.gui.IGuiHolder;
import top.mrxiaom.pluginbase.utils.Pair;
import top.mrxiaom.sweet.inventory.func.menus.MenuInstance;

import java.util.List;

public class ActionRefresh implements IAction {
    public static final ActionRefresh INSTANCE = new ActionRefresh();
    private ActionRefresh() {}
    @Override
    public void run(@Nullable Player player, @Nullable List<Pair<String, Object>> list) {
        IGuiHolder gui = GuiManager.inst().getOpeningGui(player);
        if (gui instanceof MenuInstance) {
            MenuInstance menu = (MenuInstance) gui;
            menu.plugin().getScheduler().runTask(menu::refresh);
        }
    }
}
