package it.branduzzo.nextbedwars.api.sidebar.lib;

import org.jetbrains.annotations.NotNull;

public class SidebarLineAnimated extends SidebarLine {

    private final String[] frames;
    private int index;

    public SidebarLineAnimated(String[] frames) {
        this.frames = frames == null || frames.length == 0 ? new String[]{""} : frames;
    }

    public void nextFrame() {
        index = (index + 1) % frames.length;
    }

    @Override
    public @NotNull String getLine() {
        return frames[index];
    }
}
