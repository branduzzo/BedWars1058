package it.branduzzo.nextbedwars.blockdisplays.web.player;

import org.jetbrains.annotations.Nullable;

public interface MojangSessionProperty {
    String getName();

    String getValue();

    @Nullable
    MojangTextureData decodeValue();

    @Nullable
    String getSignature();
}
