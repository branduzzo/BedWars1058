package it.branduzzo.nextbedwars.blockdisplays.custom.block;

import lombok.*;
import lombok.experimental.FieldDefaults;
import it.branduzzo.nextbedwars.blockdisplays.custom.CommandBundle;

@Getter
@Setter
@EqualsAndHashCode
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BDCCustomBlockPlaceSettings implements CustomBlockPlaceSettings {
    PlacementMode placementMode;
    CommandBundle commandBundle;

    public BDCCustomBlockPlaceSettings(PlacementMode placementMode) {
        this(placementMode, null);
    }

    public BDCCustomBlockPlaceSettings() {
        this(PlacementMode.DEFAULT, null);
    }
}
