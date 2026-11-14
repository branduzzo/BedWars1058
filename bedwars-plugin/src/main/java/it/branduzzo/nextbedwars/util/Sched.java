package it.branduzzo.nextbedwars.util;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.lang.reflect.Method;
import java.util.function.Consumer;

/**
 * Scheduler abstraction that keeps a single plugin jar working on both Paper and Folia.
 * Bukkit scheduler calls are mirrored so call sites only need a class swap.
 */
public final class Sched {

    private static final boolean FOLIA;
    private static Method globalExecute, globalRunDelayed, globalRunAtFixedRate;
    private static Method regionExecute, regionRunDelayed, regionRunAtFixedRate;
    private static Method asyncExecute;

    static {
        boolean folia;
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            folia = true;
        } catch (ClassNotFoundException e) {
            folia = false;
        }
        FOLIA = folia;
        if (FOLIA) {
            try {
                Class<?> global = Class.forName("io.papermc.paper.threadedregions.scheduler.GlobalRegionScheduler");
                globalExecute = global.getMethod("execute", Plugin.class, Runnable.class);
                globalRunDelayed = global.getMethod("runDelayed", Plugin.class, Consumer.class, long.class);
                globalRunAtFixedRate = global.getMethod("runAtFixedRate", Plugin.class, Consumer.class, long.class, long.class);

                Class<?> region = Class.forName("io.papermc.paper.threadedregions.scheduler.RegionScheduler");
                regionExecute = region.getMethod("execute", Plugin.class, Location.class, Runnable.class);
                regionRunDelayed = region.getMethod("runDelayed", Plugin.class, Location.class, Consumer.class, long.class);
                regionRunAtFixedRate = region.getMethod("runAtFixedRate", Plugin.class, Location.class, Consumer.class, long.class, long.class);

                Class<?> async = Class.forName("io.papermc.paper.threadedregions.scheduler.AsyncScheduler");
                asyncExecute = async.getMethod("runNow", Plugin.class, Consumer.class);
            } catch (ReflectiveOperationException ignored) {
            }
        }
    }

    private Sched() {
    }

    public static boolean isFolia() {
        return FOLIA;
    }

    public static void runTask(Plugin plugin, Runnable task) {
        if (!FOLIA) {
            Bukkit.getScheduler().runTask(plugin, task);
            return;
        }
        invoke(globalExecute, plugin, task);
    }

    public static void runTaskAsynchronously(Plugin plugin, Runnable task) {
        if (!FOLIA) {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, task);
            return;
        }
        dispatch(plugin, asyncExecute, task);
    }

    public static BukkitTask runTaskLater(Plugin plugin, Runnable task, long delay) {
        if (!FOLIA) {
            return Bukkit.getScheduler().runTaskLater(plugin, task, delay);
        }
        return handle(plugin, globalRunDelayed, null, task, delay, 0);
    }

    public static void runTaskLaterAsynchronously(Plugin plugin, Runnable task, long delay) {
        if (!FOLIA) {
            Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, task, delay);
            return;
        }
        dispatch(plugin, asyncExecute, task);
    }

    public static BukkitTask runTaskTimer(Plugin plugin, Runnable task, long delay, long period) {
        if (!FOLIA) {
            return Bukkit.getScheduler().runTaskTimer(plugin, task, delay, period);
        }
        return handle(plugin, globalRunAtFixedRate, null, task, delay, period);
    }

    public static BukkitTask runTaskTimerAsynchronously(Plugin plugin, Runnable task, long delay, long period) {
        if (!FOLIA) {
            return Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, task, delay, period);
        }
        return new Handle(plugin, dispatch(plugin, asyncExecute, task));
    }

    public static int scheduleSyncDelayedTask(Plugin plugin, Runnable task, long delay) {
        if (!FOLIA) {
            return Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, task, delay);
        }
        handle(plugin, globalRunDelayed, null, task, delay, 0);
        return -1;
    }

    public static void scheduleSyncRepeatingTask(Plugin plugin, Runnable task, long delay, long period) {
        runTaskTimer(plugin, task, delay, period);
    }

    public static boolean isCurrentlyRunning(int taskId) {
        return !FOLIA && taskId > 0 && Bukkit.getScheduler().isCurrentlyRunning(taskId);
    }

    public static boolean isQueued(int taskId) {
        return !FOLIA && taskId > 0 && Bukkit.getScheduler().isQueued(taskId);
    }

    public static void cancelTask(int taskId) {
        if (!FOLIA && taskId > 0) {
            Bukkit.getScheduler().cancelTask(taskId);
        }
    }

    public static void runAtLocation(Plugin plugin, Location location, Runnable task) {
        if (!FOLIA || location == null || location.getWorld() == null) {
            Bukkit.getScheduler().runTask(plugin, task);
            return;
        }
        try {
            regionExecute.invoke(Bukkit.getRegionScheduler(), plugin, location, task);
        } catch (ReflectiveOperationException e) {
            runTask(plugin, task);
        }
    }

    public static void runAtLocationLater(Plugin plugin, Location location, Runnable task, long delay) {
        if (!FOLIA || location == null || location.getWorld() == null) {
            Bukkit.getScheduler().runTaskLater(plugin, task, delay);
            return;
        }
        handle(plugin, regionRunDelayed, location, task, delay, 0);
    }

    public static BukkitTask runAtLocationTimer(Plugin plugin, Location location, Runnable task, long delay, long period) {
        if (!FOLIA || location == null || location.getWorld() == null) {
            return Bukkit.getScheduler().runTaskTimer(plugin, task, delay, period);
        }
        return handle(plugin, regionRunAtFixedRate, location, task, delay, period);
    }

    public static void runForEntity(Plugin plugin, Entity entity, Runnable task) {
        if (!FOLIA || entity == null) {
            Bukkit.getScheduler().runTask(plugin, task);
            return;
        }
        try {
            Object scheduler = entity.getClass().getMethod("getScheduler").invoke(entity);
            scheduler.getClass().getMethod("run", Plugin.class, Consumer.class, Runnable.class)
                    .invoke(scheduler, plugin, (Consumer<Object>) o -> task.run(), null);
        } catch (ReflectiveOperationException e) {
            runTask(plugin, task);
        }
    }

    public static void cancelTasks(Plugin plugin) {
        if (!FOLIA) {
            Bukkit.getScheduler().cancelTasks(plugin);
            return;
        }
        try {
            Bukkit.getGlobalRegionScheduler().cancelTasks(plugin);
            Bukkit.getAsyncScheduler().cancelTasks(plugin);
        } catch (Throwable ignored) {
        }
    }

    private static void invoke(Method method, Plugin plugin, Runnable task) {
        try {
            method.invoke(Bukkit.getGlobalRegionScheduler(), plugin, task);
        } catch (ReflectiveOperationException e) {
            task.run();
        }
    }

    private static Object dispatch(Plugin plugin, Method method, Runnable task) {
        try {
            return method.invoke(Bukkit.getAsyncScheduler(), plugin, (Consumer<Object>) o -> task.run());
        } catch (ReflectiveOperationException e) {
            task.run();
            return null;
        }
    }

    private static BukkitTask handle(Plugin plugin, Method method, Location location, Runnable task,
                                     long delay, long period) {
        try {
            Object scheduler = location == null ? Bukkit.getGlobalRegionScheduler() : Bukkit.getRegionScheduler();
            Object scheduled;
            if (period > 0) {
                scheduled = location == null
                        ? method.invoke(scheduler, plugin, (Consumer<Object>) o -> task.run(), Math.max(1, delay), Math.max(1, period))
                        : method.invoke(scheduler, plugin, location, (Consumer<Object>) o -> task.run(), Math.max(1, delay), Math.max(1, period));
            } else {
                scheduled = location == null
                        ? method.invoke(scheduler, plugin, (Consumer<Object>) o -> task.run(), Math.max(1, delay))
                        : method.invoke(scheduler, plugin, location, (Consumer<Object>) o -> task.run(), Math.max(1, delay));
            }
            return new Handle(plugin, scheduled);
        } catch (ReflectiveOperationException e) {
            if (period > 0) {
                Bukkit.getScheduler().runTaskTimer(plugin, task, delay, period);
            } else {
                Bukkit.getScheduler().runTaskLater(plugin, task, delay);
            }
            return new Handle(plugin, null);
        }
    }

    private static final class Handle implements BukkitTask {

        private final Plugin plugin;
        private final Object scheduled;
        private boolean cancelled;

        private Handle(Plugin plugin, Object scheduled) {
            this.plugin = plugin;
            this.scheduled = scheduled;
        }

        @Override
        public int getTaskId() {
            return -1;
        }

        @Override
        public Plugin getOwner() {
            return plugin;
        }

        @Override
        public boolean isSync() {
            return true;
        }

        @Override
        public boolean isCancelled() {
            return cancelled;
        }

        @Override
        public void cancel() {
            cancelled = true;
            if (scheduled != null) {
                try {
                    scheduled.getClass().getMethod("cancel").invoke(scheduled);
                } catch (ReflectiveOperationException ignored) {
                }
            }
        }
    }
}
