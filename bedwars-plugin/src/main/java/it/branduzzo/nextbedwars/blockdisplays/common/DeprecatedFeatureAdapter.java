package it.branduzzo.nextbedwars.blockdisplays.common;

import lombok.experimental.UtilityClass;
import it.branduzzo.nextbedwars.blockdisplays.custom.block.BDCCustomBlockService;
import org.jetbrains.annotations.ApiStatus;

import java.util.UUID;

@ApiStatus.Internal
@UtilityClass
@Deprecated
public final class DeprecatedFeatureAdapter {
    public UUID checkMissingCustomBlockUUID(UUID uuid) {
        if (uuid == null) {
            return UUID.randomUUID();
        }
        return uuid;
    }

    public String checkMissingServiceClass(String serviceClass) {
        if (serviceClass == null) {
            return BDCCustomBlockService.class.getName();
        }
        return serviceClass;
    }
}
