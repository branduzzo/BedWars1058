package it.branduzzo.nextbedwars.api.sidebar.lib;

import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class SidebarImpl implements Sidebar {

    private static final int MAX_LINES = 15;

    private final List<SidebarLine> lines = new ArrayList<>();
    private final List<PlaceholderProvider> placeholders = new ArrayList<>();
    private final Map<String, PlayerTab> tabs = new HashMap<>();
    private final Map<String, String[]> tabText = new HashMap<>();
    private final Map<java.util.UUID, Integer> health = new HashMap<>();

    private SidebarLine title;
    private Player viewer;
    private Scoreboard board;
    private net.megavex.scoreboardlibrary.api.sidebar.Sidebar packetSidebar;
    private BossBar bar;
    private SidebarLine healthLine;
    private boolean healthInTab;

    SidebarImpl(SidebarLine title, Collection<SidebarLine> initialLines,
                Collection<PlaceholderProvider> initialPlaceholders) {
        this.title = title;
        this.lines.addAll(initialLines);
        if (initialPlaceholders != null) {
            this.placeholders.addAll(initialPlaceholders);
        }
    }

    @Override
    public synchronized void add(Player player) {
        this.viewer = player;
        this.board = createBoard();
        if (board != null) {
            try {
                player.setScoreboard(board);
            } catch (UnsupportedOperationException ignored) {
                board = null;
            }
        }
        this.packetSidebar = SidebarManager.createSidebar();
        if (packetSidebar == null) {
            this.bar = BossBar.bossBar(Component.empty(), 1f, BossBar.Color.WHITE, BossBar.Overlay.PROGRESS);
        }
        render();
        if (packetSidebar != null && !packetSidebar.closed()) {
            packetSidebar.addPlayer(player);
        }
    }

    @Override
    public synchronized void remove(Player player) {
        if (packetSidebar != null) {
            if (player != null) {
                packetSidebar.removePlayer(player);
            }
            packetSidebar.close();
            packetSidebar = null;
        }
        if (player != null) {
            if (board != null) {
                try {
                    player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
                } catch (UnsupportedOperationException ignored) {
                }
            }
            final BossBar shown = bar;
            if (shown != null) {
                SidebarManager.forPlayer(player, () -> player.hideBossBar(shown));
            }
        }
        this.viewer = null;
        this.board = null;
        this.bar = null;
    }

    private static Scoreboard createBoard() {
        try {
            return Bukkit.getScoreboardManager().getNewScoreboard();
        } catch (UnsupportedOperationException | IllegalStateException e) {
            return null;
        }
    }

    @Override
    public synchronized void setTitle(SidebarLine title) {
        this.title = title;
        renderTitle();
    }

    @Override
    public synchronized void addLine(SidebarLine line) {
        lines.add(line);
        render();
    }

    @Override
    public synchronized void clearLines() {
        lines.clear();
        render();
    }

    @Override
    public Collection<PlaceholderProvider> getPlaceholders() {
        return placeholders;
    }

    @Override
    public void addPlaceholder(PlaceholderProvider placeholder) {
        placeholders.add(placeholder);
    }

    @Override
    public void removePlaceholder(String placeholder) {
        placeholders.removeIf(provider -> provider.getPlaceholder().equals(placeholder));
    }

    @Override
    public synchronized void refreshTitle() {
        if (title instanceof SidebarLineAnimated animated) {
            animated.nextFrame();
        }
        renderTitle();
    }

    @Override
    public synchronized void refreshPlaceholders() {
        render();
    }

    @Override
    public void hidePlayersHealth() {
        healthInTab = false;
    }

    @Override
    public void showPlayersHealth(SidebarLine line, boolean inTab) {
        this.healthLine = line;
        this.healthInTab = inTab;
    }

    @Override
    public void setPlayerHealth(Player player, int health) {
        this.health.put(player.getUniqueId(), health);
    }

    @Override
    public void playerHealthRefreshAnimation() {
        if (healthLine instanceof SidebarLineAnimated animated) {
            animated.nextFrame();
        }
    }

    @Override
    public synchronized void playerTabRefreshAnimation() {
        tabs.keySet().forEach(this::renderTab);
    }

    @Override
    public synchronized PlayerTab playerTabCreate(String identifier, Player player, SidebarLine prefix, SidebarLine suffix,
                                                  PlayerTab.PushingRule pushingRule, Collection<PlaceholderProvider> providers) {
        String id = identifier == null ? player.getName() : identifier;
        if (id.length() > 16) {
            id = id.substring(0, 16);
        }
        PlayerTab tab = new PlayerTab(id);
        tabs.put(player.getName(), tab);
        tabText.put(player.getName(), new String[]{prefix == null ? "" : prefix.getLine(),
                suffix == null ? "" : suffix.getLine()});
        renderTab(player.getName());
        return tab;
    }

    @Override
    public synchronized void removeTab(String identifier) {
        if (board == null || identifier == null) {
            return;
        }
        String id = identifier.length() > 16 ? identifier.substring(0, 16) : identifier;
        Team team = board.getTeam(id);
        if (team != null) {
            team.unregister();
        }
        tabs.values().removeIf(tab -> tab.getIdentifier().equals(id));
    }

    @Override
    public synchronized void removeTabs() {
        if (board == null) {
            return;
        }
        new ArrayList<>(board.getTeams()).forEach(Team::unregister);
        tabs.clear();
        tabText.clear();
        render();
    }

    private void renderTab(String playerName) {
        if (board == null) {
            return;
        }
        PlayerTab tab = tabs.get(playerName);
        if (tab == null) {
            return;
        }
        String[] text = tabText.get(playerName);
        if (text == null) {
            return;
        }
        Team team = board.getTeam(tab.getIdentifier());
        if (team == null) {
            team = board.registerNewTeam(tab.getIdentifier());
        }
        team.setPrefix(trim(apply(text[0])));
        team.setSuffix(trim(apply(text[1])));
        team.setOption(Team.Option.NAME_TAG_VISIBILITY,
                Team.OptionStatus.valueOf(tab.getNameTagVisibility().name()));
        if (!team.hasEntry(playerName)) {
            team.addEntry(playerName);
        }
    }

    private synchronized void renderTitle() {
        if (packetSidebar != null) {
            packetSidebar.title(SidebarManager.color(titleText()));
            packetSidebar.refreshTitle();
            return;
        }
        if (board == null) {
            renderBar();
            return;
        }
        if (title == null) {
            return;
        }
        Objective objective = board.getObjective("nbw");
        if (objective == null) {
            render();
            return;
        }
        objective.setDisplayName(trim(apply(title.getLine())));
    }

    private synchronized void renderBar() {
        if (bar == null || viewer == null) {
            return;
        }
        StringBuilder content = new StringBuilder();
        if (title != null) {
            content.append(apply(title.getLine()));
        }
        for (SidebarLine line : lines) {
            String text = apply(line.getLine());
            if (text.isEmpty()) {
                continue;
            }
            content.append('\n').append(text);
        }
        final Component component = SidebarManager.color(content.toString());
        final BossBar shown = bar;
        final Player target = viewer;
        SidebarManager.forPlayer(target, () -> {
            shown.name(component);
            target.showBossBar(shown);
        });
    }

    private synchronized void render() {
        if (packetSidebar != null) {
            packetSidebar.title(SidebarManager.color(titleText()));
            packetSidebar.clearLines();
            int index = 0;
            for (SidebarLine line : lines) {
                if (index >= MAX_LINES) {
                    break;
                }
                String text = apply(line.getLine());
                packetSidebar.line(index++, text.isEmpty() ? Component.empty() : SidebarManager.color(text));
            }
            return;
        }
        if (board == null) {
            renderBar();
            return;
        }
        if (viewer == null) {
            return;
        }
        Objective objective = board.getObjective("nbw");
        if (objective == null) {
            objective = board.registerNewObjective("nbw", Criteria.DUMMY, trim(apply(title == null ? "" : title.getLine())));
            objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        }
        for (String entry : new ArrayList<>(board.getEntries())) {
            board.resetScores(entry);
        }
        int rendered = 0;
        int score = lines.size();
        for (SidebarLine line : lines) {
            if (rendered >= MAX_LINES) {
                break;
            }
            String text = apply(line.getLine());
            if (text.isEmpty()) {
                continue;
            }
            String entry = SidebarManager.sidebarEntry(rendered);
            Team team = board.getTeam("line" + rendered);
            if (team == null) {
                team = board.registerNewTeam("line" + rendered);
            }
            String[] split = split(text);
            team.setPrefix(split[0]);
            team.setSuffix(split[1]);
            if (!team.hasEntry(entry)) {
                team.addEntry(entry);
            }
            objective.getScore(entry).setScore(Math.max(0, score - rendered));
            rendered++;
        }
        objective.setDisplayName(trim(apply(title == null ? "" : title.getLine())));
    }

    private String titleText() {
        return title == null ? "" : apply(title.getLine());
    }

    private String apply(String text) {
        return SidebarManager.apply(text, placeholders);
    }

    private static String[] split(String text) {
        String colored = text;
        String prefix;
        String suffix;
        if (colored.length() <= 64) {
            prefix = colored;
            suffix = "";
        } else {
            prefix = colored.substring(0, 64);
            suffix = colored.substring(64, Math.min(colored.length(), 128));
            String lastColor = ChatColor.getLastColors(prefix);
            if (!lastColor.isEmpty() && !suffix.startsWith(lastColor)) {
                suffix = lastColor + suffix;
            }
        }
        return new String[]{prefix, suffix};
    }

    private static String trim(String text) {
        if (text == null) {
            return "";
        }
        return text.length() > 64 ? text.substring(0, 64) : text;
    }
}
