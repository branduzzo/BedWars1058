package it.branduzzo.nextbedwars.blockdisplays.file.config.value;

import it.branduzzo.nextbedwars.blockdisplays.data.yaml.YamlData;

public final class LongConfigValue implements ConstantConfigValue<Long> {

    public static long MAX_ENTITIES_PER_CHUNK;

    @Override
    public void initialize(YamlData<Long> configuration) {
        MAX_ENTITIES_PER_CHUNK = configuration.get("max-entities-per-chunk", -1L);
    }
}
