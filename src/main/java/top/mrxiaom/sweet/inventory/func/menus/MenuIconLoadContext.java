package top.mrxiaom.sweet.inventory.func.menus;

import java.util.concurrent.atomic.AtomicInteger;

public class MenuIconLoadContext {
    private final AtomicInteger autoMenuSequence = new AtomicInteger(0);
    private final AtomicInteger autoPageSequence = new AtomicInteger(0);
    private MenuIconLoadContext() {}

    public AtomicInteger autoMenuSequence() {
        return autoMenuSequence;
    }

    public AtomicInteger autoPageSequence() {
        return autoPageSequence;
    }

    public static MenuIconLoadContext create() {
        return new MenuIconLoadContext();
    }
}
