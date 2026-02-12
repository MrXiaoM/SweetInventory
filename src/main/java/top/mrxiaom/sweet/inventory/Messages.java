package top.mrxiaom.sweet.inventory;

import com.google.common.collect.Lists;
import top.mrxiaom.pluginbase.func.language.Language;
import top.mrxiaom.pluginbase.func.language.Message;

import static top.mrxiaom.pluginbase.func.language.LanguageFieldAutoHolder.field;

@Language(prefix="messages.")
public class Messages {
    public static final Message no_permission = field("&c你没有执行该操作的权限");
    public static final Message player__not_online = field("&c玩家不在线 &7(或不存在)");
    public static final Message player__only = field("只有玩家才能执行该操作");

    @Language(prefix="messages.arguments")
    public static class Arguments {
        public static final Message no_integer = field("&e请输入一个整数");
        public static final Message no_number = field("&e请输入一个实数");
        public static final Message no_material = field("&e请输入一个物品类型");
        public static final Message out_of_range = field("&e你输入的参数不在指定范围内");
    }

    @Language(prefix="messages.command.")
    public static class Command {
        public static final Message open__no_menu_found = field("&c找不到菜单&e %menu%");
        public static final Message list__header = field(Lists.newArrayList("&e&l菜单列表: "));
        public static final Message list__entry = field(Lists.newArrayList("  &8· &f%menu%"));
        public static final Message list__footer = field(Lists.newArrayList());
        public static final Message reload__success = field("&a配置文件已重载");
    }
}
