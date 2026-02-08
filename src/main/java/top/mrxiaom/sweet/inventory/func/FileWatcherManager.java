package top.mrxiaom.sweet.inventory.func;

import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
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
            List<File> folders = Menus.inst().getMenuFolders();
            try {
                FileAlterationObserver[] observers = new FileAlterationObserver[folders.size()];
                for (int i = 0; i < folders.size(); i++) {
                    File folder = folders.get(i);
                    FileAlterationObserver observer = new FileAlterationObserver(folder);
                    observer.addListener(new FileListener(folder));
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
        private final File folder;
        protected FileListener(File folder) {
            this.folder = folder;
        }
        @Nullable
        private String getId(File file) {
            return Menus.getRelationPath(folder, file);
        }
        @Override
        public void onFileChange(File file) {
            String id = getId(file);
            if (id != null) {
                Menus.inst().updateConfig(id, file);
            }
        }

        @Override
        public void onFileDelete(File file) {
            String id = getId(file);
            if (id != null) {
                Menus.inst().removeConfig(id);
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
