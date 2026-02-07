package top.mrxiaom.sweet.inventory.func;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.InventoryView;
import top.mrxiaom.pluginbase.func.AutoRegister;
import top.mrxiaom.pluginbase.utils.inventory.BukkitInventoryFactory;
import top.mrxiaom.sweet.inventory.SweetInventory;
import top.mrxiaom.sweet.inventory.func.menus.MenuInstance;

import java.util.HashMap;
import java.util.Map;

/**
 * 当不支持 PaperInventory 时，通过 ProtocolLib 复原菜单标题 Component
 */
@AutoRegister(requirePlugins = {"ProtocolLib"})
public class AlternativeRichTitle extends AbstractModule implements Listener {
    Map<String, InventoryView> openedWindow = new HashMap<>();
    ProtocolManager protocolManager;
    boolean enable = false;
    public AlternativeRichTitle(SweetInventory plugin) {
        super(plugin);
        protocolManager = ProtocolLibrary.getProtocolManager();
        if (plugin.getInventoryFactory() instanceof BukkitInventoryFactory) {
            enable = true;
            protocolManager.addPacketListener(new PacketAdapter(
                    new PacketAdapter.AdapterParameteters()
                            .plugin(plugin).serverSide().optionAsync()
                            .types(PacketType.Play.Server.OPEN_WINDOW)
            ) {
                @Override
                public void onPacketSending(PacketEvent event) {
                    AlternativeRichTitle.this.onPacketSending(event);
                }
            });
        }
    }

    @Override
    public void onDisable() {
        if (protocolManager != null) {
            protocolManager.removePacketListeners(plugin);
        }
    }

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent e) {
        if (!enable) return;
        openedWindow.put(e.getPlayer().getName(), e.getView());
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent e) {
        if (!enable) return;
        openedWindow.remove(e.getPlayer().getName());
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        if (!enable) return;
        openedWindow.remove(e.getPlayer().getName());
    }

    public void onPacketSending(PacketEvent event) {
        InventoryView view = openedWindow.remove(event.getPlayer().getName());
        InventoryHolder holder = view == null ? null : view.getTopInventory().getHolder();
        if (holder instanceof MenuInstance) {
            PacketContainer packet = event.getPacket();
            Component title = ((MenuInstance) holder).title();
            WrappedChatComponent component = packet.getChatComponents().readSafely(0);
            component.setJson(GsonComponentSerializer.gson().serialize(title));
            packet.getChatComponents().writeSafely(0, component);
        }
    }
}
