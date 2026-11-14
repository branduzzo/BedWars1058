package it.branduzzo.nextbedwars.christmas;

import it.branduzzo.nextbedwars.BedWars;
import it.branduzzo.nextbedwars.util.Sched;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public final class IceBridge {

    private IceBridge() {
    }

    public static void build(Player player) {
        if (!ChristmasManager.config().enabled() || !ChristmasManager.config().getBoolean(ChristmasConfig.BRIDGE_ENABLED)) {
            return;
        }
        Location start = player.getLocation().clone();
        int length = Math.max(3, ChristmasManager.config().getInt(ChristmasConfig.BRIDGE_LENGTH));
        double yaw = Math.toRadians(start.getYaw());
        int dx = (int) Math.round(-Math.sin(yaw));
        int dz = (int) Math.round(Math.cos(yaw));
        int y = start.getBlockY() - 1;

        List<Block> blocks = new ArrayList<>();
        for (int i = 1; i <= length; i++) {
            Block block = start.getWorld().getBlockAt(start.getBlockX() + dx * i, y, start.getBlockZ() + dz * i);
            if (!block.getType().isAir()) {
                continue;
            }
            block.setType(Material.ICE);
            blocks.add(block);
        }
        if (blocks.isEmpty()) {
            return;
        }
        int duration = Math.max(2, ChristmasManager.config().getInt(ChristmasConfig.BRIDGE_DURATION));
        int perSecond = Math.max(1, ChristmasManager.config().getInt(ChristmasConfig.BRIDGE_DECAY));
        int[] index = {0};
        Sched.runAtLocationTimer(BedWars.plugin, start, () -> {
            for (int i = 0; i < perSecond && index[0] < blocks.size(); i++, index[0]++) {
                Block block = blocks.get(index[0]);
                if (block.getType() == Material.ICE || block.getType() == Material.FROSTED_ICE) {
                    block.setType(Material.AIR);
                }
            }
        }, duration * 20L, 20L);
    }

    public static void convertFrosted(Block block) {
        if (block.getType() == Material.ICE) {
            block.setType(Material.FROSTED_ICE);
        }
    }
}
