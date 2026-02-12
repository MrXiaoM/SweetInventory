package top.mrxiaom.sweet.inventory.func.menus.arguments.range;

import org.jspecify.annotations.NonNull;

public class IntegerRange implements IArgumentRange {
    private final int min, max;
    public IntegerRange(int min, int max) {
        this.min = min;
        this.max = max;
    }

    @Override
    public boolean isInRange(@NonNull Object value) {
        if (value instanceof Number) {
            int i = ((Number) value).intValue();
            return i >= min && i <= max;
        }
        return false;
    }
}
