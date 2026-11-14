package it.branduzzo.nextbedwars.blockdisplays.file.config.loader;

import it.branduzzo.nextbedwars.blockdisplays.custom.block.AbstractCustomBlock;

public interface CustomBlockConfigurationFile {
    AbstractCustomBlock getAbstractCustomBlock();

    void set(String key, Object value);

    Object get(String key);

    String getName();

    String getPath();

    void reload();
}
