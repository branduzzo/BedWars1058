package it.branduzzo.nextbedwars.blockdisplays.listener.block;

import com.jeff_media.customblockdata.CustomBlockData;
import it.branduzzo.nextbedwars.blockdisplays.BlockDisplayCreator;
import it.branduzzo.nextbedwars.blockdisplays.common.BDCDependentPluginsManager;
import it.branduzzo.nextbedwars.blockdisplays.common.DeprecatedFeatureAdapter;
import it.branduzzo.nextbedwars.blockdisplays.custom.block.CustomBlock;
import it.branduzzo.nextbedwars.blockdisplays.custom.block.CustomBlockKey;
import it.branduzzo.nextbedwars.blockdisplays.custom.block.CustomBlockPermissions;
import it.branduzzo.nextbedwars.blockdisplays.custom.block.CustomBlockService;
import it.branduzzo.nextbedwars.blockdisplays.custom.block.option.CustomBlockBreakOption;
import it.branduzzo.nextbedwars.blockdisplays.custom.block.option.CustomBlockOption;
import it.branduzzo.nextbedwars.blockdisplays.file.config.value.BooleanConfigValue;
import it.branduzzo.nextbedwars.blockdisplays.file.config.value.StringMessagesValue;
import it.branduzzo.nextbedwars.blockdisplays.permission.DefaultPermissions;
import it.branduzzo.nextbedwars.blockdisplays.service.ServiceManager;
import it.branduzzo.nextbedwars.blockdisplays.util.ChatUtil;
import it.branduzzo.nextbedwars.blockdisplays.world.guard.WGRegionAccessChecker;
import it.branduzzo.nextbedwars.blockdisplays.world.guard.WGRegionFlags;
import it.branduzzo.nextbedwars.blockdisplays.world.skyblock.superior.SSBIslandAccessChecker;
import it.branduzzo.nextbedwars.blockdisplays.world.skyblock.superior.SSBIslandPrivileges;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.persistence.PersistentDataType;

public class BlockBreakListener implements Listener {

    private final ServiceManager<String, CustomBlockService> serviceManager;

    public BlockBreakListener(ServiceManager<String, CustomBlockService> serviceManager) {
        this.serviceManager = serviceManager;
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        Location blockLocation = block.getLocation();
        CustomBlockData customBlockData = new CustomBlockData(block, BlockDisplayCreator.getHost());
        String blockName = customBlockData.get(CustomBlockKey.NAME, PersistentDataType.STRING);

        if (blockName == null)
            return;

        if (!BooleanConfigValue.BLOCKS_DESTRUCTION) {
            if (!player.hasPermission(DefaultPermissions.BDC.CustomBlock.BLOCKS_DESTRUCTION)) {
                ChatUtil.sendMessage(player, StringMessagesValue.PERMISSION_DENIED_BREAK);
                event.setCancelled(true);
                return;
            }
        }


        String serviceClassName = customBlockData.get(CustomBlockKey.SERVICE_CLASS, PersistentDataType.STRING);

        serviceClassName = DeprecatedFeatureAdapter.checkMissingServiceClass(serviceClassName);

        CustomBlockService customBlockService = serviceManager.getService(serviceClassName);

        CustomBlock customBlock = customBlockService.getCustomBlock(blockLocation);

        if (customBlock == null) {
            ChatUtil.sendMessage(player, StringMessagesValue.CUSTOM_BLOCK_NOT_FOUND.replace("%customblock_name%", blockName));
            event.setCancelled(true);
            return;
        }

        if (!checkAccess(player, customBlock)) {
            event.setCancelled(true);
            return;
        }


        event.setDropItems(false);

        CustomBlockOption[] options = new CustomBlockOption[5];

        if (!(player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR)) {
            options[0] = CustomBlockBreakOption.DROP_ITEM;
        }

        try {
            customBlockService.breakBlock(customBlock, player, options);
        } catch (IllegalArgumentException ignore) {
        }
    }

    private static boolean checkAccess(Player player, CustomBlock customBlock) {
        BDCDependentPluginsManager dependentPluginManager = BlockDisplayCreator.getInstance().getDependentPluginsManager();
        Location customBlockLocation = customBlock.getLocation();

        if (dependentPluginManager.isWorldGuardAvailable()) {
            if (!WGRegionAccessChecker.checkRegionAccess(customBlockLocation, WGRegionFlags.BREAK_CB, player)) {
                ChatUtil.sendMessage(player, StringMessagesValue.REGION_DENIED_BREAK);
                return false;
            }
        }

        if (dependentPluginManager.isSuperiorSkyblockAvailable()) {
            if (!SSBIslandAccessChecker.checkIslandAccess(customBlockLocation, SSBIslandPrivileges.BREAK_CB, player)) {
                ChatUtil.sendMessage(player, StringMessagesValue.ISLAND_DENIED_BREAK);
                return false;
            }
        }

        CustomBlockPermissions permissions = customBlock.getPermissions();

        if (permissions != null) {
            if (!permissions.hasPermissions(player, CustomBlockPermissions.Type.BREAK)) {
                ChatUtil.sendMessage(player, StringMessagesValue.PERMISSION_DENIED_BREAK);
                return false;
            }
        }
        return true;
    }
}
