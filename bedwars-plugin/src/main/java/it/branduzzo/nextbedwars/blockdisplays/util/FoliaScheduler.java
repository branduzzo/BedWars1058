package it.branduzzo.nextbedwars.blockdisplays.util;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public final class FoliaScheduler {
    private static final boolean IS_FOLIA;
    private static final Method GET_GLOBAL_SCHEDULER;
    private static final Method GET_ASYNC_SCHEDULER;
    private static final Method GET_REGION_SCHEDULER;
    private static final Method GLOBAL_EXECUTE;
    private static final Method GLOBAL_RUN_DELAYED;
    private static final Method ASYNC_RUN_NOW;
    private static final Method ASYNC_RUN_DELAYED;
    private static final Method REGION_EXECUTE;
    private static final Method REGION_RUN_DELAYED;

    static {
        boolean folia = false;
        Method ggs = null, gas = null, grs = null, ge = null, gd = null, an = null, ad = null, re = null, rd = null;
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServerInitEvent");
            folia = true;
        } catch (ClassNotFoundException ignored) {}
        if (!folia) {
            try {
                Bukkit.class.getMethod("getAsyncScheduler");
                folia = true;
            } catch (NoSuchMethodException ignored) {}
        }
        if (folia) {
            try {
                ggs = Bukkit.class.getMethod("getGlobalRegionScheduler");
                gas = Bukkit.class.getMethod("getAsyncScheduler");
                grs = Bukkit.class.getMethod("getRegionScheduler");
                Class<?> global = Class.forName("io.papermc.paper.threadedregions.scheduler.GlobalRegionScheduler");
                Class<?> async = Class.forName("io.papermc.paper.threadedregions.scheduler.AsyncScheduler");
                Class<?> region = Class.forName("io.papermc.paper.threadedregions.scheduler.RegionScheduler");
                ge = global.getMethod("execute", Plugin.class, Runnable.class);
                gd = global.getMethod("runDelayed", Plugin.class, Consumer.class, long.class);
                an = async.getMethod("runNow", Plugin.class, Consumer.class);
                ad = async.getMethod("runDelayed", Plugin.class, Consumer.class, long.class, TimeUnit.class);
                re = region.getMethod("execute", Plugin.class, org.bukkit.World.class, int.class, int.class, Runnable.class);
                rd = region.getMethod("runDelayed", Plugin.class, org.bukkit.World.class, int.class, int.class, Consumer.class, long.class);
            } catch (Exception e) {
                folia = false;
            }
        }
        IS_FOLIA = folia;
        GET_GLOBAL_SCHEDULER = ggs;
        GET_ASYNC_SCHEDULER = gas;
        GET_REGION_SCHEDULER = grs;
        GLOBAL_EXECUTE = ge;
        GLOBAL_RUN_DELAYED = gd;
        ASYNC_RUN_NOW = an;
        ASYNC_RUN_DELAYED = ad;
        REGION_EXECUTE = re;
        REGION_RUN_DELAYED = rd;
    }

    private FoliaScheduler() {}

    public static boolean isFolia() {
        return IS_FOLIA;
    }

    public static void runGlobal(Plugin plugin, Runnable task) {
        if (IS_FOLIA) {
            try {
                Object scheduler = GET_GLOBAL_SCHEDULER.invoke(null);
                GLOBAL_EXECUTE.invoke(scheduler, plugin, task);
                return;
            } catch (Exception ignored) {}
        }
        Bukkit.getScheduler().runTask(plugin, task);
    }

    public static void runGlobalDelayed(Plugin plugin, Runnable task, long delayTicks) {
        if (delayTicks <= 0) { runGlobal(plugin, task); return; }
        if (IS_FOLIA) {
            try {
                Object scheduler = GET_GLOBAL_SCHEDULER.invoke(null);
                GLOBAL_RUN_DELAYED.invoke(scheduler, plugin, (Consumer<Object>) o -> task.run(), delayTicks);
                return;
            } catch (Exception ignored) {}
        }
        Bukkit.getScheduler().runTaskLater(plugin, task, delayTicks);
    }

    public static void runAsync(Plugin plugin, Runnable task) {
        if (IS_FOLIA) {
            try {
                Object scheduler = GET_ASYNC_SCHEDULER.invoke(null);
                ASYNC_RUN_NOW.invoke(scheduler, plugin, (Consumer<Object>) o -> task.run());
                return;
            } catch (Exception ignored) {}
        }
        Bukkit.getScheduler().runTaskAsynchronously(plugin, task);
    }

    public static void runAsyncDelayed(Plugin plugin, Runnable task, long delayTicks) {
        if (IS_FOLIA) {
            try {
                Object scheduler = GET_ASYNC_SCHEDULER.invoke(null);
                ASYNC_RUN_DELAYED.invoke(scheduler, plugin, (Consumer<Object>) o -> task.run(), delayTicks * 50L, TimeUnit.MILLISECONDS);
                return;
            } catch (Exception ignored) {}
        }
        Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, task, delayTicks);
    }

    public static void runAtLocation(Plugin plugin, Location loc, Runnable task) {
        if (IS_FOLIA && loc != null && loc.getWorld() != null) {
            try {
                Object scheduler = GET_REGION_SCHEDULER.invoke(null);
                REGION_EXECUTE.invoke(scheduler, plugin, loc.getWorld(), loc.getBlockX() >> 4, loc.getBlockZ() >> 4, task);
                return;
            } catch (Exception ignored) {}
        }
        Bukkit.getScheduler().runTask(plugin, task);
    }

    public static void runAtLocationDelayed(Plugin plugin, Location loc, Runnable task, long delayTicks) {
        if (delayTicks <= 0) { runAtLocation(plugin, loc, task); return; }
        if (IS_FOLIA && loc != null && loc.getWorld() != null) {
            try {
                Object scheduler = GET_REGION_SCHEDULER.invoke(null);
                REGION_RUN_DELAYED.invoke(scheduler, plugin, loc.getWorld(), loc.getBlockX() >> 4, loc.getBlockZ() >> 4, (Consumer<Object>) o -> task.run(), delayTicks);
                return;
            } catch (Exception ignored) {}
        }
        Bukkit.getScheduler().runTaskLater(plugin, task, delayTicks);
    }

    public static void cancelTask(int taskId) {
        Bukkit.getScheduler().cancelTask(taskId);
    }

    public static boolean isCurrentlyRunning(int taskId) {
        try { return Bukkit.getScheduler().isCurrentlyRunning(taskId); } catch (Exception e) { return false; }
    }

    public static boolean isQueued(int taskId) {
        try { return Bukkit.getScheduler().isQueued(taskId); } catch (Exception e) { return false; }
    }
}
