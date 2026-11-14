package it.branduzzo.nextbedwars.blockdisplays.listener.skyblock.superior;

import com.bgsoftware.superiorskyblock.api.events.PluginInitializeEvent;
import it.branduzzo.nextbedwars.blockdisplays.BlockDisplayCreator;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class SuperiorSkyblockPluginInitializeListener implements Listener {
    @EventHandler
    public void onPluginInitialize(PluginInitializeEvent event) {
        BlockDisplayCreator.getInstance().getDependentPluginsManager().registerSkyblockFlags();
    }
}
