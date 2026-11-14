package it.branduzzo.nextbedwars.arena.stats;

import it.branduzzo.nextbedwars.BedWars;
import it.branduzzo.nextbedwars.api.arena.stats.Incrementable;
import it.branduzzo.nextbedwars.api.arena.stats.GameStatistic;
import it.branduzzo.nextbedwars.api.arena.stats.GameStatisticProvider;
import it.branduzzo.nextbedwars.api.language.Language;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class GenericStatistic implements GameStatisticProvider<GenericStatistic.Value> {

    @Override
    public GenericStatistic.Value getDefault() {
        return new Value();
    }

    @Override
    public String getVoidReplacement(@Nullable Language language) {
        return "0";
    }

    @Override
    public Plugin getOwner() {
        return BedWars.plugin;
    }

    public static class Value implements GameStatistic<Integer>, Incrementable, Comparable<GameStatistic<Integer>> {
        private int count = 0;

        @Override
        public Integer getValue() {
            return count;
        }

        @Override
        public String getDisplayValue(Language language) {
            return String.valueOf(getValue());
        }

        @Override
        public int compareTo(@NotNull GameStatistic<Integer> o) {
            return Integer.compare(this.count, o.getValue());
        }

        @Override
        public void increment() {
            count++;
        }
    }
}
