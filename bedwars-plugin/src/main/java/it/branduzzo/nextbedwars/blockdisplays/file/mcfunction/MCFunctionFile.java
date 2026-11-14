package it.branduzzo.nextbedwars.blockdisplays.file.mcfunction;

import lombok.Getter;
import it.branduzzo.nextbedwars.blockdisplays.commandparser.CommandLine;
import it.branduzzo.nextbedwars.blockdisplays.commandparser.MCCommandLine;
import it.branduzzo.nextbedwars.blockdisplays.commandparser.exception.CommandParseException;
import it.branduzzo.nextbedwars.blockdisplays.file.CachedFile;
import it.branduzzo.nextbedwars.blockdisplays.file.exception.FileLoadException;
import it.branduzzo.nextbedwars.blockdisplays.file.exception.InvalidFileFormatException;
import it.branduzzo.nextbedwars.blockdisplays.placeholder.universal.UniversalPlaceholder;
import it.branduzzo.nextbedwars.blockdisplays.util.OperationUtil;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Getter
public class MCFunctionFile extends CachedFile implements MCFunction {
    List<CommandLine> commands;

    public MCFunctionFile(@NotNull Path path, boolean createNew) {
        super(path, createNew);
        this.commands = new ArrayList<>();
    }

    public MCFunctionFile(@NotNull Path path) {
        this(path, false);
    }

    private static boolean isMCFunctionFormat(@NotNull Path file) {
        return file.getFileName().endsWith(".mcfunction");
    }

    @Override
    public CommandLine getCommand(int index) {
        return this.commands.get(index);
    }

    @Override
    public void addCommand(CommandLine commandLine) {
        this.commands.add(commandLine);
    }

    @Override
    public void removeCommand(int index) {
        this.commands.remove(index);
    }

    @Override
    public void removeCommand(CommandLine commandLine) {
        this.commands.remove(commandLine);
    }

    @Override
    public void setCommand(int index, CommandLine commandLine) {
        this.commands.set(index, commandLine);
    }

    @Override
    public void save() {
        try {
            Files.write(this.path, this.commands.stream().map(CommandLine::toString).collect(OperationUtil.toArrayList()));
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to save MCFunction file", e);
        }
    }

    @Override
    protected void load(@NotNull Path path, boolean createNew) {

        if (!isMCFunctionFormat(path)) {
            throw new InvalidFileFormatException("Provided file is not a mcfunction file.");
        }

        if (!Files.exists(path)) {
            if (createNew) {
                try {
                    Files.createDirectories(path.getParent());
                    Files.createFile(path);
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            } else {
                throw new IllegalArgumentException("file is not exists!");
            }
        }

        try {
            List<String> lines = Files.readAllLines(path);
            commands.clear();
            lines.stream().map(MCCommandLine::new).forEach(commands::add);
        } catch (IOException e) {
            throw new FileLoadException(e);
        } catch (CommandParseException e) {
            throw new FileLoadException(e);
        }
    }

    @Override
    public void reload(boolean loadNew) {
        this.load(this.path, loadNew);
    }

    @Override
    public void reload() {
        reload(false);
    }

    @Override
    public void execute(CommandSender sender, UniversalPlaceholder<?>... placeholders) {
        if (sender == null) {
            return;
        }

        for (CommandLine command : this.commands) {
            command.execute(sender, placeholders);
        }
    }
}
