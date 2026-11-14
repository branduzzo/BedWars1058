package it.branduzzo.nextbedwars.blockdisplays.compat;

import org.bukkit.Bukkit;

public final class HorizonSupport {
    private static final boolean HORIZON = detect();

    private HorizonSupport() {}

    private static boolean detect() {
        try {
            Class.forName("io.canvasmc.horizon.Horizon");
            return true;
        } catch (ClassNotFoundException e) {
            String ver = Bukkit.getVersion();
            String name = Bukkit.getName();
            return ver.toLowerCase().contains("horizon") || name.toLowerCase().contains("horizon");
        }
    }

    public static boolean isHorizon() {
        return HORIZON;
    }

    public static String getHorizonVersion() {
        if (!HORIZON) return null;
        try {
            Class<?> c = Class.forName("io.canvasmc.horizon.Horizon");
            return c.getPackage().getImplementationVersion();
        } catch (Exception e) {
            return Bukkit.getVersion();
        }
    }
}
