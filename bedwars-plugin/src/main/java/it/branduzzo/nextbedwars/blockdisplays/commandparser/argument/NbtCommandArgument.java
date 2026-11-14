package it.branduzzo.nextbedwars.blockdisplays.commandparser.argument;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import it.branduzzo.nextbedwars.blockdisplays.commandparser.CommandLine;
import it.branduzzo.nextbedwars.blockdisplays.commandparser.exception.CommandParseException;
import it.branduzzo.nextbedwars.blockdisplays.nbt.NbtContainer;
import it.branduzzo.nextbedwars.blockdisplays.nbt.NbtObject;
import it.branduzzo.nextbedwars.blockdisplays.nbt.exception.NbtParseException;
import org.jetbrains.annotations.NotNull;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class NbtCommandArgument extends MCCommandArgument {
    NbtContainer container;

    public NbtCommandArgument(@NotNull String strNbt, CommandLine commandLine) {
        super(strNbt, commandLine);
        try {
            container = new NbtObject(strNbt);
        } catch (NbtParseException e) {
            throw new CommandParseException(e, commandLine.toString());
        }
    }

    @Override
    public String toString() {
        return container.toString();
    }
}
