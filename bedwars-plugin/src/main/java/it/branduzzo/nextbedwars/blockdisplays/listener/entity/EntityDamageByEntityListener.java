package it.branduzzo.nextbedwars.blockdisplays.listener.entity;

import it.branduzzo.nextbedwars.blockdisplays.BlockDisplayCreator;
import it.branduzzo.nextbedwars.blockdisplays.common.BDCDependentPluginsManager;
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
import org.bukkit.entity.Entity;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.Player;
import org.bukkit.entity.Shulker;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;


public class EntityDamageByEntityListener implements Listener {

    private final ServiceManager<String, CustomBlockService> serviceManager;

    public EntityDamageByEntityListener(ServiceManager<String, CustomBlockService> serviceManager) {
        this.serviceManager = serviceManager;
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player))
            return;

        if (player.getGameMode() == GameMode.ADVENTURE)
            return;

        Entity entity = event.getEntity();

        if (!(entity instanceof Interaction) && !(entity instanceof Shulker))
            return;

        String blockName = CustomBlockKey.holder(entity).getName();

        if (blockName == null)
            return;

        event.setCancelled(true);

        if (!BooleanConfigValue.BLOCKS_DESTRUCTION) {
            if (!player.hasPermission(DefaultPermissions.BDC.CustomBlock.BLOCKS_DESTRUCTION)) {
                ChatUtil.sendMessage(player, StringMessagesValue.PERMISSION_DENIED_BREAK);
                return;
            }
        }

        CustomBlockService customBlockService = CustomBlockService.getService(serviceManager, entity);

        CustomBlock customBlock = customBlockService.getCustomBlock(entity);

        if (customBlock == null) {
            ChatUtil.sendMessage(player, StringMessagesValue.CUSTOM_BLOCK_NOT_FOUND.replace("%customblock_name%", blockName));
            return;
        }

        if (!checkAccess(player, customBlock)) {
            return;
        }

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
