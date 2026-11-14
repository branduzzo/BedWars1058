package it.branduzzo.nextbedwars.blockdisplays.custom.block;

import it.branduzzo.nextbedwars.blockdisplays.sound.PlayableSound;
import org.jetbrains.annotations.Nullable;


public interface CustomBlockSoundGroup {
    @Nullable
    PlayableSound getPlaceSound();

    void setPlaceSound(PlayableSound sound);

    @Nullable
    PlayableSound getBreakSound();

    void setBreakSound(PlayableSound sound);
}
