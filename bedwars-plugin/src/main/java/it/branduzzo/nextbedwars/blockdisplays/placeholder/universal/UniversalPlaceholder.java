package it.branduzzo.nextbedwars.blockdisplays.placeholder.universal;

import org.jetbrains.annotations.NotNull;

public interface UniversalPlaceholder<T> {
    T context();

    @NotNull
    String apply(String template);
}
