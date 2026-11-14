package it.branduzzo.nextbedwars.blockdisplays.web.player;

import it.branduzzo.nextbedwars.blockdisplays.annotation.EmptyCollection;

import java.util.List;

public interface MojangSession {
    String getId();

    String getName();

    @EmptyCollection
    List<MojangSessionProperty> getProperties();

    @EmptyCollection
    List<String> getProfileActions();
}
