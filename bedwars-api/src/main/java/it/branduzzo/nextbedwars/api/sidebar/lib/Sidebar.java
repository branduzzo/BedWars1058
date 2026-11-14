package it.branduzzo.nextbedwars.api.sidebar.lib;

import org.bukkit.entity.Player;

import java.util.Collection;

public interface Sidebar {

    void add(Player player);

    void remove(Player player);

    void setTitle(SidebarLine title);

    void addLine(SidebarLine line);

    void clearLines();

    Collection<PlaceholderProvider> getPlaceholders();

    void addPlaceholder(PlaceholderProvider placeholder);

    void removePlaceholder(String placeholder);

    void refreshTitle();

    void refreshPlaceholders();

    void hidePlayersHealth();

    void showPlayersHealth(SidebarLine line, boolean inTab);

    void setPlayerHealth(Player player, int health);

    void playerHealthRefreshAnimation();

    void playerTabRefreshAnimation();

    PlayerTab playerTabCreate(String identifier, Player player, SidebarLine prefix, SidebarLine suffix,
                              PlayerTab.PushingRule pushingRule, Collection<PlaceholderProvider> placeholders);

    void removeTab(String identifier);

    void removeTabs();
}
