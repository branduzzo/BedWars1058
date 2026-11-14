package it.branduzzo.nextbedwars.blockdisplays.bdengine;

import it.branduzzo.nextbedwars.blockdisplays.util.FoliaScheduler;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.SoundCategory;
import org.bukkit.World;
import org.bukkit.entity.Display;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Transformation;
import org.joml.AxisAngle4f;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class BDEAnimationPlayer {
    private static final Map<String, Boolean> ACTIVE = new ConcurrentHashMap<>();
    private static final Pattern TAG_PATTERN = Pattern.compile("tag=([^,\\]]+)");
    private static final Pattern TRANSFORMATION_PATTERN = Pattern.compile("transformation:\\[([^\\]]+)\\]");
    private static final Pattern DURATION_PATTERN = Pattern.compile("interpolation_duration:(\\d+)");
    private static final Pattern DELAY_PATTERN = Pattern.compile("start_interpolation:(\\d+)");
    private static final Pattern PLAYSOUND_PATTERN = Pattern.compile("playsound\\s+([^\\s]+)\\s+([^\\s]+)\\s+@a.*?(\\d+(?:\\.\\d+)?)\\s+(\\d+(?:\\.\\d+)?)\\s*$");

    private BDEAnimationPlayer() {}

    private static String key(Location loc, String blockName, String animName) {
        return loc.getWorld().getUID() + ";" + loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ() + ";" + blockName + ";" + animName;
    }

    public static void play(Plugin plugin, Location loc, String blockName, String animName) {
        play(plugin, loc, blockName, animName, BDEAnimationRegistry.isLoop(blockName, animName));
    }

    public static void play(Plugin plugin, Location loc, String blockName, String animName, boolean loop) {
        if (ACTIVE.size() > 100) return;
        try {
            java.lang.reflect.Method m = Bukkit.class.getMethod("getTPS");
            double[] tps = (double[]) m.invoke(null);
            if (tps.length > 0 && tps[0] < 17.0) return;
        } catch (Exception ignored) {}
        String k = key(loc, blockName, animName);
        if (ACTIVE.containsKey(k)) return;
        ACTIVE.put(k, true);
        playInternal(plugin, loc, blockName, animName, loop, k);
    }

    private static void playInternal(Plugin plugin, Location loc, String blockName, String animName, boolean loop, String key) {
        Map<Integer, List<String>> frames = BDEAnimationRegistry.getAnimation(blockName, animName);
        Map<Integer, List<String>> soundFrames = BDEAnimationRegistry.getSounds(blockName, animName);
        if ((frames == null || frames.isEmpty()) && (soundFrames == null || soundFrames.isEmpty())) {
            ACTIVE.remove(key);
            return;
        }
        if (frames == null) frames = Map.of();
        int maxTick = 0;
        for (int t : frames.keySet()) if (t > maxTick) maxTick = t;
        if (soundFrames != null) for (int t : soundFrames.keySet()) if (t > maxTick) maxTick = t;
        for (Map.Entry<Integer, List<String>> e : frames.entrySet()) {
            int tick = e.getKey();
            List<String> cmds = e.getValue();
            FoliaScheduler.runAtLocationDelayed(plugin, loc, () -> {
                if (!ACTIVE.containsKey(key)) return;
                for (String cmd : cmds) executeDataMerge(loc, cmd);
            }, tick);
        }
        if (soundFrames != null) {
            for (Map.Entry<Integer, List<String>> e : soundFrames.entrySet()) {
                int tick = e.getKey();
                List<String> cmds = e.getValue();
                FoliaScheduler.runAtLocationDelayed(plugin, loc, () -> {
                    if (!ACTIVE.containsKey(key)) return;
                    for (String cmd : cmds) executePlaySound(loc, cmd);
                }, tick);
            }
        }
        FoliaScheduler.runAtLocationDelayed(plugin, loc, () -> {
            if (!loop) {
                ACTIVE.remove(key);
                return;
            }
            if (!ACTIVE.containsKey(key)) return;
            if (!BDEAnimationRegistry.hasAnimations(blockName)) {
                ACTIVE.remove(key);
                return;
            }
            playInternal(plugin, loc, blockName, animName, true, key);
        }, maxTick + 2);
    }

    public static void stop(Location loc, String blockName, String animName) {
        ACTIVE.remove(key(loc, blockName, animName));
    }

    public static void stopAll(Location loc, String blockName) {
        ACTIVE.keySet().removeIf(k -> k.contains(";" + blockName + ";") && k.startsWith(loc.getWorld().getUID() + ";" + loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ()));
    }

    public static void stopAll(Location loc) {
        String prefix = loc.getWorld().getUID() + ";" + loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ() + ";";
        ACTIVE.keySet().removeIf(k -> k.startsWith(prefix));
    }

    public static void playSound(Plugin plugin, Location loc, String blockName, String animName) {
        playSound(plugin, loc, blockName, animName, false);
    }

    public static void playSound(Plugin plugin, Location loc, String blockName, String animName, boolean loop) {
        if (ACTIVE.size() > 100) return;
        String k = key(loc, blockName, animName) + ":sound";
        if (ACTIVE.containsKey(k)) return;
        ACTIVE.put(k, true);
        playSoundInternal(plugin, loc, blockName, animName, loop, k);
    }

    public static void clearAll() {
        ACTIVE.clear();
    }

    private static void playSoundInternal(Plugin plugin, Location loc, String blockName, String animName, boolean loop, String key) {
        Map<Integer, List<String>> soundFrames = BDEAnimationRegistry.getSounds(blockName, animName);
        if (soundFrames == null || soundFrames.isEmpty()) {
            ACTIVE.remove(key);
            return;
        }
        int maxTick = 0;
        for (int t : soundFrames.keySet()) if (t > maxTick) maxTick = t;
        for (Map.Entry<Integer, List<String>> e : soundFrames.entrySet()) {
            int tick = e.getKey();
            List<String> cmds = e.getValue();
            FoliaScheduler.runAtLocationDelayed(plugin, loc, () -> {
                if (!ACTIVE.containsKey(key)) return;
                for (String cmd : cmds) executePlaySound(loc, cmd);
            }, tick);
        }
        FoliaScheduler.runAtLocationDelayed(plugin, loc, () -> {
            if (!loop) {
                ACTIVE.remove(key);
                return;
            }
            if (!ACTIVE.containsKey(key)) return;
            playSoundInternal(plugin, loc, blockName, animName, true, key);
        }, maxTick + 2);
    }

    private static void executeDataMerge(Location loc, String command) {
        String tag = extractTag(command);
        if (tag == null) return;
        Display display = findDisplay(loc, tag);
        if (display == null) return;
        Matcher tm = TRANSFORMATION_PATTERN.matcher(command);
        if (tm.find()) {
            String[] parts = tm.group(1).split(",");
            if (parts.length == 16) {
                try {
                    float[] vals = new float[16];
                    for (int i = 0; i < 16; i++) vals[i] = Float.parseFloat(parts[i].replace("f", "").trim());
                    Matrix4f mat = new Matrix4f(vals[0], vals[1], vals[2], vals[3], vals[4], vals[5], vals[6], vals[7], vals[8], vals[9], vals[10], vals[11], vals[12], vals[13], vals[14], vals[15]);
                    mat.transpose();
                    Vector3f translation = new Vector3f();
                    Vector3f scale = new Vector3f();
                    AxisAngle4f leftRot = new AxisAngle4f();
                    AxisAngle4f rightRot = new AxisAngle4f();
                    mat.getTranslation(translation);
                    mat.getScale(scale);
                    mat.getRotation(leftRot);
                    Transformation tr = new Transformation(translation, leftRot, scale, rightRot);
                    int duration = 0;
                    Matcher dm = DURATION_PATTERN.matcher(command);
                    if (dm.find()) duration = Integer.parseInt(dm.group(1));
                    int delay = 0;
                    Matcher dl = DELAY_PATTERN.matcher(command);
                    if (dl.find()) delay = Integer.parseInt(dl.group(1));
                    display.setInterpolationDelay(delay);
                    display.setInterpolationDuration(duration);
                    display.setTransformation(tr);
                    return;
                } catch (Exception ignored) {}
            }
        }
        silentDispatch(loc, command);
    }

    private static void silentDispatch(Location loc, String command) {
        String cmd = command.trim();
        if (cmd.startsWith("/")) cmd = cmd.substring(1);
        String positioned = "execute positioned " + loc.getBlockX() + " " + loc.getBlockY() + " " + loc.getBlockZ() + " run " + cmd;
        Bukkit.dispatchCommand(it.branduzzo.nextbedwars.blockdisplays.util.SilentCommandSender.get(), positioned);
    }

    private static String extractTag(String command) {
        Matcher m = TAG_PATTERN.matcher(command);
        if (m.find()) return m.group(1).replace("\"", "").replace("'", "").trim();
        return null;
    }

    private static Display findDisplay(Location loc, String tag) {
        World w = loc.getWorld();
        if (w == null) return null;
        Display nearest = null;
        double best = Double.MAX_VALUE;
        for (org.bukkit.entity.Entity e : w.getNearbyEntities(loc, 5, 5, 5, en -> en instanceof Display && en.getScoreboardTags().contains(tag))) {
            double d = e.getLocation().distanceSquared(loc);
            if (d < best) {
                best = d;
                nearest = (Display) e;
            }
        }
        return nearest;
    }

    private static void executePlaySound(Location loc, String command) {
        Matcher m = PLAYSOUND_PATTERN.matcher(command.trim());
        if (m.find()) {
            String soundName = m.group(1);
            String categoryName = m.group(2);
            float volume = Float.parseFloat(m.group(3));
            float pitch = Float.parseFloat(m.group(4));
            try {
                SoundCategory cat = SoundCategory.valueOf(categoryName.toUpperCase());
                World w = loc.getWorld();
                if (w == null) return;
                w.playSound(loc, soundName, cat, volume, pitch);
                return;
            } catch (Exception ignored) {}
        }
        silentDispatch(loc, command);
    }
}
