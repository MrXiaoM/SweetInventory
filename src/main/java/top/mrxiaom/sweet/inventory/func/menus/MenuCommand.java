package top.mrxiaom.sweet.inventory.func.menus;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jspecify.annotations.NonNull;
import top.mrxiaom.sweet.inventory.func.MenuCommandManager;

import java.util.List;

public class MenuCommand extends Command {
    private final MenuCommandManager manager;
    public MenuCommand(MenuCommandManager manager, @NonNull String name) {
        super(name);
        this.manager = manager;
    }

    @Override
    public boolean execute(@NonNull CommandSender sender, @NonNull String commandLabel, @NonNull String @NonNull [] args) {
        manager.onCommand(sender, getName(), args);
        return true;
    }

    @Override
    public @NonNull List<String> tabComplete(@NonNull CommandSender sender, @NonNull String alias, @NonNull String @NonNull [] args) throws IllegalArgumentException {
        List<String> list = manager.onTabComplete(sender, getName(), args);
        if (list != null) {
            return list;
        } else {
            return super.tabComplete(sender, alias, args);
        }
    }
}
