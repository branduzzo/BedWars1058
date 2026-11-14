package it.branduzzo.nextbedwars.api.sidebar.lib;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class TabHeaderFooter {

    private final List<SidebarLine> header;
    private final List<SidebarLine> footer;
    private final Collection<PlaceholderProvider> placeholders;

    public TabHeaderFooter(Collection<SidebarLine> header, Collection<SidebarLine> footer,
                           Collection<PlaceholderProvider> placeholders) {
        this.header = new ArrayList<>(header);
        this.footer = new ArrayList<>(footer);
        this.placeholders = placeholders == null ? new ArrayList<>() : placeholders;
    }

    public List<SidebarLine> getHeader() {
        return header;
    }

    public List<SidebarLine> getFooter() {
        return footer;
    }

    public Collection<PlaceholderProvider> getPlaceholders() {
        return placeholders;
    }
}
