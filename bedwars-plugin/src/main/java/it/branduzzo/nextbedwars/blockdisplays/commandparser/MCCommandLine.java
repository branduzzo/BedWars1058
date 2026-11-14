package it.branduzzo.nextbedwars.blockdisplays.commandparser;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.PlaceholderAPIPlugin;
import it.branduzzo.nextbedwars.blockdisplays.commandparser.argument.CommandArgument;
import it.branduzzo.nextbedwars.blockdisplays.commandparser.argument.MCCommandArgument;
import it.branduzzo.nextbedwars.blockdisplays.commandparser.exception.CommandParseException;
import it.branduzzo.nextbedwars.blockdisplays.commandparser.exception.InvalidCommandNameException;
import it.branduzzo.nextbedwars.blockdisplays.common.DeepCloneable;
import it.branduzzo.nextbedwars.blockdisplays.bdengine.BDEAnimationPlayer;
import it.branduzzo.nextbedwars.blockdisplays.bdengine.BDEAnimationRegistry;
import it.branduzzo.nextbedwars.blockdisplays.BlockDisplayCreator;
import it.branduzzo.nextbedwars.blockdisplays.placeholder.universal.CustomBlockPlaceholder;
import it.branduzzo.nextbedwars.blockdisplays.placeholder.universal.UniversalPlaceholder;
import it.branduzzo.nextbedwars.blockdisplays.util.OperationUtil;
import it.branduzzo.nextbedwars.blockdisplays.util.ProxyUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


/**
 * Represents a Minecraft command with its arguments.
 */

@Getter
@EqualsAndHashCode
@FieldDefaults(level = AccessLevel.PROTECTED)
public class MCCommandLine implements CommandLine, DeepCloneable<MCCommandLine> {
    List<CommandArgument> arguments = new ArrayList<>();
    String name;
    @Nullable
    String namespace;

    /**
     * Constructs a MinecraftCommandLine object from a command string.
     *
     * @param commandLine The command string.
     */
    public MCCommandLine(@NotNull String commandLine) throws CommandParseException {
        if (commandLine.startsWith("/")) {
            commandLine = commandLine.substring(1);
        }

        if (commandLine.isEmpty()) {
            throw new InvalidCommandNameException("The command name cannot be empty", commandLine);
        }


        String[] split = commandLine.split("\\s+");

        String commandName = split[0];
        String key;

        String namespaceCandidate = null;
        if (commandName.contains(":")) {
            String[] parts = commandName.split(":", 2);
            if (parts.length > 2) {
                throw new InvalidCommandNameException("The command name cannot contain more than one ':'", commandLine);
            }
            namespaceCandidate = parts[0];
            key = parts[1];
        } else {
            key = commandName;
        }

        namespace = namespaceCandidate;

        name = key;

        arguments.addAll(Arrays.stream(split).skip(1).map(argument -> new MCCommandArgument(argument, this)).collect(Collectors.toCollection(ArrayList::new)));

        if (name.length() > 256) {
            throw new InvalidCommandNameException("The maximum length of a command name should not exceed 256 characters", commandLine);
        }
    }

    public MCCommandLine(List<CommandArgument> arguments, String name, @Nullable String namespace) {
        if (name.isEmpty()) {
            throw new InvalidCommandNameException("The command name cannot be empty");
        }
        this.arguments = arguments;
        this.name = name;
        this.namespace = namespace;
    }

    public MCCommandLine(CommandLine commandLine) {
        this(commandLine.getArguments(), commandLine.getName(), commandLine.getNamespace());
    }

    /**
     * Returns the argument at the specified index.
     *
     * @param index The index of the argument.
     * @return The argument at the specified index.
     * @throws IndexOutOfBoundsException If the index is out of range.
     */
    public CommandArgument getArgument(int index) {
        try {
            return arguments.get(index);
        } catch (IndexOutOfBoundsException e) {
            return null;
        }
    }

    public CommandLine setPlaceholders(Player player, PlaceholderAPIPlugin papi) {
        String stringCommand = toString();

        if (papi == null || player == null) {
            return this;
        } else {
            stringCommand = PlaceholderAPI.setPlaceholders(player, stringCommand);
        }

        return new MCCommandLine(stringCommand);
    }

