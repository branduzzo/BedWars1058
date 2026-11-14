package it.branduzzo.nextbedwars.christmas;

import it.branduzzo.nextbedwars.BedWars;
import it.branduzzo.nextbedwars.api.arena.IArena;
import it.branduzzo.nextbedwars.util.Sched;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class PresentDrop {

    private static final Random RANDOM = new Random();

    private final IArena arena;
    private final Location target;
    private final String tag;
    private final List<Entity> model = new ArrayList<>();
    private UUID interactionId;
    private int age;
    private boolean opened;
    private org.bukkit.scheduler.BukkitTask fallTask;
    private org.bukkit.scheduler.BukkitTask particleTask;

    public PresentDrop(IArena arena) {
        this.arena = arena;
        this.target = findLanding(arena);
        this.tag = "nbwxmas_present_" + UUID.randomUUID().toString().substring(0, 8);
    }

    public boolean spawn() {
        if (target == null || target.getWorld() == null) {
            return false;
        }
        int height = Math.max(10, ChristmasManager.config().getInt(ChristmasConfig.DROP_HEIGHT));
        Location spawn = target.clone().add(0, height, 0);
        model.addAll(DisplayModel.spawn(spawn, "models/present.mcfunction", tag));
        if (model.isEmpty()) {
            spawnFallback(spawn);
        }
        if (model.isEmpty()) {
            return false;
        }
        Entity hitbox = target.getWorld().spawn(target.clone().add(0, 1, 0), Interaction.class);
        hitbox.addScoreboardTag(tag);
        Interaction interaction = (Interaction) hitbox;
        interaction.setInteractionWidth(2.0f);
        interaction.setInteractionHeight(2.0f);
        interaction.setResponsive(true);
        interactionId = hitbox.getUniqueId();
        animateFall(spawn.getY() - target.getY());
        birthdayCheck();
        return true;
    }

    private void spawnFallback(Location location) {
        if (location.getWorld() == null) {
            return;
        }
        org.bukkit.entity.BlockDisplay chest = location.getWorld().spawn(location, org.bukkit.entity.BlockDisplay.class);
        chest.setBlock(Material.CHEST.createBlockData());
        chest.setTransformation(new org.bukkit.util.Transformation(
                new org.joml.Vector3f(-0.45f, 0, -0.45f),
                new org.joml.AxisAngle4f(0, 0, 1, 0),
                new org.joml.Vector3f(0.9f, 0.9f, 0.9f),
                new org.joml.AxisAngle4f(0, 0, 1, 0)));
        chest.addScoreboardTag(tag);
        model.add(chest);
        org.bukkit.entity.BlockDisplay ribbon = location.getWorld().spawn(location.clone().add(0, 0.45, 0), org.bukkit.entity.BlockDisplay.class);
        ribbon.setBlock(Material.RED_STAINED_GLASS.createBlockData());
        ribbon.setTransformation(new org.bukkit.util.Transformation(
                new org.joml.Vector3f(-0.5f, 0, -0.1f),
                new org.joml.AxisAngle4f(0, 0, 1, 0),
                new org.joml.Vector3f(1.0f, 0.1f, 0.2f),
                new org.joml.AxisAngle4f(0, 0, 1, 0)));
        ribbon.addScoreboardTag(tag);
        model.add(ribbon);
    }

    private void animateFall(double distance) {
        double step = Math.max(0.5, ChristmasManager.config().getDouble(ChristmasConfig.DROP_FALL_SPEED));
        int ticks = (int) Math.ceil(distance / step);
        final int[] elapsed = {0};
        fallTask = Sched.runAtLocationTimer(BedWars.plugin, target, () -> {
            if (opened || elapsed[0] >= ticks) {
                if (interactionId != null) {
                    Entity interaction = BukkitEntity.find(target.getWorld(), interactionId);
                    if (interaction != null) {
                        interaction.teleport(target.clone().add(0, 1, 0));
                    }
                }
                if (fallTask != null) {
                    fallTask.cancel();
                }
                return;
            }
            elapsed[0]++;
            DisplayModel.moveDown(model, step);
        }, 1L, 1L);
    }

    private void birthdayCheck() {
        int lifetime = Math.max(30, ChristmasManager.config().getInt(ChristmasConfig.DROP_LIFETIME));
        Sched.runAtLocationLater(BedWars.plugin, target, () -> {
            if (!opened) {
                remove();
            }
        }, lifetime * 20L);
        particleTask = Sched.runAtLocationTimer(BedWars.plugin, target, () -> {
            age++;
            if (age % 20 == 0 && target.getWorld() != null) {
                target.getWorld().spawnParticle(ChristmasConfig.particle("SNOWFLAKE"),
                        target.clone().add(0, 1.2, 0), 6, 0.4, 0.4, 0.4, 0.01);
            }
        }, 20L, 20L);
    }

    public boolean isInteraction(UUID uuid) {
        return interactionId != null && interactionId.equals(uuid);
    }

    public boolean isModel(Entity entity) {
        for (String value : entity.getScoreboardTags()) {
            if (value.startsWith(tag)) {
                return true;
            }
        }
        return false;
    }

    public void open(Player player) {
        if (opened) {
            return;
        }
        opened = true;
        LootTable.open(player, arena, target.clone().add(0, 0.5, 0));
        remove();
    }

    public void remove() {
        opened = true;
        if (fallTask != null) {
            fallTask.cancel();
        }
        if (particleTask != null) {
            particleTask.cancel();
        }
        DisplayModel.remove(model);
        if (interactionId != null && target.getWorld() != null) {
            Entity entity = BukkitEntity.find(target.getWorld(), interactionId);
            if (entity != null) {
                entity.remove();
            }
        }
        model.clear();
    }

    public IArena getArena() {
        return arena;
    }

    public Location getTarget() {
        return target;
    }

    private static Location findLanding(IArena arena) {
        World world = BedWars.plugin.getServer().getWorld(arena.getWorldName());
        if (world == null && !arena.getTeams().isEmpty()) {
            world = arena.getTeams().get(0).getSpawn().getWorld();
        }
        if (world == null) {
            return null;
        }
        List<Location> candidates = new ArrayList<>();
        arena.getTeams().forEach(team -> {
            if (team.getSpawn() != null) {
                candidates.add(team.getSpawn());
            }
        });
        if (candidates.isEmpty()) {
            return null;
        }
        Location base = candidates.get(RANDOM.nextInt(candidates.size()));
        int radius = Math.max(4, arena.getIslandRadius());
        int x = base.getBlockX() + RANDOM.nextInt(radius * 2 + 1) - radius;
        int z = base.getBlockZ() + RANDOM.nextInt(radius * 2 + 1) - radius;
        int y = world.getHighestBlockYAt(x, z);
        Block block = world.getBlockAt(x, y, z);
        if (block.isEmpty() || block.isLiquid()) {
            y = base.getBlockY();
        }
        return new Location(world, x + 0.5, y + 1, z + 0.5);
    }

    private static final class BukkitEntity {
        private static Entity find(World world, UUID uuid) {
            for (Entity entity : world.getEntities()) {
                if (entity.getUniqueId().equals(uuid)) {
                    return entity;
                }
            }
            return null;
        }
    }
}
