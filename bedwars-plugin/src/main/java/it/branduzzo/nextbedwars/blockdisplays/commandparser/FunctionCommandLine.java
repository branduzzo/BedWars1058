package it.branduzzo.nextbedwars.blockdisplays.commandparser;

import lombok.Getter;
import it.branduzzo.nextbedwars.blockdisplays.commandparser.exception.CommandParseException;
import it.branduzzo.nextbedwars.blockdisplays.commandparser.exception.InvalidCommandNameException;
import it.branduzzo.nextbedwars.blockdisplays.commandparser.exception.InvalidNumberOfCommandArgumentsException;
import org.jetbrains.annotations.NotNull;

@Getter
public class FunctionCommandLine extends MCCommandLine {
    String modulePackName;
    String functionPath;

    public FunctionCommandLine(@NotNull String commandLine) throws CommandParseException {
        super(commandLine);

        if (!getName().equals("function")) {
            throw new InvalidCommandNameException("The string passed is not a function command!", commandLine);
        }

        if (getArguments().isEmpty()) {
            throw new InvalidNumberOfCommandArgumentsException("The function command must have arguments", commandLine);
        }

        String[] split = this.arguments.get(0).toString().split(":");

        this.modulePackName = split[0];
        this.functionPath = split[1];
    }
}
