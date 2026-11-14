package it.branduzzo.nextbedwars.christmas;

import it.branduzzo.nextbedwars.BedWars;
import it.branduzzo.nextbedwars.api.arena.IArena;
import it.branduzzo.nextbedwars.api.arena.team.ITeam;
import it.branduzzo.nextbedwars.api.language.Language;
import it.branduzzo.nextbedwars.api.language.Messages;
import it.branduzzo.nextbedwars.util.Sched;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Transformation;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ChristmasTree {

    private final IArena arena;
    private final ITeam team;
    private final Location location;
    private final List<Entity> displays = new ArrayList<>();
    private UUID interactionId;
    private int health;
    private int ticksLeft;
    private boolean removed;
    private org.bukkit.scheduler.BukkitTask auraTask;

    public ChristmasTree(IArena arena, ITeam team) {
        this.arena = arena;
        this.team = team;
        this.location = team.getSpawn().clone().add(0, 0, 0);
        this.health = Math.max(1, ChristmasManager.config().getInt(ChristmasConfig.TREE_HEALTH));
        this.ticksLeft = Math.max(20, ChristmasManager.config().getInt(ChristmasConfig.TREE_DURATION) * 20);
    }

    public void spawn() {
        if (location.getWorld() == null) {
            return;
        }
        build(location.clone().add(0, 0, 0));
        Interaction interaction = location.getWorld().spawn(location.clone().add(0, 1.5, 0), Interaction.class);
        interaction.setInteractionWidth(2.2f);
        interaction.setInteractionHeight(3.0f);
        interaction.setResponsive(true);
        interaction.addScoreboardTag("nbwxmas_tree_" + team.getIdentity());
        interactionId = interaction.getUniqueId();
        tick();
    }

    private void build(Location base) {
        Location trunk = base.clone();
        for (int i = 0; i < 3; i++) {
            spawnPart(Material.SPRUCE_FENCE, trunk.clone().add(0, i * 0.55, 0), 1.0f);
        }
        int layers = 6;
        for (int layer = 0; layer < layers; layer++) {
            double radius = 1.7 - layer * 0.24;
            int count = Math.max(3, (int) (radius * 7));
            double y = 1.2 + layer * 0.55;
            for (int i = 0; i < count; i++) {
                double angle = (2 * Math.PI / count) * i + (layer % 2 == 0 ? 0 : Math.PI / count);
                double x = Math.cos(angle) * radius;
                double z = Math.sin(angle) * radius;
                spawnPart(Material.SWEET_BERRY_BUSH, base.clone().add(x, y, z), 0.8f);
            }
        }
        spawnPart(Material.GOLD_BLOCK, base.clone().add(0, 4.9, 0), 0.45f);
        for (int i = 0; i < 8; i++) {
            double angle = (2 * Math.PI / 8) * i;
            spawnPart(Material.GLOWSTONE, base.clone().add(Math.cos(angle) * 1.25, 2.1, Math.sin(angle) * 1.25), 0.22f);
        }
    }

    private void spawnPart(Material material, Location location, float scale) {
        if (location.getWorld() == null) {
            return;
        }
        BlockDisplay display = location.getWorld().spawn(location, BlockDisplay.class);
        display.setBlock(material.createBlockData());
        display.setBillboard(Display.Billboard.FIXED);
        display.setTransformation(new Transformation(
                new Vector3f(-scale / 2, 0, -scale / 2),
                new AxisAngle4f(0, 0, 1, 0),
                new Vector3f(scale, scale, scale),
                new AxisAngle4f(0, 0, 1, 0)
        ));
        display.addScoreboardTag("nbwxmas_tree_" + team.getIdentity());
        displays.add(display);
    }

    private void tick() {
        auraTask = Sched.runAtLocationTimer(BedWars.plugin, location, () -> {
            if (removed) {
                return;
            }
            ticksLeft -= 20;
            if (ticksLeft <= 0) {
                remove();
                return;
            }
            applyAura();
        }, 20L, 20L);
    }

    private void applyAura() {
        int radius = Math.max(ChristmasManager.config().getInt(ChristmasConfig.TREE_RADIUS), arena.getIslandRadius());
        double damage = ChristmasManager.config().getDouble(ChristmasConfig.TREE_ENEMY_DAMAGE);
        double minHealth = ChristmasManager.config().getDouble(ChristmasConfig.TREE_ENEMY_MIN_HEALTH);
        List<String> allyEffects = ChristmasManager.config().getYml().getStringList(ChristmasConfig.TREE_ALLY_EFFECTS);

        for (Player player : arena.getPlayers()) {
            if (player.getWorld() != location.getWorld()) {
                continue;
            }
            if (player.getLocation().distanceSquared(location) > radius * radius) {
                continue;
            }
            if (team.isMember(player)) {
                for (String entry : allyEffects) {
                    String[] parts = entry.split(":");
                    PotionEffectType type = LootTable.effect(parts[0]);
                    int amplifier = parts.length > 1 ? parse(parts[1], 0) : 0;
                    player.addPotionEffect(new PotionEffect(type, 100, Math.max(0, amplifier), false, true, true));
                }
            } else if (damage > 0 && player.getHealth() > minHealth) {
                double target = Math.max(minHealth, player.getHealth() - damage);
                player.setHealth(Math.min(player.getHealth(), target));
                player.spawnParticle(ChristmasConfig.particle("SNOWFLAKE"),
                        player.getLocation().add(0, 1, 0), 10, 0.3, 0.5, 0.3, 0.01);
            }
        }
    }

    private static int parse(String value, int fallback) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    public boolean hit(Player damager, double damage) {
        if (removed || damager == null || team.isMember(damager)) {
            return false;
        }
        health -= (int) Math.max(1, Math.round(damage));
        damager.playSound(damager.getLocation(), org.bukkit.Sound.BLOCK_SNOW_BREAK, 1f, 1f);
        if (health <= 0) {
            String message = it.branduzzo.nextbedwars.api.util.Theme.apply(
                    ChristmasManager.config().getString("tree.destroyed-message"));
            for (Player member : team.getMembers()) {
                member.sendMessage(Language.getMsg(member, Messages.PREFIX) + message);
            }
            remove();
        }
        return true;
    }

    public boolean isInteraction(UUID uuid) {
        return interactionId != null && interactionId.equals(uuid);
    }

    public void remove() {
        removed = true;
        if (auraTask != null) {
            auraTask.cancel();
        }
        for (Entity entity : displays) {
            if (entity.isValid()) {
                entity.remove();
            }
        }
        displays.clear();
        if (interactionId != null && location.getWorld() != null) {
            for (Entity entity : location.getWorld().getEntities()) {
                if (entity.getUniqueId().equals(interactionId)) {
                    entity.remove();
                    break;
                }
            }
        }
        ChristmasManager.unregisterTree(this);
    }

    public IArena getArena() {
        return arena;
    }

    public ITeam getTeam() {
        return team;
    }

    public Location getLocation() {
        return location;
    }

    public int getHealth() {
        return health;
    }
}
