package it.branduzzo.nextbedwars.blockdisplays.serialize;

public interface Serializer<F, T> {
    T serialize(F obj);
}
