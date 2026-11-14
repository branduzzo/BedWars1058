package it.branduzzo.nextbedwars.blockdisplays.custom;

import it.branduzzo.nextbedwars.blockdisplays.commandparser.CommandContainer;
import it.branduzzo.nextbedwars.blockdisplays.commandparser.CommandLine;

import java.util.List;


public interface CommandBundle extends CommandContainer {
    List<CommandLine> getCommands();

    void setCommands(List<CommandLine> commands);

    CommandSource getCommandSource();

    void setCommandSource(CommandSource commandSource);

    List<String> getGrantedCommandPermissions();

    void setGrantedCommandPermissions(List<String> grantedCommandPermissions);

    enum CommandSource {
        PLAYER,
        CONSOLE
    }
}
