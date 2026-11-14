package it.branduzzo.nextbedwars.blockdisplays.commandparser;

import it.branduzzo.nextbedwars.blockdisplays.placeholder.universal.UniversalPlaceholder;
import org.bukkit.command.CommandSender;

public interface CommandContainer {
    void execute(CommandSender sender, UniversalPlaceholder<?>... placeholders);
}
