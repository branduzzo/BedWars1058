package it.branduzzo.nextbedwars.blockdisplays.custom;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import it.branduzzo.nextbedwars.blockdisplays.BlockDisplayCreator;
import it.branduzzo.nextbedwars.blockdisplays.commandparser.CommandLine;
import it.branduzzo.nextbedwars.blockdisplays.commandparser.CommandRegistry;
import it.branduzzo.nextbedwars.blockdisplays.commandparser.SummonCommandLine;
import it.branduzzo.nextbedwars.blockdisplays.common.DeepCloneable;
import it.branduzzo.nextbedwars.blockdisplays.entity.CommandSummoner;
import it.branduzzo.nextbedwars.blockdisplays.entity.display.TranslationVectorAdjustable;
import it.branduzzo.nextbedwars.blockdisplays.permission.DefaultPermissions;
import it.branduzzo.nextbedwars.blockdisplays.rotation.DirectedVector;
import it.branduzzo.nextbedwars.blockdisplays.util.LocationUtil;
import it.branduzzo.nextbedwars.blockdisplays.util.OperationUtil;
import it.branduzzo.nextbedwars.blockdisplays.world.TransformationBuilder;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.ListIterator;
import java.util.function.Predicate;

@Getter
@Setter
@EqualsAndHashCode
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AutomaticCommandDisplaySummoner implements CommandSummoner<Display>, TranslationVectorAdjustable, DeepCloneable<AutomaticCommandDisplaySummoner> {
    List<CommandLine> commands;
    DirectedVector translation;
    boolean usePlaceholder;

    public AutomaticCommandDisplaySummoner(List<CommandLine> commands, DirectedVector translation) {
        this.commands = commands;
        this.translation = translation;
    }

    public AutomaticCommandDisplaySummoner(List<CommandLine> commands) {
        this.commands = commands;
    }

    @Override
    @SuppressWarnings("all")
    public List<Display> summon(@NotNull Location location, @NotNull Entity commandSender, @Nullable Predicate<Display> filter) {
        prepareDisplayCommands(location, this.commands);

        World world = location.getWorld();

        if (world == null) {
            return List.of();
        }

        boolean summonPermission = commandSender.hasPermission(DefaultPermissions.Vanilla.Command.SUMMON);
        boolean functionPermission = commandSender.hasPermission(DefaultPermissions.Vanilla.Command.FUNCTION);

        giveCommandPermissions(commandSender);

        executeDisplayCommands(commands, commandSender);

        commandSender.addAttachment(BlockDisplayCreator.getHost(), DefaultPermissions.Vanilla.Command.SUMMON, summonPermission);
        commandSender.addAttachment(BlockDisplayCreator.getHost(), DefaultPermissions.Vanilla.Command.FUNCTION, functionPermission);


        return LocationUtil.getEntities(location, entity -> {
                    if (entity instanceof Display display) {
                        if (this.translation != null) {
                            display.setTransformation(new TransformationBuilder(display).addTranslation(this.translation.clone()).build());
                        }

                        if (filter != null) {
                            return filter.test(display);
                        }
                        return true;
                    }
                    return false;
                })
                .stream()
                .map(entity -> (Display) entity)
                .collect(OperationUtil.toArrayList());
    }

    private void prepareDisplayCommands(Location location, List<CommandLine> displayCommands) {
        CommandRegistry registry = new CommandRegistry();

        ListIterator<CommandLine> iterator = displayCommands.listIterator();
        while (iterator.hasNext()) {
            CommandLine line = iterator.next();

            SummonCommandLine summon = (line instanceof SummonCommandLine sc)
                    ? sc
                    : (registry.getCommand(line.toString()) instanceof SummonCommandLine rc ? rc : null);

            if (summon != null) {
                summon.setCoordinates(location);
                iterator.set(summon);
            }
        }
    }

    private void executeDisplayCommands(List<CommandLine> displayCommand, Entity commandSender) {
        displayCommand.forEach(command -> command.execute(commandSender));
    }

    private void giveCommandPermissions(Entity commandSender) {
        commandSender.addAttachment(BlockDisplayCreator.getHost(), DefaultPermissions.Vanilla.Command.SUMMON, true);
        commandSender.addAttachment(BlockDisplayCreator.getHost(), DefaultPermissions.Vanilla.Command.FUNCTION, true);
    }

    @Override
    public AutomaticCommandDisplaySummoner clone() {
        AutomaticCommandDisplaySummoner cloned = new AutomaticCommandDisplaySummoner(DeepCloneable.tryCloneList(this.commands));
        if (translation != null) {
            cloned.setTranslation(this.translation.clone());
        }
        cloned.setUsePlaceholder(this.usePlaceholder);
        return cloned;
    }
}
