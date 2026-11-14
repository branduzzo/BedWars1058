package it.branduzzo.nextbedwars.support.paper;

import it.branduzzo.nextbedwars.api.configuration.ConfigPath;
import io.papermc.lib.PaperLib;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.event.player.PlayerTeleportEvent;

import static it.branduzzo.nextbedwars.BedWars.config;
import static it.branduzzo.nextbedwars.BedWars.isPaper;

public final class TeleportManager {

    public static void teleport(Entity entity, Location location) {
        teleportC(entity, location, PlayerTeleportEvent.TeleportCause.PLUGIN);
    }

    public static void teleportC(Entity entity, Location location, PlayerTeleportEvent.TeleportCause cause) {
        if (isPaper && config.getBoolean(ConfigPath.GENERAL_CONFIGURATION_PERFORMANCE_PAPER_FEATURES)) {
            PaperLib.teleportAsync(entity, location, cause);
            return;
        }
        entity.teleport(location, cause);
    }

}
