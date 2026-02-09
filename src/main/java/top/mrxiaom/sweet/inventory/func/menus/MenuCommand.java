package top.mrxiaom.sweet.inventory.func.menus;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jspecify.annotations.NonNull;
import top.mrxiaom.sweet.inventory.func.Menus;

import java.util.List;

public class MenuCommand extends Command {
    private final Menus parent;
    public MenuCommand(Menus parent, @NonNull String name) {
        super(name);
        this.parent = parent;
    }

    @Override
    public boolean execute(@NonNull CommandSender sender, @NonNull String commandLabel, @NonNull String @NonNull [] args) {
        parent.onCommand(sender, getName(), args);
        return true;
    }

    @Override
    public @NonNull List<String> tabComplete(@NonNull CommandSender sender, @NonNull String alias, @NonNull String @NonNull [] args) throws IllegalArgumentException {
        List<String> list = parent.onTabComplete(sender, getName(), args);
        if (list != null) {
            return list;
        } else {
            return super.tabComplete(sender, alias, args);
        }
    }
}
