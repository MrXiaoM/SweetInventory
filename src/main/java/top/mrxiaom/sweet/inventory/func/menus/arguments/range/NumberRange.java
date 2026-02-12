package top.mrxiaom.sweet.inventory.func.menus.arguments.range;

import org.jspecify.annotations.NonNull;

public class NumberRange implements IArgumentRange {
    private final double min, max;
    public NumberRange(double min, double max) {
        this.min = min;
        this.max = max;
    }

    @Override
    public boolean isInRange(@NonNull Object value) {
        if (value instanceof Number) {
            double v = ((Number) value).doubleValue();
            return v >= min && v <= max;
        }
        return false;
    }
}
