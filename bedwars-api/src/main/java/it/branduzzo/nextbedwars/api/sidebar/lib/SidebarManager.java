package it.branduzzo.nextbedwars.api.sidebar.lib;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.megavex.scoreboardlibrary.api.ScoreboardLibrary;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.Collection;

public class SidebarManager {

    private static SidebarManager instance;
    private static ScoreboardLibrary library;
    private static boolean libraryLoaded;

    private SidebarManager() {
    }

    public static SidebarManager init() {
        if (instance == null) {
            instance = new SidebarManager();
        }
        return instance;
    }

    public static SidebarManager getInstance() {
        return instance == null ? init() : instance;
    }

    public static synchronized ScoreboardLibrary library() {
        if (libraryLoaded) {
            return library;
        }
        libraryLoaded = true;
        Plugin plugin = plugin();
        if (plugin == null) {
            return null;
        }
        try {
            library = ScoreboardLibrary.loadScoreboardLibrary(plugin);
        } catch (Throwable throwable) {
            plugin.getLogger().warning("Packet scoreboard unavailable, using fallback: " + throwable.getMessage());
            library = null;
        }
        return library;
    }

    public static net.megavex.scoreboardlibrary.api.sidebar.Sidebar createSidebar() {
        ScoreboardLibrary scoreboardLibrary = library();
        if (scoreboardLibrary == null || scoreboardLibrary.closed()) {
            return null;
        }
        return scoreboardLibrary.createSidebar();
    }

    public static synchronized void shutdown() {
        if (library != null) {
            library.close();
            library = null;
        }
        libraryLoaded = false;
    }

    public static boolean isFolia() {
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public static Plugin plugin() {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("NextBedWars");
        if (plugin != null) {
            return plugin;
        }
        Plugin[] plugins = Bukkit.getPluginManager().getPlugins();
        return plugins.length == 0 ? null : plugins[0];
    }

    public static void forPlayer(Player player, Runnable action) {
        if (player == null) {
            return;
        }
        if (!isFolia()) {
            action.run();
            return;
        }
        Plugin plugin = plugin();
        if (plugin == null) {
            action.run();
            return;
        }
        try {
            player.getScheduler().run(plugin, task -> action.run(), null);
        } catch (Throwable throwable) {
            action.run();
        }
    }

    public Sidebar createSidebar(SidebarLine title, Collection<SidebarLine> lines,
                                 Collection<PlaceholderProvider> placeholders) {
        return new SidebarImpl(title, lines, placeholders);
    }

    public void sendHeaderFooter(Player player, TabHeaderFooter headerFooter) {
        if (player == null || headerFooter == null) {
            return;
        }
        StringBuilder header = new StringBuilder();
        for (SidebarLine line : headerFooter.getHeader()) {
            if (header.length() > 0) {
                header.append('\n');
            }
            header.append(apply(line.getLine(), headerFooter.getPlaceholders()));
        }
        StringBuilder footer = new StringBuilder();
        for (SidebarLine line : headerFooter.getFooter()) {
            if (footer.length() > 0) {
                footer.append('\n');
            }
            footer.append(apply(line.getLine(), headerFooter.getPlaceholders()));
        }
        String headerText = header.toString();
        String footerText = footer.toString();
        forPlayer(player, () -> player.sendPlayerListHeaderAndFooter(color(headerText), color(footerText)));
    }

    static Component color(String text) {
        return LegacyComponentSerializer.legacySection()
                .deserialize(ChatColor.translateAlternateColorCodes('&', text == null ? "" : text));
    }

    static String apply(String text, Collection<PlaceholderProvider> placeholders) {
        if (text == null) {
            return "";
        }
        String result = text;
        if (placeholders != null) {
            for (PlaceholderProvider provider : placeholders) {
                if (provider.getPlaceholder() == null || provider.getPlaceholder().isEmpty()) {
                    continue;
                }
                String value = provider.getValue();
                result = result.replace(provider.getPlaceholder(), value);
            }
        }
        return result;
    }

    static String sidebarEntry(int index) {
        ChatColor[] values = ChatColor.values();
        return values[index % values.length] + "" + ChatColor.RESET;
    }
}