    /**
     * Returns a string representation of the command.
     *
     * @return The string representation of the command.
     */
    @Override
    public String toString() {
        return (namespace == null ? "" : namespace + ":") + name + " " + MCCommandArgument.argsToString(arguments);
    }

    @Override
    public MCCommandLine clone() {
        return new MCCommandLine(this.arguments.stream().map(DeepCloneable::tryClone).collect(OperationUtil.toArrayList()), this.name, this.namespace);
    }

    @Override
    public void execute(CommandSender sender, UniversalPlaceholder<?>... placeholders) {
        if (sender == null) {
            return;
        }

        String stringCommand = this.toString();

        for (UniversalPlaceholder<?> placeholder : placeholders) {
            stringCommand = placeholder.apply(stringCommand);
        }

        if (ProxyUtil.isProxyCommand(stringCommand)) {
            if (ProxyUtil.sendProxyCommand(sender, stringCommand)) return;
        }
        if (namespace != null && (namespace.equalsIgnoreCase("velocity") || namespace.equalsIgnoreCase("proxy") || namespace.equalsIgnoreCase("bungee") || namespace.equalsIgnoreCase("bungeecord") || namespace.equalsIgnoreCase("waterfall"))) {
            String proxied = name + " " + MCCommandArgument.argsToString(arguments);
            for (UniversalPlaceholder<?> placeholder : placeholders) proxied = placeholder.apply(proxied);
            if (ProxyUtil.sendProxyCommand(sender, proxied)) return;
        }
        String lower = stringCommand.toLowerCase();
        for (String p : new String[]{"run velocity:", "run proxy:", "run bungee:", "run bungeecord:", "run waterfall:"}) {
            if (lower.contains(p)) {
                int idx = lower.indexOf(p) + 4;
                String proxyPart = stringCommand.substring(idx).trim();
                if (ProxyUtil.sendProxyCommand(sender, proxyPart)) return;
            }
        }
        String bdeLower = lower.trim();
        if (bdeLower.startsWith("bde:sound") || bdeLower.startsWith("bde:playsound")) {
            String animName = "default";
            boolean loop = false;
            if (namespace != null && namespace.equalsIgnoreCase("bde") && (name.equalsIgnoreCase("sound") || name.equalsIgnoreCase("playsound"))) {
                if (!arguments.isEmpty()) {
                    String raw = arguments.get(0).toString();
                    for (UniversalPlaceholder<?> ph : placeholders) raw = ph.apply(raw);
                    if (!raw.isBlank()) animName = raw.trim().split("\\s+")[0];
                    if (arguments.size() >= 2) {
                        String second = arguments.get(1).toString();
                        for (UniversalPlaceholder<?> ph : placeholders) second = ph.apply(second);
                        if (second.equalsIgnoreCase("loop") || second.equalsIgnoreCase("true")) loop = true;
                    }
                    String full = stringCommand.toLowerCase();
                    if (full.contains(" loop")) loop = true;
                }
            } else {
                String[] parts = stringCommand.split("\\s+");
                if (parts.length >= 2) animName = parts[1];
                if (parts.length >= 3 && parts[2].equalsIgnoreCase("loop")) loop = true;
            }
            String blockName = null;
            Location loc = null;
            for (UniversalPlaceholder<?> ph : placeholders) {
                if (ph instanceof CustomBlockPlaceholder cbp) {
                    blockName = cbp.context().getName();
                    loc = cbp.context().getLocation();
                    break;
                }
            }
            if (loc != null && blockName != null) {
                if (loop) BDEAnimationPlayer.playSound(BlockDisplayCreator.getHost(), loc, blockName, animName, true);
                else BDEAnimationPlayer.playSound(BlockDisplayCreator.getHost(), loc, blockName, animName);
                return;
            }
            if (sender instanceof Player pl) {
                loc = pl.getLocation();
                if (blockName == null) {
                    var svc = BlockDisplayCreator.getInstance().getCustomBlockService();
                    var cb = svc.getCustomBlock(loc);
                    if (cb != null) {
                        blockName = cb.getName();
                        loc = cb.getLocation();
                    }
                }
                if (blockName != null) {
                    if (loop) BDEAnimationPlayer.playSound(BlockDisplayCreator.getHost(), loc, blockName, animName, true);
                    else BDEAnimationPlayer.playSound(BlockDisplayCreator.getHost(), loc, blockName, animName);
                    return;
                }
            }
        }
        if (bdeLower.startsWith("bde:stop")) {
            String blockName = null;
            Location loc = null;
            for (UniversalPlaceholder<?> ph : placeholders) {
                if (ph instanceof CustomBlockPlaceholder cbp) {
                    blockName = cbp.context().getName();
                    loc = cbp.context().getLocation();
                    break;
                }
            }
            if (loc != null) {
                if (blockName != null) BDEAnimationPlayer.stop(loc, blockName, "default");
                else BDEAnimationPlayer.stopAll(loc);
                return;
            }
            if (sender instanceof Player pl) {
                BDEAnimationPlayer.stopAll(pl.getLocation());
                return;
            }
        }
        if (bdeLower.startsWith("bde:animate") || bdeLower.startsWith("bde:play")) {
            String animName = "default";
            boolean loop = false;
            if (namespace != null && namespace.equalsIgnoreCase("bde") && (name.equalsIgnoreCase("animate") || name.equalsIgnoreCase("play"))) {
                if (!arguments.isEmpty()) {
                    String raw = arguments.get(0).toString();
                    for (UniversalPlaceholder<?> ph : placeholders) raw = ph.apply(raw);
                    if (!raw.isBlank()) animName = raw.trim().split("\\s+")[0];
                    if (arguments.size() >= 2) {
                        String second = arguments.get(1).toString();
                        for (UniversalPlaceholder<?> ph : placeholders) second = ph.apply(second);
                        if (second.equalsIgnoreCase("loop") || second.equalsIgnoreCase("true")) loop = true;
                    }
                    String full = stringCommand.toLowerCase();
                    if (full.contains(" loop")) loop = true;
                }
            } else {
                String[] parts = stringCommand.split("\\s+");
                if (parts.length >= 2) animName = parts[1];
                if (parts.length >= 3 && parts[2].equalsIgnoreCase("loop")) loop = true;
            }
            String blockName = null;
            Location loc = null;
            for (UniversalPlaceholder<?> ph : placeholders) {
                if (ph instanceof CustomBlockPlaceholder cbp) {
                    blockName = cbp.context().getName();
                    loc = cbp.context().getLocation();
                    break;
                }
            }
            if (loc != null && blockName != null) {
                if (loop || BDEAnimationRegistry.isLoop(blockName, animName)) {
                    BDEAnimationPlayer.play(BlockDisplayCreator.getHost(), loc, blockName, animName, true);
                } else {
                    BDEAnimationPlayer.play(BlockDisplayCreator.getHost(), loc, blockName, animName);
                }
                return;
            }
            if (sender instanceof Player pl) {
                loc = pl.getLocation();
                if (blockName == null) {
                    var svc = BlockDisplayCreator.getInstance().getCustomBlockService();
                    var cb = svc.getCustomBlock(loc);
                    if (cb != null) {
                        blockName = cb.getName();
                        loc = cb.getLocation();
                    }
                }
                if (blockName != null) {
                    if (loop || BDEAnimationRegistry.isLoop(blockName, animName)) {
                        BDEAnimationPlayer.play(BlockDisplayCreator.getHost(), loc, blockName, animName, true);
                    } else {
                        BDEAnimationPlayer.play(BlockDisplayCreator.getHost(), loc, blockName, animName);
                    }
                    return;
                }
            }
        }

        boolean handled = Bukkit.dispatchCommand(sender, stringCommand);
        if (!handled && sender instanceof Player) {
            String low = stringCommand.toLowerCase().trim();
            if (low.startsWith("queue") || low.startsWith("server ")) {
                ProxyUtil.sendProxyCommand(sender, stringCommand);
            }
        }
    }
}
