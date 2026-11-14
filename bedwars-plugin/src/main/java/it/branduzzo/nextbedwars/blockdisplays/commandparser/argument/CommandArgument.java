package it.branduzzo.nextbedwars.blockdisplays.commandparser.argument;

import it.branduzzo.nextbedwars.blockdisplays.commandparser.CommandLine;

public interface CommandArgument {
    CommandLine getCommandLine();

    @Override
    String toString();
}
