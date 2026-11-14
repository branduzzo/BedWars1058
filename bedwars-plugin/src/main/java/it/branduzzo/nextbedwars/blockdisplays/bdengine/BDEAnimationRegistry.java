package it.branduzzo.nextbedwars.blockdisplays.bdengine;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class BDEAnimationRegistry {
    private static final Map<String, Map<String, Map<Integer, List<String>>>> ANIMATIONS = new ConcurrentHashMap<>();
    private static final Map<String, Map<String, Map<Integer, List<String>>>> SOUNDS = new ConcurrentHashMap<>();
    private static final Map<String, Map<String, Boolean>> LOOPS = new ConcurrentHashMap<>();
    private static long lastModified = 0;

    private BDEAnimationRegistry() {}

    public static void load(Plugin plugin) {
        File dir = new File(plugin.getDataFolder(), "custom-blocks");
        if (dir.exists() && dir.lastModified() == lastModified && !ANIMATIONS.isEmpty()) return;
        if (dir.exists()) lastModified = dir.lastModified();
        ANIMATIONS.clear();
        SOUNDS.clear();
        LOOPS.clear();
        if (!dir.exists() || !dir.isDirectory()) return;
        File[] files = dir.listFiles((d, n) -> n.endsWith(".yml"));
        if (files == null) return;
        for (File f : files) {
            String blockName = f.getName().replaceFirst("\\.yml$", "");
            YamlConfiguration cfg = YamlConfiguration.loadConfiguration(f);
            ConfigurationSection bd = cfg.getConfigurationSection("bdengine");
            if (bd == null) continue;
            ConfigurationSection animSec = bd.getConfigurationSection("animations");
            if (animSec != null) {
                Map<String, Map<Integer, List<String>>> blockAnims = new HashMap<>();
                Map<String, Boolean> blockLoops = new HashMap<>();
                for (String animName : animSec.getKeys(false)) {
                    ConfigurationSection anim = animSec.getConfigurationSection(animName);
                    if (anim == null) continue;
                    Map<Integer, List<String>> frames = new HashMap<>();
                    boolean loop = false;
                    for (String frameKey : anim.getKeys(false)) {
                        if (frameKey.equalsIgnoreCase("loop")) {
                            loop = anim.getBoolean(frameKey, false);
                            continue;
                        }
                        try {
                            int tick = Integer.parseInt(frameKey);
                            List<String> cmds = anim.getStringList(frameKey);
                            if (!cmds.isEmpty()) frames.put(tick, cmds);
                        } catch (NumberFormatException ignored) {}
                    }
                    if (!frames.isEmpty()) {
                        blockAnims.put(animName, frames);
                        blockLoops.put(animName, loop);
                    }
                }
                if (!blockAnims.isEmpty()) {
                    ANIMATIONS.put(blockName, blockAnims);
                    LOOPS.put(blockName, blockLoops);
                }
            }
            ConfigurationSection soundSec = bd.getConfigurationSection("sounds");
            if (soundSec != null) {
                Map<String, Map<Integer, List<String>>> blockSounds = new HashMap<>();
                for (String animName : soundSec.getKeys(false)) {
                    ConfigurationSection snd = soundSec.getConfigurationSection(animName);
                    if (snd == null) continue;
                    Map<Integer, List<String>> frames = new HashMap<>();
                    for (String frameKey : snd.getKeys(false)) {
                        if (frameKey.equalsIgnoreCase("loop")) continue;
                        try {
                            int tick = Integer.parseInt(frameKey);
                            List<String> cmds = snd.getStringList(frameKey);
                            if (!cmds.isEmpty()) frames.put(tick, cmds);
                        } catch (NumberFormatException ignored) {}
                    }
                    if (!frames.isEmpty()) blockSounds.put(animName, frames);
                }
                if (!blockSounds.isEmpty()) SOUNDS.put(blockName, blockSounds);
            }
        }
    }

    public static Map<Integer, List<String>> getAnimation(String blockName, String animName) {
        Map<String, Map<Integer, List<String>>> m = ANIMATIONS.get(blockName);
        if (m == null) return null;
        return m.get(animName);
    }

    public static boolean isLoop(String blockName, String animName) {
        Map<String, Boolean> m = LOOPS.get(blockName);
        if (m == null) return false;
        return m.getOrDefault(animName, false);
    }

    public static Map<Integer, List<String>> getSounds(String blockName, String animName) {
        Map<String, Map<Integer, List<String>>> m = SOUNDS.get(blockName);
        if (m == null) return null;
        return m.get(animName);
    }

    public static Map<String, Map<Integer, List<String>>> getAllAnimations(String blockName) {
        return ANIMATIONS.getOrDefault(blockName, Collections.emptyMap());
    }

    public static boolean hasAnimations(String blockName) {
        return ANIMATIONS.containsKey(blockName);
    }
}
