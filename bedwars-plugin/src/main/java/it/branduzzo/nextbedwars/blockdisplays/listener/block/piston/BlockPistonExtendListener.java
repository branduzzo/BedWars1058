package it.branduzzo.nextbedwars.blockdisplays.listener.block.piston;

import com.jeff_media.customblockdata.CustomBlockData;
import it.branduzzo.nextbedwars.blockdisplays.BlockDisplayCreator;
import it.branduzzo.nextbedwars.blockdisplays.custom.block.CustomBlockKey;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPistonExtendEvent;

import java.util.List;

public class BlockPistonExtendListener implements Listener {

    @EventHandler
    public void onBlockPistonExtend(BlockPistonExtendEvent event) {

        List<Block> blocks = event.getBlocks();

        for (Block block : blocks) {

            CustomBlockData customBlockData = new CustomBlockData(block, BlockDisplayCreator.getHost());

            if (!customBlockData.has(CustomBlockKey.NAME)) {
                continue;
            }

            event.setCancelled(true);
            return;
        }
    }
}
