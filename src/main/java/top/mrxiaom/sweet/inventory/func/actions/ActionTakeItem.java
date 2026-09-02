package top.mrxiaom.sweet.inventory.func.actions;

import com.google.common.base.Preconditions;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.api.IAction;
import top.mrxiaom.pluginbase.api.IActionProvider;
import top.mrxiaom.pluginbase.utils.ConfigUtils;
import top.mrxiaom.pluginbase.utils.Pair;
import top.mrxiaom.pluginbase.utils.Util;
import top.mrxiaom.pluginbase.utils.depend.PAPI;
import top.mrxiaom.sweet.inventory.SweetInventory;
import top.mrxiaom.sweet.inventory.api.ItemMatchContext;
import top.mrxiaom.sweet.inventory.api.ItemMatcher;
import top.mrxiaom.sweet.inventory.func.menus.MenuInstance;
import top.mrxiaom.sweet.inventory.requirements.IRequirement;
import top.mrxiaom.sweet.inventory.utils.ActionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ActionTakeItem implements IAction, IRequirement {
    public static final IActionProvider PROVIDER = new IActionProvider() {
        private final SweetInventory plugin = SweetInventory.getInstance();
        @Nullable
        @Override
        public IAction provide(@NotNull Object input) {
            if (input instanceof ConfigurationSection) {
                ConfigurationSection config = (ConfigurationSection) input;
                if ("take-item".equalsIgnoreCase(config.getString("type"))) {
                    String prefix = config.getString("prefix", "takeItem");
                    List<TakeItemEntry> items = new ArrayList<>();
                    List<ConfigurationSection> itemsList = new ArrayList<>();
                    add(itemsList, config, "item");
                    itemsList.addAll(ConfigUtils.getSectionList(config, "items"));

                    for (ConfigurationSection section : itemsList) {
                        String countStr = section.getString("count");
                        if (countStr == null) {
                            plugin.warn("take-item 中出现不正常的物品配置: count 的值无效");
                            continue;
                        }
                        List<ItemMatcher> matchers = new ArrayList<>();
                        List<ConfigurationSection> matchersList = new ArrayList<>();
                        add(matchersList, section, "matcher");
                        matchersList.addAll(ConfigUtils.getSectionList(section, "matchers"));
                        for (ConfigurationSection section1 : matchersList) {
                            ItemMatcher itemMatcher = plugin.parseItemMatcher(section1);
                            if (itemMatcher == null) {
                                plugin.warn("take-item 中出现不正常的物品配置: 存在无效的 matcher");
                                continue;
                            }
                            matchers.add(itemMatcher);
                        }
                        items.add(new TakeItemEntry(countStr, matchers));
                    }

                    List<IAction> denyCommands = ActionUtils.loadActions(config, "deny-commands");
                    return new ActionTakeItem(prefix, denyCommands, items, config.getBoolean("debug", false));
                }
            }
            return null;
        }
    };
    private static void add(List<ConfigurationSection> list, ConfigurationSection config, String key) {
        ConfigurationSection section = config.getConfigurationSection(key);
        if (section != null) {
            list.add(section);
        }
    }
    private final String prefix;
    private final List<IAction> denyCommands;
    private final List<TakeItemEntry> items;
    private final boolean enableDebug;
    private ActionTakeItem(String prefix, List<IAction> denyCommands, List<TakeItemEntry> items, boolean enableDebug) {
        this.prefix = prefix;
        this.denyCommands = denyCommands;
        this.items = items;
        this.enableDebug = enableDebug;
    }

    @NotNull
    public List<TakeItemEntry> items() {
        return items;
    }

    @Override
    public boolean check(MenuInstance menu, List<Pair<String, Object>> r) {
        Player player = menu.getPlayer();
        MatchResult result = resolve(player, r, enableDebug);
        if (result != null) {
            result.addReplacements(prefix, r);
            return result.leftover().isEmpty();
        } else {
            return false;
        }
    }

    @Override
    public void run(@Nullable Player player, @Nullable List<Pair<String, Object>> replacements) {
        if (player == null || replacements == null) return;
        MatchResult result = resolve(player, replacements);
        if (result != null && result.leftover().isEmpty()) {
            result.takeItems(player.getInventory());
        }
    }

    @Override
    public List<IAction> denyCommands() {
        return denyCommands;
    }

    /**
     * CraftInventory#first(item, withAmount:false)
     */
    private static int first(Player player, ItemStack[] inventory, Map<Integer, Integer> finalAmounts, TakeItemEntry.Inst adapter, List<Pair<String, Object>> r, boolean debug) {
        if (adapter == null) {
            return -1;
        } else {
            int i = 0;
            while (true) {
                if (i >= inventory.length) return -1;
                ItemStack item = inventory[i];
                int amount = item == null ? 0 : finalAmounts.getOrDefault(i, item.getAmount());
                if (item != null && amount > 0) {
                    ItemMatchContext ctx = new ItemMatchContext(player, item, amount, r, debug);
                    if (adapter.isMatch(ctx)) break;
                }
                ++i;
            }
            return i;
        }
    }

    @Nullable
    public MatchResult resolve(Player player, List<Pair<String, Object>> r) {
        return resolve(player, r, false);
    }

    @Nullable
    public MatchResult resolve(Player player, List<Pair<String, Object>> r, boolean debug) {
        List<TakeItemEntry.Inst> list = new ArrayList<>();
        for (TakeItemEntry item : items) {
            String countStr = Pair.replace(PAPI.setPlaceholders(player, item.countStr), r);
            int amount = Util.parseInt(countStr).orElse(0);
            if (amount <= 0) {
                if (debug) {
                    player.sendMessage("因为无法解析数量 " + countStr + " 为整数，匹配结果为 假");
                }
                return null;
            }
            list.add(item.new Inst(amount));
        }
        PlayerInventory inv = player.getInventory();
        ItemStack[] contents = inv.getContents();
        MatchResult result = new MatchResult(list);

        for (int i = 0; i < list.size(); ++i) {
            TakeItemEntry.Inst item = list.get(i);
            Preconditions.checkArgument(item != null, "ItemStack cannot be null");
            int amountToTake = item.requireAmount();

            while (true) {
                int slot = first(player, contents, result.finalAmounts(), item, r, debug);
                if (slot == -1) {
                    item.leftoverAmount(amountToTake);
                    result.leftover().put(i, item);
                    break;
                }

                ItemStack itemStack = contents[slot];
                if (itemStack == null) continue;
                int itemAmount = result.finalAmounts().getOrDefault(slot, itemStack.getAmount());
                int finalAmount;
                if (itemAmount <= amountToTake) {
                    finalAmount = 0;
                    amountToTake -= itemAmount;
                } else {
                    finalAmount = itemAmount - amountToTake;
                    amountToTake = 0;
                }
                result.finalAmounts().put(slot, finalAmount);
                if (amountToTake <= 0) break;
            }
        }
        return result;
    }

    public static class TakeItemEntry {
        private final String countStr;
        private final List<ItemMatcher> matchers;

        public TakeItemEntry(String countStr, List<ItemMatcher> matchers) {
            this.countStr = countStr;
            this.matchers = matchers;
        }

        @NotNull
        public String countStr() {
            return countStr;
        }

        @NotNull
        public List<ItemMatcher> matchers() {
            return matchers;
        }

        public boolean isMatch(ItemMatchContext ctx) {
            for (ItemMatcher matcher : matchers) {
                if (matcher.isItemMatch(ctx)) {
                    return true;
                }
            }
            return false;
        }

        public class Inst {
            private final int requireAmount;
            private int leftoverAmount;
            public Inst(int amount) {
                this.requireAmount = amount;
                this.leftoverAmount = 0;
            }

            public int requireAmount() {
                return requireAmount;
            }

            public int leftoverAmount() {
                return leftoverAmount;
            }

            public void leftoverAmount(int leftoverAmount) {
                this.leftoverAmount = leftoverAmount;
            }

            public boolean isMatch(ItemMatchContext ctx) {
                return TakeItemEntry.this.isMatch(ctx);
            }
        }
    }

    public static class MatchResult {
        private final List<TakeItemEntry.Inst> items;
        private final Map<Integer, TakeItemEntry.Inst> leftover = new HashMap<>();
        private final Map<Integer, Integer> finalAmounts = new HashMap<>();
        private MatchResult(List<TakeItemEntry.Inst> items) {
            this.items = items;
        }

        public void addReplacements(String prefix, List<Pair<String, Object>> r) {
            r.add(Pair.of("${" + prefix + ".leftover.count}", leftover.size()));
            for (int i = 0; i < items.size(); i++) {
                TakeItemEntry.Inst item = this.items.get(i);
                if (i == 0) {
                    addItemReplacements(prefix + ".item", item, r);
                }
                addItemReplacements(prefix + ".items[" + i + "]", item, r);
            }
        }

        private void addItemReplacements(String key, TakeItemEntry.Inst item, List<Pair<String, Object>> r) {
            r.add(Pair.of("${" + key + ".count}", item.requireAmount() - item.leftoverAmount()));
            r.add(Pair.of("${" + key + ".leftover}", item.leftoverAmount()));
            r.add(Pair.of("${" + key + ".require}", item.requireAmount()));
        }

        @NotNull
        public List<TakeItemEntry.Inst> items() {
            return items;
        }

        @NotNull
        public Map<Integer, TakeItemEntry.Inst> leftover() {
            return leftover;
        }

        @NotNull
        public Map<Integer, Integer> finalAmounts() {
            return finalAmounts;
        }

        public void takeItems(PlayerInventory inventory) {
            finalAmounts.forEach((slot, amount) -> {
                ItemStack item = inventory.getItem(slot);
                if (item != null) {
                    item.setAmount(amount);
                }
                inventory.setItem(slot, amount > 0 ? item : null);
            });
        }
    }
}
