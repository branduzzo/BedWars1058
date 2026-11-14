package it.branduzzo.nextbedwars.christmas;

import it.branduzzo.nextbedwars.BedWars;
import it.branduzzo.nextbedwars.util.Sched;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowman;

public final class SnowmanGuard {

    public static final String TAG = "nbwxmas_snowman";

    private SnowmanGuard() {
    }

    public static Snowman spawn(Player owner) {
        Location location = owner.getLocation().clone().add(0, 0.5, 0);
        Snowman snowman = owner.getWorld().spawn(location, Snowman.class);
        snowman.addScoreboardTag(TAG);
        snowman.setPersistent(false);
        snowman.setRemoveWhenFarAway(false);
        snowman.setDerp(true);
        if (snowman.getAttribute(Attribute.MAX_HEALTH) != null) {
            snowman.getAttribute(Attribute.MAX_HEALTH).setBaseValue(40);
            snowman.setHealth(40);
        }
        int lifetime = Math.max(30, ChristmasManager.config().getInt(ChristmasConfig.SNOWMAN_LIFETIME));
        Sched.runAtLocationLater(BedWars.plugin, location, () -> {
            if (snowman.isValid()) {
                snowman.remove();
            }
        }, lifetime * 20L);
        return snowman;
    }

    public static boolean isSnowman(Entity entity) {
        return entity.getScoreboardTags().contains(TAG);
    }
}
