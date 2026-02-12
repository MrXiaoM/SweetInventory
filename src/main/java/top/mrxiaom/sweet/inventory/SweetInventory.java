package top.mrxiaom.sweet.inventory;
        
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import de.tr7zw.changeme.nbtapi.utils.MinecraftVersion;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import top.mrxiaom.pluginbase.BukkitPlugin;
import top.mrxiaom.pluginbase.func.LanguageManager;
import top.mrxiaom.pluginbase.paper.PaperFactory;
import top.mrxiaom.pluginbase.resolver.DefaultLibraryResolver;
import top.mrxiaom.pluginbase.utils.ClassLoaderWrapper;
import top.mrxiaom.pluginbase.utils.ConfigUtils;
import top.mrxiaom.pluginbase.utils.inventory.InventoryFactory;
import top.mrxiaom.pluginbase.utils.item.ItemEditor;
import top.mrxiaom.pluginbase.utils.scheduler.FoliaLibScheduler;
import top.mrxiaom.sweet.inventory.api.IMaterialProvider;
import top.mrxiaom.sweet.inventory.func.menus.MenuIcon;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class SweetInventory extends BukkitPlugin {
    public static SweetInventory getInstance() {
        return (SweetInventory) BukkitPlugin.getInstance();
    }

    public SweetInventory() throws Exception {
        super(options()
                .bungee(true)
                .adventure(true)
                .database(false)
                .reconnectDatabaseWhenReloadConfig(false)
                .scanIgnore("top.mrxiaom.sweet.inventory.libs")
        );
        this.scheduler = new FoliaLibScheduler(this);

        info("正在检查依赖库状态");
        File librariesDir = ClassLoaderWrapper.isSupportLibraryLoader
                ? new File("libraries")
                : new File(this.getDataFolder(), "libraries");
        DefaultLibraryResolver resolver = new DefaultLibraryResolver(getLogger(), librariesDir);

        YamlConfiguration overrideLibraries = ConfigUtils.load(resolve("./.override-libraries.yml"));
        for (String key : overrideLibraries.getKeys(false)) {
            resolver.getStartsReplacer().put(key, overrideLibraries.getString(key));
        }
        resolver.addResolvedLibrary(BuildConstants.RESOLVED_LIBRARIES);

        List<URL> libraries = resolver.doResolve();
        info("正在添加 " + libraries.size() + " 个依赖库到类加载器");
        for (URL library : libraries) {
            this.classLoader.addURL(library);
        }
    }

    public InventoryFactory getInventoryFactory() {
        return inventory;
    }

    @Override
    public @NotNull InventoryFactory initInventoryFactory() {
        return PaperFactory.createInventoryFactory();
    }

    @Override
    public @NotNull ItemEditor initItemEditor() {
        return PaperFactory.createItemEditor();
    }

    private final List<IMaterialProvider> materialRegistry = new ArrayList<>();

    public void registerMaterial(IMaterialProvider provider) {
        materialRegistry.add(provider);
        materialRegistry.sort(Comparator.comparingInt(IMaterialProvider::providerPriority));
    }

    public void unregisterMaterial(IMaterialProvider provider) {
        materialRegistry.remove(provider);
        materialRegistry.sort(Comparator.comparingInt(IMaterialProvider::providerPriority));
    }

    @NotNull
    public ItemStack parseMaterial(Player player, MenuIcon icon) {
        for (IMaterialProvider provider : materialRegistry) {
            ItemStack item = provider.parse(player, icon);
            if (item != null) {
                return item;
            }
        }
        return new ItemStack(Material.PAPER);
    }

    @Override
    protected void beforeLoad() {
        MinecraftVersion.replaceLogger(getLogger());
        MinecraftVersion.disableUpdateCheck();
        MinecraftVersion.disableBStats();
        MinecraftVersion.getVersion();
    }

    @Override
    protected void beforeEnable() {
        LanguageManager.inst()
                .setLangFile("messages.yml")
                .register(Messages.class)
                .register(Messages.Command.class)
                .reload();
    }

    @SuppressWarnings({"all"})
    public void connect(Player player, String server) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("Connect");
        out.writeUTF(server);

        player.sendPluginMessage(this, "BungeeCord", out.toByteArray());
    }

    @Override
    protected void afterEnable() {
        getLogger().info("SweetInventory 加载完毕");
    }
}
