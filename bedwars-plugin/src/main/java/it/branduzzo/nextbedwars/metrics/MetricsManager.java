package it.branduzzo.nextbedwars.metrics;

import it.branduzzo.nextbedwars.BedWars;

import java.util.concurrent.Callable;

public class MetricsManager {

    private static boolean initialized;

    private MetricsManager() {
    }

    public static void appendPie(String id, Callable<String> callable) {
    }

    public static void initService(BedWars plugin) {
        initialized = true;
    }

    public static boolean isEnabled() {
        return initialized;
    }
}
