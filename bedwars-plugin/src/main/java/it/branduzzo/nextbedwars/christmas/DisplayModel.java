package it.branduzzo.nextbedwars.christmas;

import it.branduzzo.nextbedwars.BedWars;
import it.branduzzo.nextbedwars.util.Sched;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class DisplayModel {

    private DisplayModel() {
    }

    public static List<Entity> spawn(Location location, String resource, String tag) {
        List<Entity> spawned = new ArrayList<>();
        if (location.getWorld() == null) {
            return spawned;
        }
        List<String> commands = readResource(resource);
        if (commands.isEmpty()) {
            return spawned;
        }
        String position = String.format(Locale.US, "%.3f %.3f %.3f",
                location.getX(), location.getY(), location.getZ());
        for (String command : commands) {
            String line = command.replace("presentchristmas", tag).replace("project", tag);
            try {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "execute positioned " + position + " run " + line);
            } catch (Exception e) {
                BedWars.plugin.getLogger().warning("Could not run model command: " + e.getMessage());
                break;
            }
        }
        spawned.addAll(byTag(location.getWorld(), tag));
        return spawned;
    }

    private static List<String> readResource(String resource) {
        List<String> commands = new ArrayList<>();
        try (InputStream stream = BedWars.plugin.getResource(resource)) {
            if (stream == null) {
                return commands;
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String trimmed = line.trim();
                    if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                        continue;
                    }
                    commands.add(trimmed);
                }
            }
        } catch (Exception e) {
            BedWars.plugin.getLogger().warning("Could not read model " + resource + ": " + e.getMessage());
        }
        return commands;
    }

    public static List<Entity> byTag(World world, String tag) {
        List<Entity> entities = new ArrayList<>();
        for (Entity entity : world.getEntities()) {
            for (String value : entity.getScoreboardTags()) {
                if (value.equals(tag) || value.startsWith(tag + "_")) {
                    entities.add(entity);
                    break;
                }
            }
        }
        return entities;
    }

    public static void moveDown(List<Entity> entities, double amount) {
        for (Entity entity : entities) {
            if (entity.isValid()) {
                entity.teleport(entity.getLocation().add(0, -amount, 0));
            }
        }
    }

    public static void remove(List<Entity> entities) {
        for (Entity entity : entities) {
            if (entity.isValid()) {
                entity.remove();
            }
        }
    }

    public static void scheduleRemove(Location location, List<Entity> entities, long delay) {
        Sched.runAtLocationLater(BedWars.plugin, location, () -> remove(entities), delay);
    }

    public static boolean isModelEntity(Entity entity) {
        for (String tag : entity.getScoreboardTags()) {
            if (tag.startsWith("nbwxmas_")) {
                return true;
            }
        }
        return false;
    }
}
