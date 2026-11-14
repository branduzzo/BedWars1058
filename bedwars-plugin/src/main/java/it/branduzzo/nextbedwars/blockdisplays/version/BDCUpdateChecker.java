package it.branduzzo.nextbedwars.blockdisplays.version;


import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import it.branduzzo.nextbedwars.blockdisplays.util.ChatUtil;
import it.branduzzo.nextbedwars.blockdisplays.util.FoliaScheduler;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Scanner;
import java.util.function.Consumer;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BDCUpdateChecker {
    JavaPlugin plugin;
    int resourceId;
    String pluginVersion;
    @NonFinal
    String lastVersion;

    public BDCUpdateChecker(JavaPlugin plugin, int resourceId) {
        this.plugin = plugin;
        this.resourceId = resourceId;
        this.pluginVersion = plugin.getDescription().getVersion();
    }

    public void updateLastVersion(@NotNull Consumer<BDCUpdateChecker> onComplete) {
        FoliaScheduler.runAsync(this.plugin, () -> {
            try (InputStream is = new URL("https://api.spigotmc.org/legacy/update.php?resource=" + this.resourceId + "/~").openStream(); Scanner scann = new Scanner(is)) {
                if (scann.hasNext()) {
                    this.lastVersion = scann.next();
                    onComplete.accept(this);
                }
            } catch (IOException e) {
                ChatUtil.log("Unable to check for updates: " + e.getMessage());
            }
        });
    }

    public boolean hasNewerVersion() {
        return this.lastVersion != null && !this.pluginVersion.equalsIgnoreCase(this.lastVersion);
    }

    public void sendNewUpdateMessage() {
        if (this.hasNewerVersion()) {
            ChatUtil.log("&6[NextBedWars blocks] &eA new version of plugin is available: &a" + this.lastVersion + "&e. You are currently using version &a" + this.pluginVersion + "&e.");
        }
    }
}
