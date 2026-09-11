package top.mrxiaom.sweet.inventory.func.menus;

import org.jetbrains.annotations.Nullable;

public class MenuSlot {
    private final MenuInstance menu;
    private MenuIcon config;
    private long nextCooldownEndTime = 0L;

    public MenuSlot(MenuInstance menu, MenuIcon config) {
        this.menu = menu;
        this.config = config;
    }

    public MenuInstance menu() {
        return menu;
    }

    public boolean checkClickCooldown() {
        MenuIcon config = this.config;
        if (config == null) return true;
        long cooldown = config.clickCooldownMs();
        if (cooldown > 0) {
            long now = System.currentTimeMillis();
            if (now <= nextCooldownEndTime) {
                return false;
            }
            nextCooldownEndTime = now + cooldown;
        }
        return true;
    }

    @Nullable
    public MenuIcon icon() {
        return config;
    }

    public void icon(@Nullable MenuIcon config) {
        this.config = config;
    }
}
