package it.branduzzo.nextbedwars.blockdisplays.custom.block;

import lombok.*;
import lombok.experimental.FieldDefaults;
import it.branduzzo.nextbedwars.blockdisplays.common.DeepCloneable;
import it.branduzzo.nextbedwars.blockdisplays.sound.PlayableSound;

@Getter
@Setter
@AllArgsConstructor
@EqualsAndHashCode
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BDCCustomBlockSoundGroup implements CustomBlockSoundGroup, DeepCloneable<BDCCustomBlockSoundGroup> {
    PlayableSound placeSound;
    PlayableSound breakSound;


    @Override
    public BDCCustomBlockSoundGroup clone() {
        return new BDCCustomBlockSoundGroup(this.placeSound, this.breakSound);
    }
}
