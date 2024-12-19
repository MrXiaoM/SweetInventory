package top.mrxiaom.sweet.inventory.func.menus;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.func.gui.LoadedIcon;
import top.mrxiaom.pluginbase.func.gui.actions.IAction;
import top.mrxiaom.sweet.inventory.SweetInventory;
import top.mrxiaom.sweet.inventory.requirements.IRequirement;

import java.util.ArrayList;
import java.util.List;

import static top.mrxiaom.pluginbase.func.AbstractGuiModule.loadActions;
import static top.mrxiaom.sweet.inventory.func.menus.MenuConfig.getBoolean;
import static top.mrxiaom.sweet.inventory.requirements.RequirementsRegistry.loadRequirements;

public class MenuIcon {
    final LoadedIcon icon;
    final List<Character> slots;
    final boolean needsUpdate;
    final int priorityLess;
    final List<IRequirement> viewRequirements;
    final List<IAction> viewDenyCommands;
    final @Nullable Click leftClick;
    final @Nullable Click rightClick;
    final @Nullable Click shiftLeftClick;
    final @Nullable Click shiftRightClick;
    final @Nullable Click dropClick;
    final @Nullable Click ctrlDropClick;

    public MenuIcon(LoadedIcon icon, List<Character> slots, boolean needsUpdate, int priorityLess, List<IRequirement> viewRequirements, List<IAction> viewDenyCommands, Click leftClick, Click rightClick, Click shiftLeftClick, Click shiftRightClick, Click dropClick, Click ctrlDropClick) {
        this.icon = icon;
        this.slots = slots;
        this.needsUpdate = needsUpdate;
        this.priorityLess = priorityLess;
        this.viewRequirements = viewRequirements;
        this.viewDenyCommands = viewDenyCommands;
        this.leftClick = leftClick;
        this.rightClick = rightClick;
        this.shiftLeftClick = shiftLeftClick;
        this.shiftRightClick = shiftRightClick;
        this.dropClick = dropClick;
        this.ctrlDropClick = ctrlDropClick;
    }

    public LoadedIcon getIcon() {
        return icon;
    }

    public List<Character> getSlots() {
        return slots;
    }

    public boolean isNeedsUpdate() {
        return needsUpdate;
    }

    public int getPriorityLess() {
        return priorityLess;
    }

    public List<IRequirement> getViewRequirements() {
        return viewRequirements;
    }

    public List<IAction> getViewDenyCommands() {
        return viewDenyCommands;
    }

    @Nullable
    public Click getLeftClick() {
        return leftClick;
    }

    @Nullable
    public Click getRightClick() {
        return rightClick;
    }

    @Nullable
    public Click getShiftLeftClick() {
        return shiftLeftClick;
    }

    @Nullable
    public Click getShiftRightClick() {
        return shiftRightClick;
    }

    @Nullable
    public Click getDropClick() {
        return dropClick;
    }

    @Nullable
    public Click getCtrlDropClick() {
        return ctrlDropClick;
    }

    public static MenuIcon load(boolean alt, ConfigurationSection section, String id) {
        ConfigurationSection section1 = section.getConfigurationSection(id);
        if (section1 == null) {
            SweetInventory.getInstance().warn("预料中的错误: 找不到键 " + id);
            return null;
        }
        List<Character> slots = loadSlots(alt, section1);
        if (slots.isEmpty()) {
            SweetInventory.getInstance().warn("没有将图标 " + id + " 添加到布局的任意格子中");
            return null;
        }
        LoadedIcon icon = LoadedIcon.load(section, id);
        boolean needsUpdate = getBoolean(alt, section1, alt ? "需要更新" : "needs-update");
        int priorityLess = section1.getInt(alt ? "优先级_越小越优先" : "priority-less");
        List<IRequirement> viewRequirements = loadRequirements(alt, section1, alt ? "查看图标" : "view");
        List<IAction> viewDenyCommands = loadActions(section1, alt ? "查看图标.不满足需求执行" : "view.deny-commands");
        Click leftClick = Click.load(alt, section1, alt ? "左键点击" : "left-click");
        Click rightClick = Click.load(alt, section1,alt ? "右键点击" :  "right-click");
        Click shiftLeftClick = Click.load(alt, section1, alt ? "Shift左键点击" : "shift-left-click");
        Click shiftRightClick = Click.load(alt, section1, alt ? "Shift右键点击" : "shift-right-click");
        Click dropClick = Click.load(alt, section1, alt ? "Q键点击" : "drop-click");
        Click ctrlDropClick = Click.load(alt, section1, alt ? "Ctrl+Q键点击" : "ctrl-drop-click");
        return new MenuIcon(icon, slots, needsUpdate, priorityLess, viewRequirements, viewDenyCommands, leftClick, rightClick, shiftLeftClick, shiftRightClick, dropClick, ctrlDropClick);
    }

    private static List<Character> loadSlots(boolean alt, ConfigurationSection section) {
        List<Character> list = new ArrayList<>();
        String slotKey = alt ? "格子" : "slot";
        String slotsKey = alt ? "格子" : "slots";
        String slotStr = section.isString(slotKey) ? section.getString(slotKey) : null;
        if (slotStr == null) {
            if (section.contains(slotsKey) && !section.isString(slotsKey)) {
                slotStr = section.getString(slotsKey);
            }
        }
        if (slotStr != null && !slotStr.isEmpty()) {
            char[] charArray = slotStr.toCharArray();
            for (char c : charArray) {
                list.add(c);
            }
        } else if (section.contains(slotsKey) && section.isList(slotsKey)) {
            list.addAll(section.getCharacterList(slotsKey));
        }
        return list;
    }
}
