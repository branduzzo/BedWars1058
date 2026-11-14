package it.branduzzo.nextbedwars.blockdisplays.commandparser;

import it.branduzzo.nextbedwars.blockdisplays.commandparser.argument.CommandArgument;
import it.branduzzo.nextbedwars.blockdisplays.commandparser.argument.CommandCoordinate;
import it.branduzzo.nextbedwars.blockdisplays.commandparser.argument.EntityArgument;
import it.branduzzo.nextbedwars.blockdisplays.commandparser.argument.NbtCommandArgument;
import it.branduzzo.nextbedwars.blockdisplays.commandparser.exception.InvalidCommandArgumentException;
import it.branduzzo.nextbedwars.blockdisplays.common.DeepCloneable;
import it.branduzzo.nextbedwars.blockdisplays.entity.display.DisplaySummoner;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SummonDisplayCommandLine extends SummonCommandLine {
    public SummonDisplayCommandLine(@NotNull String commandLine) throws IllegalArgumentException {
        super(commandLine);

        if (!DisplaySummoner.isDisplayEntity(getSummonEntity().getType())) {
            throw new InvalidCommandArgumentException(getSummonEntity().toString() + " is not a display entity!", commandLine);
        }
    }

    public SummonDisplayCommandLine(List<CommandArgument> arguments, String name, @Nullable String namespace, EntityArgument summonEntity, CommandCoordinate x, CommandCoordinate y, CommandCoordinate z, NbtCommandArgument nbtArg) {
        super(arguments, name, namespace, summonEntity, x, y, z, nbtArg);
        if (!DisplaySummoner.isDisplayEntity(getSummonEntity().getType())) {
            throw new InvalidCommandArgumentException(getSummonEntity().toString() + " is not a display entity!");
        }
    }

    @Override
    public SummonDisplayCommandLine clone() {
        return new SummonDisplayCommandLine(
                this.arguments.stream().map(DeepCloneable::tryClone).collect(Collectors.toCollection(ArrayList::new)),
                this.name,
                this.namespace,
                this.summonEntity,
                this.x,
                this.y,
                this.z,
                this.nbtArg
        );
    }
}
