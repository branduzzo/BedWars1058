package it.branduzzo.nextbedwars.blockdisplays.file.config.value;

import it.branduzzo.nextbedwars.blockdisplays.data.yaml.YamlData;

public interface ConstantConfigValue<T> {
    void initialize(YamlData<T> configuration);
}
