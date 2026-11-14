package it.branduzzo.nextbedwars.christmas;

import it.branduzzo.nextbedwars.BedWars;
import it.branduzzo.nextbedwars.api.configuration.ConfigManager;
import it.branduzzo.nextbedwars.api.language.Language;
import it.branduzzo.nextbedwars.api.util.Theme;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.potion.PotionEffectType;

import java.util.Arrays;
import java.util.List;

public class ChristmasConfig extends ConfigManager {

    public static final String ENABLED = "enabled";
    public static final String THEME_PRIMARY = "theme.primary";
    public static final String THEME_SECONDARY = "theme.secondary";
    public static final String THEME_ACCENT = "theme.accent";
    public static final String THEME_PREFIX = "theme.prefix";

    public static final String DROP_ENABLED = "loot-drop.enabled";
    public static final String DROP_INTERVAL = "loot-drop.interval-seconds";
    public static final String DROP_HEIGHT = "loot-drop.height";
    public static final String DROP_MIN_PLAYERS = "loot-drop.min-players";
    public static final String DROP_FALL_SPEED = "loot-drop.fall-blocks-per-tick";
    public static final String DROP_LIFETIME = "loot-drop.lifetime-seconds";

    public static final String SNOWMAN_ENABLED = "snowman.enabled";
    public static final String SNOWMAN_LIFETIME = "snowman.lifetime-seconds";

    public static final String TREE_ENABLED = "tree.enabled";
    public static final String TREE_DURATION = "tree.duration-seconds";
    public static final String TREE_HEALTH = "tree.health";
    public static final String TREE_RADIUS = "tree.radius";
    public static final String TREE_ALLY_EFFECTS = "tree.ally-effects";
    public static final String TREE_ENEMY_DAMAGE = "tree.enemy-damage";
    public static final String TREE_ENEMY_MIN_HEALTH = "tree.enemy-min-health";

    public static final String TRAP_ENABLED = "dream-trap.enabled";
    public static final String TRAP_DARKNESS = "dream-trap.darkness-seconds";
    public static final String TRAP_MESSAGE = "dream-trap.message";

    public static final String BRIDGE_ENABLED = "ice-bridge.enabled";
    public static final String BRIDGE_LENGTH = "ice-bridge.length";
    public static final String BRIDGE_DURATION = "ice-bridge.duration-seconds";
    public static final String BRIDGE_DECAY = "ice-bridge.decay-blocks-per-second";

    public ChristmasConfig() {
        super(BedWars.plugin, "christmas", BedWars.plugin.getDataFolder().getPath());

        var yml = getYml();
        yml.options().header("NextBedWars - Christmas event configuration");
        yml.options().copyDefaults(true);

        yml.addDefault(ENABLED, true);
        yml.addDefault(THEME_PRIMARY, "#00C853");
        yml.addDefault(THEME_SECONDARY, "#FFFFFF");
        yml.addDefault(THEME_ACCENT, "#D32F2F");
        yml.addDefault(THEME_PREFIX, "{primary}&lNEXT&f&lBEDWARS {accent}\u2744 {secondary}");

        yml.addDefault(DROP_ENABLED, true);
        yml.addDefault(DROP_INTERVAL, 300);
        yml.addDefault(DROP_HEIGHT, 45);
        yml.addDefault(DROP_MIN_PLAYERS, 2);
        yml.addDefault(DROP_FALL_SPEED, 1.0);
        yml.addDefault(DROP_LIFETIME, 180);

        yml.addDefault(SNOWMAN_ENABLED, true);
        yml.addDefault(SNOWMAN_LIFETIME, 300);

        yml.addDefault(TREE_ENABLED, true);
        yml.addDefault(TREE_DURATION, 180);
        yml.addDefault(TREE_HEALTH, 100);
        yml.addDefault(TREE_RADIUS, 6);
        yml.addDefault(TREE_ALLY_EFFECTS, Arrays.asList("STRENGTH:0", "SPEED:0", "RESISTANCE:0"));
        yml.addDefault(TREE_ENEMY_DAMAGE, 1.0);
        yml.addDefault(TREE_ENEMY_MIN_HEALTH, 1.0);
        yml.addDefault("tree.destroyed-message", "{accent}\u2744 {secondary}L'albero di Natale \u00e8 stato distrutto!");

        yml.addDefault(TRAP_ENABLED, true);
        yml.addDefault(TRAP_DARKNESS, 10);
        yml.addDefault(TRAP_MESSAGE, "{accent}\u2744 {secondary}Questo \u00e8 un sogno natalizio..");

        yml.addDefault(BRIDGE_ENABLED, true);
        yml.addDefault(BRIDGE_LENGTH, 10);
        yml.addDefault(BRIDGE_DURATION, 10);
        yml.addDefault(BRIDGE_DECAY, 1);

        yml.addDefault("loot.materials", Arrays.asList("IRON_INGOT:8", "GOLD_INGOT:4", "DIAMOND:2", "EMERALD:1",
                "SNOWBALL:16", "ARROW:12", "GOLDEN_APPLE:1", "OAK_LOG:8"));
        yml.addDefault("loot.shop-items", Arrays.asList("wool", "stone", "glass", "arrow", "speed-potion"));
        yml.addDefault("loot.potions", Arrays.asList("SPEED:200:1", "JUMP:200:1", "REGENERATION:120:0"));
        yml.addDefault("loot.effects", Arrays.asList("SPEED:200:1", "REGENERATION:100:0", "DAMAGE_RESISTANCE:100:0"));
        yml.addDefault("loot.max-rolls", 3);

        yml.addDefault("debug.default-arena", "");

        mergeDefaultsFirst();
    }

    public void applyTheme() {
        Theme.load(getString(THEME_PRIMARY), getString(THEME_SECONDARY), getString(THEME_ACCENT), getString(THEME_PREFIX));
        Language.getLanguages().forEach(language -> {
            language.setPrefix(Theme.getPrefix());
            language.setPrefixStatic(Theme.getPrefix());
        });
    }

    public boolean enabled() {
        return getBoolean(ENABLED);
    }

    public List<String> lootMaterials() {
        return getYml().getStringList("loot.materials");
    }

    public List<String> lootShopItems() {
        return getYml().getStringList("loot.shop-items");
    }

    public List<String> lootPotions() {
        return getYml().getStringList("loot.potions");
    }

    public List<String> lootEffects() {
        return getYml().getStringList("loot.effects");
    }

    public static Material material(String name) {
        Material material = Material.matchMaterial(name);
        return material == null ? Material.SNOWBALL : material;
    }

    public static PotionEffectType effect(String name) {
        PotionEffectType type = PotionEffectType.getByName(name);
        return type == null ? PotionEffectType.SPEED : type;
    }

    public static Particle particle(String name) {
        try {
            return Particle.valueOf(name);
        } catch (IllegalArgumentException e) {
            return Particle.SNOWFLAKE;
        }
    }
}
