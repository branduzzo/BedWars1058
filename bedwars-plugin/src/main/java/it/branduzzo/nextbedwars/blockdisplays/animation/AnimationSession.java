package it.branduzzo.nextbedwars.blockdisplays.animation;

import it.branduzzo.nextbedwars.blockdisplays.util.FoliaScheduler;

public class AnimationSession {
    private final long expiryMillis;
    private volatile boolean cancelled = false;

    public AnimationSession(long durationTicks) {
        this.expiryMillis = System.currentTimeMillis() + durationTicks * 50L;
    }

    public boolean isCurrentlyRunning() {
        return !cancelled && System.currentTimeMillis() < expiryMillis;
    }

    public boolean isQueued() {
        return isCurrentlyRunning();
    }

    public void cancelTask() {
        cancelled = true;
    }

    public boolean isExpired() {
        return System.currentTimeMillis() >= expiryMillis;
    }
}
