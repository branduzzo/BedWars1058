package it.branduzzo.nextbedwars.blockdisplays.file.config.value;

import it.branduzzo.nextbedwars.blockdisplays.data.yaml.YamlData;


public final class BooleanConfigValue implements ConstantConfigValue<Boolean> {
    public static boolean BLOCKS_DESTRUCTION;
    public static boolean BLOCKS_PLACEMENT;
    public static boolean BLOCKS_INTERACTION;


    @Override
    public void initialize(YamlData<Boolean> configuration) {
        BLOCKS_DESTRUCTION = configuration.get("blocks-destruction", true);
        BLOCKS_PLACEMENT = configuration.get("blocks-placement", true);
        BLOCKS_INTERACTION = configuration.get("blocks-interaction", true);
    }
}
