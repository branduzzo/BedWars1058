package it.branduzzo.nextbedwars.blockdisplays.commandparser.argument;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import it.branduzzo.nextbedwars.blockdisplays.commandparser.CommandLine;
import it.branduzzo.nextbedwars.blockdisplays.commandparser.exception.InvalidCommandArgumentException;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class EntityArgument extends MCCommandArgument {
    EntityType entityType;

    public EntityArgument(@NotNull String stringArg, @NotNull CommandLine commandLine) {
        super(stringArg, commandLine);
        try {
            if (stringArg.toLowerCase().startsWith("minecraft:")) {
                this.entityType = EntityType.valueOf(stringArg.toUpperCase().substring(10));
            } else {
                this.entityType = EntityType.valueOf(stringArg.toUpperCase());
            }
        } catch (IllegalArgumentException e) {
            throw new InvalidCommandArgumentException(stringArg + " is not an entity type!", commandLine.toString());
        }
    }

    public EntityArgument(@NotNull CommandArgument argument, @NotNull CommandLine commandLine) {
        this(argument.toString(), commandLine);
    }

    public EntityArgument(@NotNull EntityType entityType, @NotNull CommandLine commandLine) {
        super(entityType.toString().toLowerCase(), commandLine);
    }

    public EntityType getType() {
        return entityType;
    }

    public void setType(EntityType entityType) {
        this.entityType = entityType;
    }

    public String toString() {
        return entityType.toString();
    }
}
