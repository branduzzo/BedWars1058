package it.branduzzo.nextbedwars.blockdisplays.listener.packet;

import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPickItemFromBlock;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPickItemFromEntity;
import com.jeff_media.customblockdata.CustomBlockData;
import it.branduzzo.nextbedwars.blockdisplays.BlockDisplayCreator;
import it.branduzzo.nextbedwars.blockdisplays.custom.block.CustomBlock;
import it.branduzzo.nextbedwars.blockdisplays.custom.block.CustomBlockKey;
import it.branduzzo.nextbedwars.blockdisplays.custom.block.CustomBlockService;
import it.branduzzo.nextbedwars.blockdisplays.permission.DefaultPermissions;
import it.branduzzo.nextbedwars.blockdisplays.util.EntityUtil;
import it.branduzzo.nextbedwars.blockdisplays.util.ItemUtil;
import it.branduzzo.nextbedwars.blockdisplays.util.LocationUtil;
import it.branduzzo.nextbedwars.blockdisplays.util.FoliaScheduler;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.function.Supplier;

public final class PacketReceiveListener {

    public static PacketListener get() {
        return new PacketListener() {
            @Override
            public void onPacketReceive(PacketReceiveEvent event) {
                PacketTypeCommon packetType = event.getPacketType();

                CustomBlockService customBlockService = BlockDisplayCreator.getInstance().getCustomBlockService();

                if (packetType == PacketType.Play.Client.PICK_ITEM_FROM_ENTITY) {

                    WrapperPlayClientPickItemFromEntity pickItem = new WrapperPlayClientPickItemFromEntity(event);
                    Player player = event.getPlayer();

                    Entity entity = EntityUtil.getNearEntity(player.getLocation(), pickItem.getEntityId());

                    if (entity == null) {
                        return;
                    }

                    handleSync(event, () -> customBlockService.getCustomBlock(entity), entity.getPersistentDataContainer());

                } else if (packetType == PacketType.Play.Client.PICK_ITEM_FROM_BLOCK) {

                    WrapperPlayClientPickItemFromBlock pickItemFromBlock = new WrapperPlayClientPickItemFromBlock(event);
                    Player player = event.getPlayer();

                    Location blockLocation = LocationUtil.toLocation(pickItemFromBlock.getBlockPos(), player.getWorld());

                    handleSync(event, () -> customBlockService.getCustomBlock(blockLocation), new CustomBlockData(blockLocation.getBlock(), BlockDisplayCreator.getHost()));
                }
            }

            private void handleSync(PacketReceiveEvent event, Supplier<CustomBlock> blockSupplier, PersistentDataContainer blockContainer) {
                if (blockContainer.has(CustomBlockKey.NAME, PersistentDataType.STRING)) {
                    event.setCancelled(true);
                    Player p = (Player) event.getPlayer();
                    FoliaScheduler.runAtLocation(BlockDisplayCreator.getHost(), p.getLocation(), () -> {
                        CustomBlock block = blockSupplier.get();
                        selectCustomBlockItem(p, block);
                    });
                }
            }
        };
    }


    private static void selectCustomBlockItem(Player player, CustomBlock block) {
        ItemStack blockItem = block.getItem();

        ItemUtil.selectItem(player, stack -> {

            if (stack.getType() != blockItem.getType())
                return false;

            ItemMeta meta = stack.getItemMeta();

            if (meta == null) {
                return false;
            }

            String name = CustomBlockKey.holder(meta).getName();

            return name != null && name.equals(block.getName());
        }, blockItem, DefaultPermissions.BDC.QUICKSELECT_CREATIVE);
    }
}
