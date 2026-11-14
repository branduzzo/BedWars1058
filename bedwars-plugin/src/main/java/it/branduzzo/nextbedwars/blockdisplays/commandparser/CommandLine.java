package it.branduzzo.nextbedwars.blockdisplays.commandparser;

import it.branduzzo.nextbedwars.blockdisplays.commandparser.argument.CommandArgument;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface CommandLine extends CommandContainer {
    String getName();

    @Nullable
    String getNamespace();

    CommandArgument getArgument(int index);

    List<CommandArgument> getArguments();

    @Override
    String toString();
}
