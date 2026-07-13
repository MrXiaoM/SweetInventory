package top.mrxiaom.sweet.inventory.func.menus.arguments.range;

import org.jetbrains.annotations.NotNull;

public class NumberRange implements IArgumentRange {
    private final double min, max;
    public NumberRange(double min, double max) {
        this.min = min;
        this.max = max;
    }

    @Override
    public boolean isInRange(@NotNull Object value) {
        if (value instanceof Number) {
            double v = ((Number) value).doubleValue();
            return v >= min && v <= max;
        }
        return false;
    }
}
