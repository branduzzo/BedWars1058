package it.branduzzo.nextbedwars.blockdisplays.listener.block;

import com.jeff_media.customblockdata.CustomBlockData;
import it.branduzzo.nextbedwars.blockdisplays.BlockDisplayCreator;
import it.branduzzo.nextbedwars.blockdisplays.custom.block.CustomBlockKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockExplodeEvent;

public class BlockExplodeListener implements Listener {
    @EventHandler
    public void onBlockExplode(BlockExplodeEvent event) {
        event.blockList().removeIf(block -> {
            CustomBlockData customBlockData = new CustomBlockData(block, BlockDisplayCreator.getHost());

            return customBlockData.has(CustomBlockKey.NAME);
        });
    }
}
