package top.mrxiaom.sweet.inventory.func.actions;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.api.IAction;
import top.mrxiaom.pluginbase.utils.Pair;
import top.mrxiaom.sweet.inventory.SweetInventory;

import java.util.List;

public class ActionConnectServer implements IAction {
    public final String server;
    public ActionConnectServer(String menu) {
        this.server = menu.trim();
    }

    @Override
    public void run(@Nullable Player player, @Nullable List<Pair<String, Object>> list) {
        SweetInventory.getInstance().connect(player, server);
    }
}
