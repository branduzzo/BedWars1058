package it.branduzzo.nextbedwars.blockdisplays.data.locator;

import com.google.common.reflect.TypeToken;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@AllArgsConstructor
public class DataLocator<K, T> {
    K path;
    TypeToken<T> valueTypeToken;
}
