package top.mrxiaom.sweet.inventory.commands;
        
import com.google.common.collect.Lists;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.func.AutoRegister;
import top.mrxiaom.pluginbase.utils.Util;
import top.mrxiaom.sweet.inventory.SweetInventory;
import top.mrxiaom.sweet.inventory.func.AbstractModule;
import top.mrxiaom.sweet.inventory.func.Menus;
import top.mrxiaom.sweet.inventory.func.menus.MenuConfig;
import top.mrxiaom.sweet.inventory.func.menus.MenuInstance;

import java.util.*;

@AutoRegister
public class CommandMain extends AbstractModule implements CommandExecutor, TabCompleter, Listener {
    public CommandMain(SweetInventory plugin) {
        super(plugin);
        registerCommand("sweetinventory", this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length >= 2 && "open".equalsIgnoreCase(args[0])) {
            MenuConfig menu = Menus.inst().getMenu(args[1]);
            if (menu == null) {
                return t(sender, "&4找不到菜单 &c" + args[1]);
            }
            Player player;
            if (args.length == 3) {
                if (!sender.hasPermission("sweet.inventory.open.other")) {
                    return t(sender, "&c你没有进行该操作的权限");
                }
                player = Util.getOnlinePlayer(args[2]).orElse(null);
                if (player == null) {
                    return t(sender, "&c玩家不在线");
                }
            } else if (sender instanceof Player) {
                player = (Player) sender;
            } else return t(sender, "&c只有可以执行该命令");
            menu.create(player).open();
            return true;
        }
        if (args.length == 1 && "list".equalsIgnoreCase(args[0])) {
            if (!sender.hasPermission("sweet.inventory.list")) {
                return t(sender, "&c你没有进行该操作的权限");
            }
            Set<String> menusId = Menus.inst().getMenusId();
            StringBuilder sb = new StringBuilder("&e&l菜单列表:");
            for (String s : menusId) {
                sb.append("\n  &8· &f").append(s);
            }
            return t(sender, sb.toString());
        }
        if (args.length == 1 && "reload".equalsIgnoreCase(args[0]) && sender.isOp()) {
            plugin.reloadConfig();
            return t(sender, "&a配置文件已重载");
        }
        return true;
    }

    private static final List<String> emptyList = Lists.newArrayList();
    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            List<String> sub = new ArrayList<>();
            sub.add("open");
            if (sender.hasPermission("sweet.inventory.list")) sub.add("list");
            if (sender.hasPermission("sweet.inventory.reload")) sub.add("reload");
            return startsWith(sub, args[0]);
        }
        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("open")) {
                Set<String> menusId = Menus.inst().getMenusId();
                List<String> list = new ArrayList<>();
                for (String id : menusId) {
                    if (sender.hasPermission("sweet.inventory.open.menu." + id)) {
                        list.add(id);
                    }
                }
                return list;
            }
        }
        if (args.length == 3) {
            if (args[0].equalsIgnoreCase("open") && sender.hasPermission("sweet.inventory.open.other")) {
                return null;
            }
        }
        return emptyList;
    }

    public List<String> startsWith(Collection<String> list, String s) {
        return startsWith(null, list, s);
    }
    public List<String> startsWith(String[] addition, Collection<String> list, String s) {
        String s1 = s.toLowerCase();
        List<String> stringList = new ArrayList<>(list);
        if (addition != null) stringList.addAll(0, Lists.newArrayList(addition));
        stringList.removeIf(it -> !it.toLowerCase().startsWith(s1));
        return stringList;
    }
}
