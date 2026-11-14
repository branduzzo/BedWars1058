package it.branduzzo.nextbedwars.christmas;

import it.branduzzo.nextbedwars.BedWars;
import it.branduzzo.nextbedwars.api.arena.GameState;
import it.branduzzo.nextbedwars.api.arena.IArena;
import it.branduzzo.nextbedwars.api.arena.team.ITeam;
import it.branduzzo.nextbedwars.arena.Arena;
import it.branduzzo.nextbedwars.util.Sched;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public final class ChristmasManager {

    private static ChristmasConfig config;
    private static Plugin plugin;

    private static final Map<String, PresentDrop> PRESENTS = new ConcurrentHashMap<>();
    private static final Map<String, Long> LAST_DROP = new ConcurrentHashMap<>();
    private static final List<ChristmasTree> TREES = new CopyOnWriteArrayList<>();
    private static final Set<UUID> ARMED_TRAPS = ConcurrentHashMap.newKeySet();
    private static final Set<UUID> ITEM_IDS = ConcurrentHashMap.newKeySet();

    private ChristmasManager() {
    }

    public static void init(Plugin owner) {
        plugin = owner;
        config = new ChristmasConfig();
        config.applyTheme();
        Bukkit.getPluginManager().registerEvents(new ChristmasListener(), owner);
        startTicker();
        owner.getLogger().info("Christmas event loaded.");
    }

    public static void reload() {
        if (config == null) {
            return;
        }
        config.reload();
        config.applyTheme();
        BedWars.plugin.getLogger().info("Christmas configuration reloaded.");
    }

    public static ChristmasConfig config() {
        return config;
    }

    public static boolean enabled() {
        return config != null && config.enabled();
    }

    private static void startTicker() {
        Sched.runTaskTimer(plugin, () -> {
            if (!enabled()) {
                return;
            }
            for (IArena arena : Arena.getArenas()) {
                org.bukkit.Location anchor = anchor(arena);
                if (anchor == null) {
                    continue;
                }
                Sched.runAtLocation(plugin, anchor, () -> tickArena(arena));
            }
        }, 100L, 100L);
    }

    private static org.bukkit.Location anchor(IArena arena) {
        if (arena.getSpectatorLocation() != null) {
            return arena.getSpectatorLocation();
        }
        if (arena.getWaitingLocation() != null) {
            return arena.getWaitingLocation();
        }
        for (ITeam team : arena.getTeams()) {
            if (team.getSpawn() != null) {
                return team.getSpawn();
            }
        }
        return arena.getWorld() == null ? null : arena.getWorld().getSpawnLocation();
    }

    private static void tickArena(IArena arena) {
        if (!config.getBoolean(ChristmasConfig.DROP_ENABLED)) {
            return;
        }
        if (arena.getStatus() != GameState.playing) {
            cleanup(arena);
            return;
        }
        if (arena.getPlayers().size() < config.getInt(ChristmasConfig.DROP_MIN_PLAYERS)) {
            return;
        }
        String key = arena.getArenaName();
        if (PRESENTS.containsKey(key)) {
            return;
        }
        long interval = Math.max(30, config.getInt(ChristmasConfig.DROP_INTERVAL)) * 1000L;
        long now = System.currentTimeMillis();
        if (now - LAST_DROP.getOrDefault(key, 0L) < interval) {
            return;
        }
        PresentDrop drop = new PresentDrop(arena);
        if (drop.spawn()) {
            PRESENTS.put(key, drop);
            LAST_DROP.put(key, now);
        }
    }

    public static void cleanup(IArena arena) {
        PRESENTS.entrySet().removeIf(entry -> {
            if (entry.getValue().getArena().equals(arena)) {
                entry.getValue().remove();
                return true;
            }
            return false;
        });
        for (ChristmasTree tree : TREES) {
            if (tree.getArena().equals(arena)) {
                tree.remove();
            }
        }
        ARMED_TRAPS.removeIf(identity -> arena.getTeams().stream().noneMatch(team -> team.getIdentity().equals(identity)));
        if (arena.getWorld() != null) {
            for (Entity entity : arena.getWorld().getEntities()) {
                if (entity.getScoreboardTags().contains(SnowmanGuard.TAG) || entity.getScoreboardTags().stream().anyMatch(tag -> tag.startsWith("nbwxmas_"))) {
                    entity.remove();
                }
            }
        }
    }

    public static PresentDrop present(UUID interactionId) {
        for (PresentDrop drop : PRESENTS.values()) {
            if (drop.isInteraction(interactionId)) {
                return drop;
            }
        }
        return null;
    }

    public static void openPresent(UUID interactionId, Player player) {
        PresentDrop drop = present(interactionId);
        if (drop == null) {
            return;
        }
        PRESENTS.remove(drop.getArena().getArenaName());
        drop.open(player);
    }

    public static ChristmasTree tree(UUID interactionId) {
        for (ChristmasTree tree : TREES) {
            if (tree.isInteraction(interactionId)) {
                return tree;
            }
        }
        return null;
    }

    public static ChristmasTree spawnTree(IArena arena, ITeam team) {
        for (ChristmasTree tree : TREES) {
            if (tree.getTeam().equals(team)) {
                tree.remove();
            }
        }
        ChristmasTree tree = new ChristmasTree(arena, team);
        tree.spawn();
        TREES.add(tree);
        return tree;
    }

    public static void unregisterTree(ChristmasTree tree) {
        TREES.remove(tree);
    }

    public static void armTrap(ITeam team) {
        ARMED_TRAPS.add(team.getIdentity());
    }

    public static boolean consumeTrap(ITeam team) {
        return ARMED_TRAPS.remove(team.getIdentity());
    }

    public static boolean hasTrap(ITeam team) {
        return ARMED_TRAPS.contains(team.getIdentity());
    }

    public static NamespacedKey itemKey() {
        return new NamespacedKey(plugin, "christmas_item");
    }

    public static void tagItem(ItemStack stack, String id) {
        if (stack == null || stack.getItemMeta() == null) {
            return;
        }
        ItemMeta meta = stack.getItemMeta();
        meta.getPersistentDataContainer().set(itemKey(), PersistentDataType.STRING, id);
        stack.setItemMeta(meta);
    }

    public static String itemId(ItemStack stack) {
        if (stack == null || !stack.hasItemMeta()) {
            return null;
        }
        return stack.getItemMeta().getPersistentDataContainer().get(itemKey(), PersistentDataType.STRING);
    }

    public static void removeOneLater(Player player, String material) {
        Sched.runTaskLater(plugin, () -> {
            for (ItemStack stack : player.getInventory().getContents()) {
                if (stack == null || stack.getType().isAir()) {
                    continue;
                }
                if (!stack.getType().name().equalsIgnoreCase(material)) {
                    continue;
                }
                if (stack.getAmount() > 1) {
                    stack.setAmount(stack.getAmount() - 1);
                } else {
                    player.getInventory().removeItem(stack);
                }
                break;
            }
            player.updateInventory();
        }, 1L);
    }

    public static void tagLater(Player player, String material, String id) {
        Sched.runTaskLater(plugin, () -> {
            for (ItemStack stack : player.getInventory().getContents()) {
                if (stack == null || stack.getType().isAir()) {
                    continue;
                }
                if (itemId(stack) != null) {
                    continue;
                }
                if (!stack.getType().name().equalsIgnoreCase(material)) {
                    continue;
                }
                tagItem(stack, id);
                break;
            }
            player.updateInventory();
        }, 1L);
    }

    public static void spawnDropFor(IArena arena) {
        if (arena == null || arena.getStatus() != GameState.playing) {
            return;
        }
        PresentDrop drop = new PresentDrop(arena);
        if (drop.spawn()) {
            PresentDrop previous = PRESENTS.put(arena.getArenaName(), drop);
            if (previous != null) {
                previous.remove();
            }
            LAST_DROP.put(arena.getArenaName(), System.currentTimeMillis());
        }
    }

    public static Map<String, PresentDrop> presents() {
        return new HashMap<>(PRESENTS);
    }

    public static List<ChristmasTree> trees() {
        return TREES;
    }

    public static Entity findEntity(org.bukkit.World world, UUID uuid) {
        for (Entity entity : world.getEntities()) {
            if (entity.getUniqueId().equals(uuid)) {
                return entity;
            }
        }
        return null;
    }
}
