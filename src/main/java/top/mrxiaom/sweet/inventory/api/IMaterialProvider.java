package top.mrxiaom.sweet.inventory.api;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.sweet.inventory.func.menus.MenuIcon;

/**
 * 物品图标提供器
 */
public interface IMaterialProvider {
    default int providerPriority() {
        return 1000;
    }

    /**
     * 解析菜单图标物品，如果不符合输入规则，返回 <code>null</code>
     * @param player 玩家
     * @param icon 图标配置
     */
    @Nullable ItemStack parse(Player player, MenuIcon icon);
}
