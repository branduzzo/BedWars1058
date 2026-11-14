package it.branduzzo.nextbedwars.blockdisplays.data.locator;

import com.google.common.reflect.TypeToken;

public class MetaDataLocator<T> extends DataLocator<String, T> {
    public MetaDataLocator(String key, TypeToken<T> valueTypeToken) {
        super(key, valueTypeToken);
    }
}
