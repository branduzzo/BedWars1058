package it.branduzzo.nextbedwars.blockdisplays.custom.block;

import it.branduzzo.nextbedwars.blockdisplays.custom.CommandBundle;
import org.jetbrains.annotations.Nullable;

public interface CustomBlockBreakSettings {

    DropMode getDropMode();

    void setDropMode(DropMode dropMode);

    @Nullable
    CommandBundle getCommandBundle();

    void setCommandBundle(@Nullable CommandBundle breakCommandBundle);

    /**
     * Defines how items are dropped when a custom block is broken by a player.
     */
    enum DropMode {

        /**
         * Item dropped on the ground at the block's location.
         */
        ON_GROUND,

        /**
         * Items are placed directly into the player's inventory.
         * If the inventory is full, items will drop at the player's location.
         */
        INVENTORY
    }
}
