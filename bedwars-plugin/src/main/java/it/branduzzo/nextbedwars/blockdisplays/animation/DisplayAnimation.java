package it.branduzzo.nextbedwars.blockdisplays.animation;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import it.branduzzo.nextbedwars.blockdisplays.util.FoliaScheduler;
import org.bukkit.Location;
import org.bukkit.entity.Display;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Transformation;
import org.jetbrains.annotations.NotNull;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DisplayAnimation {

    @NotNull
    final Plugin plugin;
    @Setter
    @NotNull
    Display display;
    @Setter
    @NotNull
    Transformation transformation;

    AnimationSession animationSession = null;

    public DisplayAnimation(@NotNull Display display, @NotNull Transformation transformation, @NotNull Plugin plugin) {
        this.display = display;
        this.transformation = transformation;
        this.plugin = plugin;
    }

    public void animate(int interpolationDuration, int delay, Runnable afterEnd) {
        Location loc = display.getLocation();
        this.animationSession = new AnimationSession(delay + interpolationDuration);
        FoliaScheduler.runAtLocationDelayed(this.plugin, loc, () -> {
            if (animationSession == null || !animationSession.isCurrentlyRunning()) return;
            this.display.setInterpolationDelay(-1);
            this.display.setInterpolationDuration(interpolationDuration);
            this.display.setTransformation(this.transformation);
        }, delay);

        FoliaScheduler.runAtLocationDelayed(this.plugin, loc, () -> {
            this.animationSession = null;
            if (afterEnd != null) {
                afterEnd.run();
            }
        }, delay + interpolationDuration);
    }

    public void animate(int interpolationDuration, int delay) {
        animate(interpolationDuration, delay, null);
    }

    public void animate(int interpolationDuration) {
        animate(interpolationDuration, 0);
    }

    public boolean isAnimationRunning() {
        return this.animationSession != null && this.animationSession.isCurrentlyRunning();
    }
}
