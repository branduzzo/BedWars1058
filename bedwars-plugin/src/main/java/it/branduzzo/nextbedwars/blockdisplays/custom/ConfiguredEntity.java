package it.branduzzo.nextbedwars.blockdisplays.custom;

import it.branduzzo.nextbedwars.blockdisplays.entity.Summoner;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ConfiguredEntity<E> extends Summoner<E> {
    @NotNull
    Summoner<E> getSummoner();

    void setSummoner(@NotNull Summoner<E> summoner);

    @NotNull
    String getIdentifier();

    @Nullable
    Vector getOffset();

    void setOffset(Vector offset);
}
