package it.branduzzo.nextbedwars.api.sidebar.lib;

import java.util.function.Supplier;

public class PlaceholderProvider {

    private final String placeholder;
    private final Supplier<String> supplier;

    public PlaceholderProvider(String placeholder, Supplier<String> supplier) {
        this.placeholder = placeholder;
        this.supplier = supplier;
    }

    public String getPlaceholder() {
        return placeholder;
    }

    public String getValue() {
        String value = supplier == null ? "" : supplier.get();
        return value == null ? "" : value;
    }
}
