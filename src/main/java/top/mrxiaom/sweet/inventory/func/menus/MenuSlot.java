package top.mrxiaom.sweet.inventory.func.menus;

import org.jetbrains.annotations.Nullable;

public class MenuSlot {
    private final MenuInstance menu;
    private MenuIcon config;

    public MenuSlot(MenuInstance menu, MenuIcon config) {
        this.menu = menu;
        this.config = config;
    }

    public MenuInstance menu() {
        return menu;
    }

    @Nullable
    public MenuIcon icon() {
        return config;
    }

    public void icon(@Nullable MenuIcon config) {
        this.config = config;
    }
}
