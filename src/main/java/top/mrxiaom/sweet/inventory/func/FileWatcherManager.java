package top.mrxiaom.sweet.inventory.func;

import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.apache.commons.io.monitor.FileEntry;
import org.bukkit.configuration.MemoryConfiguration;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.func.AutoRegister;
import top.mrxiaom.sweet.inventory.SweetInventory;

import java.io.File;
import java.util.List;

@AutoRegister
public class FileWatcherManager extends AbstractModule {
    private FileAlterationMonitor monitor;
    public FileWatcherManager(SweetInventory plugin) {
        super(plugin);
    }

    @Override
    public int priority() {
        return 1001;
    }

    @Override
    public void reloadConfig(MemoryConfiguration config) {
        disableWatcher();
        if (config.getBoolean("file-watcher.enable", false)) {
            long interval = config.getLong("file-watcher.interval", 1000L);
            MenuManager manager = MenuManager.inst();
            List<File> folders = manager.getMenuFolders();
            try {
                FileAlterationObserver[] observers = new FileAlterationObserver[folders.size()];
                for (int i = 0; i < folders.size(); i++) {
                    File folder = folders.get(i);
                    FileAlterationObserver observer = FileAlterationObserver.builder().setRootEntry(new FileEntry(folder)).get();
                    observer.addListener(new FileListener(manager, folder));
                    observers[i] = observer;
                }
                FileAlterationMonitor monitor = new FileAlterationMonitor(interval, observers);
                monitor.start();
                this.monitor = monitor;
            } catch (Exception e) {
                warn("无法启动文件变更监听器", e);
            }
        }
    }

    public static class FileListener extends FileAlterationListenerAdaptor {
        private final MenuManager manager;
        private final File folder;
        protected FileListener(MenuManager manager, File folder) {
            this.manager = manager;
            this.folder = folder;
        }
        @Nullable
        private String getId(File file) {
            return MenuManager.getRelationPath(folder, file);
        }

        @Override
        public void onFileCreate(File file) {
            if (file.length() > 16) {
                String id = getId(file);
                if (id != null) {
                    manager.updateConfig(id, file);
                }
            }
        }

        @Override
        public void onFileChange(File file) {
            String id = getId(file);
            if (id != null) {
                manager.updateConfig(id, file);
            }
        }

        @Override
        public void onFileDelete(File file) {
            String id = getId(file);
            if (id != null) {
                manager.removeConfig(id, true);
            }
        }
    }

    private void disableWatcher() {
        FileAlterationMonitor monitor = this.monitor;
        if (monitor != null) {
            this.monitor = null;
            try {
                monitor.stop();
            } catch (Exception ignored) {}
        }
    }

    @Override
    public void onDisable() {
        disableWatcher();
    }
}
