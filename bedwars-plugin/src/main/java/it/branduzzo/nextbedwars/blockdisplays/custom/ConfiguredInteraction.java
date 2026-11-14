package it.branduzzo.nextbedwars.blockdisplays.custom;

import it.branduzzo.nextbedwars.blockdisplays.common.DeepCloneable;
import org.bukkit.entity.Interaction;
import org.jetbrains.annotations.Nullable;

public interface ConfiguredInteraction extends ConfiguredEntity<Interaction>, DeepCloneable<ConfiguredInteraction> {
    @Nullable
    InteractionHandler getInteractionHandler();

    void setInteractionHandler(InteractionHandler interactionHandler);
}
