package it.branduzzo.nextbedwars.sidebar;

import it.branduzzo.nextbedwars.api.sidebar.lib.ScoredLine;
import it.branduzzo.nextbedwars.api.sidebar.lib.SidebarLine;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BwSidebarLine extends SidebarLine implements ScoredLine {

    public final String content;
    public final String score;

    public BwSidebarLine(String content, @Nullable String score) {
        this.content = content;
        this.score = score == null ? "" : score;
    }
    @Override
    public String getScore() {
        return score;
    }

    @Override
    public @NotNull String getLine() {
        return content;
    }
}
