package top.mrxiaom.sweet.inventory.func.menus;

import com.google.common.collect.Iterables;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permissible;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.actions.ActionProviders;
import top.mrxiaom.sweet.inventory.Messages;
import top.mrxiaom.sweet.inventory.SweetInventory;

import java.util.*;

import static top.mrxiaom.pluginbase.utils.CollectionUtils.startsWith;

public class MenuCommand extends Command {
    public static class ChildCommand {
        private final MenuConfig menu;
        private final List<String> arguments;
        public ChildCommand(MenuConfig menu, List<String> arguments) {
            this.menu = menu;
            this.arguments = arguments;
        }

        @NotNull
        public MenuConfig menu() {
            return menu;
        }

        @NotNull
        public List<String> arguments() {
            return arguments;
        }

        public boolean isTheSameArguments(@NotNull List<String> arguments) {
            if (arguments.size() != this.arguments.size()) return false;
            for (int i = 0; i < this.arguments.size(); i++) {
                if (!arguments.get(i).equalsIgnoreCase(this.arguments.get(i))) {
                    return false;
                }
            }
            return true;
        }

        @Nullable
        public String[] match(String[] args) {
            if (args.length < arguments.size()) return null;
            for (int i = 0; i < arguments.size(); i++) {
                if (!args[i].equalsIgnoreCase(arguments.get(i))) {
                    return null;
                }
            }
            String[] newArgs = new String[args.length - arguments.size()];
            if (newArgs.length > 0) {
                System.arraycopy(args, arguments.size(), newArgs, 0, newArgs.length);
            }
            return newArgs;
        }
    }
    private @Nullable MenuConfig menu;
    private final List<ChildCommand> childCommands = new ArrayList<>();
    @ApiStatus.Internal
    public MenuCommand(@NotNull String name, @Nullable MenuConfig menu) {
        super(name);
        this.menu = menu;
    }

    @Nullable
    public MenuConfig menu() {
        return menu;
    }

    public void menu(@Nullable MenuConfig menu) {
        this.menu = menu;
    }

    @Nullable
    public ChildCommand getChild(List<String> arguments) {
        for (ChildCommand child : childCommands) {
            if (child.isTheSameArguments(arguments)) {
                return child;
            }
        }
        return null;
    }

    public boolean addChild(List<String> arguments, MenuConfig menu) {
        if (getChild(arguments) != null) return false;
        childCommands.add(new ChildCommand(menu, arguments));
        return true;
    }

    public boolean removeChild(List<String> arguments) {
        return childCommands.removeIf(it -> it.isTheSameArguments(arguments));
    }

    public boolean removeChild(MenuConfig menu) {
        return childCommands.removeIf(it -> it.menu().equals(menu));
    }

    public boolean removeChild(ChildCommand childCommand) {
        return childCommands.remove(childCommand);
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            Messages.player__only.tm(sender);
            return true;
        }
        Player player = (Player) sender;
        for (ChildCommand child : childCommands) {
            String[] newArgs = child.match(args);
            if (newArgs != null) {
                if (!child.menu().hasPermission(player)) {
                    return Messages.no_permission.tm(player);
                }
                child.menu().open(player, newArgs);
                return true;
            }
        }
        if (menu != null) {
            // 如果不匹配子命令，且根菜单存在，则执行根菜单命令
            if (!menu.hasPermission(player)) {
                return Messages.no_permission.tm(player);
            }
            menu.open(player, args);
            return true;
        } else {
            // 如果根菜单不存在，显示第一个子命令的帮助
            ChildCommand first = Iterables.getFirst(childCommands, null);
            if (first != null) {
                ActionProviders.run(SweetInventory.getInstance(), player, first.menu().menuArguments().helpActions());
            }
        }
        return true;
    }

    public List<String> getTabCompleteList(int index, Permissible p) {
        List<String> list = new ArrayList<>();
        for (ChildCommand childCommand : childCommands) {
            if (index >= childCommand.arguments().size()) continue;
            if (childCommand.menu().hasPermission(p)) {
                list.add(childCommand.arguments().get(index));
            }
        }
        return list;
    }

    @Nullable
    protected List<String> onTabComplete(@NotNull CommandSender sender, @NotNull String[] args) {
        if (args.length > 0) {
            List<String> list = getTabCompleteList(args.length - 1, sender);
            if (list.isEmpty()) {
                return Collections.emptyList();
            }
            return startsWith(args[args.length - 1], list);
        }
        return Collections.emptyList();
    }

    @Override
    public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException {
        List<String> list = onTabComplete(sender, args);
        if (list != null) {
            return list;
        } else {
            return super.tabComplete(sender, alias, args);
        }
    }
}
