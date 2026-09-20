package top.mrxiaom.sweet.inventory.func.menus;

public class SlotChar {
    public enum EnumType {
        MENU, PAGE_GUIDE
    }
    private final EnumType type;
    private final int sequence;
    private final char value;
    public SlotChar(EnumType type, int sequence, char value) {
        this.type = type;
        this.sequence = sequence;
        this.value = value;
    }

    public EnumType type() {
        return type;
    }

    public int sequence() {
        return sequence;
    }

    public boolean isSpaceChar() {
        return value == ' ' || value == '　' || Character.isSpaceChar(value);
    }

    public char value() {
        return value;
    }
}
