package it.branduzzo.nextbedwars.blockdisplays.command;

import com.jeff_media.customblockdata.CustomBlockData;
import it.branduzzo.nextbedwars.blockdisplays.BlockDisplayCreator;
import it.branduzzo.nextbedwars.blockdisplays.commandparser.CommandLine;
import it.branduzzo.nextbedwars.blockdisplays.commandparser.MCCommandLine;
import it.branduzzo.nextbedwars.blockdisplays.commandparser.argument.TargetSelectorType;
import it.branduzzo.nextbedwars.blockdisplays.custom.AutomaticCommandDisplaySummoner;
import it.branduzzo.nextbedwars.blockdisplays.custom.block.AbstractCustomBlock;
import it.branduzzo.nextbedwars.blockdisplays.custom.block.CustomBlockKey;
import it.branduzzo.nextbedwars.blockdisplays.custom.block.CustomBlockService;
import it.branduzzo.nextbedwars.blockdisplays.data.manager.TypeTokens;
import it.branduzzo.nextbedwars.blockdisplays.data.persistent.PersistentData;
import it.branduzzo.nextbedwars.blockdisplays.entity.GroupSummoner;
import it.branduzzo.nextbedwars.blockdisplays.file.config.value.StringMessagesValue;
import it.branduzzo.nextbedwars.blockdisplays.permission.DefaultPermissions;
import it.branduzzo.nextbedwars.blockdisplays.placeholder.universal.PlayerSkinBase64Placeholder;
import it.branduzzo.nextbedwars.blockdisplays.util.ChatUtil;
import it.branduzzo.nextbedwars.blockdisplays.util.CommandUtil;
import it.branduzzo.nextbedwars.blockdisplays.util.FoliaScheduler;
import it.branduzzo.nextbedwars.blockdisplays.util.ItemUtil;
import it.branduzzo.nextbedwars.blockdisplays.web.exception.BDEngineModelNotFoundException;
import it.branduzzo.nextbedwars.blockdisplays.web.exception.InvalidResponseException;
import it.branduzzo.nextbedwars.blockdisplays.web.serverapi.ServerApiBlockFactory;
import it.branduzzo.nextbedwars.blockdisplays.web.serverapi.ServerApiFetcher;
import it.branduzzo.nextbedwars.blockdisplays.world.WorldSelection;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;



public class BlockDisplayCreatorSpigotCommand implements TabExecutor {

    private final BlockDisplayCreator plugin;
    private final CustomBlockService service;

    public BlockDisplayCreatorSpigotCommand(BlockDisplayCreator plugin, CustomBlockService service) {
        this.plugin = plugin;
        this.service = service;
    }


    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        /*if (!sender.hasPermission(DefaultPermissions.BDC.Command.BASE)) {
            ChatUtil.sendMessage(sender, StringMessagesValue.GENERIC_NO_PERMISSION);
            return true;
        }*/

        String arg1 = null;
        String arg2 = null;
        String arg3 = null;
        String arg4 = null;
        String arg5 = null;
        String arg6 = null;
        String arg7 = null;
        Player player = null;

        try {
            arg1 = args[0];
            arg2 = args[1];
            arg3 = args[2];
            arg4 = args[3];
            arg5 = args[4];
            arg6 = args[5];
            arg7 = args[6];
        } catch (ArrayIndexOutOfBoundsException ignored) {
        }

        if (sender instanceof Player) {
            player = (Player) sender;
        }

        if (arg1 == null) {
            ChatUtil.sendMessage(sender, StringMessagesValue.GENERIC_NO_ARGS);
            return true;
        }

        switch (arg1) {
            case "animate" -> {
                if (!sender.hasPermission(DefaultPermissions.BDC.Command.CREATE_CB)) {
                    ChatUtil.sendMessage(sender, StringMessagesValue.GENERIC_NO_PERMISSION);
                    return true;
                }
                if (arg2 == null) {
                    ChatUtil.sendMessage(sender, StringMessagesValue.COMMAND_ANIMATE_USAGE);
                    return true;
                }
                String bName = arg2;
                String aName = arg3 != null ? arg3 : "default";
                boolean loop = arg4 != null && (arg4.equalsIgnoreCase("loop") || arg4.equalsIgnoreCase("true"));
                if (!(sender instanceof Player pl)) {
                    ChatUtil.sendMessage(sender, StringMessagesValue.GENERIC_ONLY_PLAYERS);
                    return true;
                }
                var frames = it.branduzzo.nextbedwars.blockdisplays.bdengine.BDEAnimationRegistry.getAnimation(bName, aName);
                if (frames == null) {
                    ChatUtil.sendMessage(sender, StringMessagesValue.COMMAND_ANIMATE_NOT_FOUND.replace("%animation%", aName).replace("%block%", bName));
                    return true;
                }
                org.bukkit.Location loc = pl.getTargetBlockExact(10) != null ? pl.getTargetBlockExact(10).getLocation() : pl.getLocation();
                var svc = this.service;
                var cb = svc.getCustomBlock(loc);
                if (cb != null) loc = cb.getLocation();
                boolean doLoop = loop || it.branduzzo.nextbedwars.blockdisplays.bdengine.BDEAnimationRegistry.isLoop(bName, aName);
                if (doLoop) it.branduzzo.nextbedwars.blockdisplays.bdengine.BDEAnimationPlayer.play(this.plugin.getPlugin(), loc, bName, aName, true);
                else it.branduzzo.nextbedwars.blockdisplays.bdengine.BDEAnimationPlayer.play(this.plugin.getPlugin(), loc, bName, aName);
                ChatUtil.sendMessage(sender, StringMessagesValue.COMMAND_ANIMATE_PLAYING.replace("%animation%", aName).replace("%block%", bName).replace("%loop%", doLoop ? StringMessagesValue.COMMAND_ANIMATE_LOOP_SUFFIX : ""));
                return true;
            }
            case "stop" -> {
                if (!(sender instanceof Player pl)) {
                    ChatUtil.sendMessage(sender, StringMessagesValue.GENERIC_ONLY_PLAYERS);
                    return true;
                }
                if (arg2 == null) {
                    ChatUtil.sendMessage(sender, StringMessagesValue.COMMAND_ANIMATE_USAGE_STOP);
                    return true;
                }
                String bName = arg2;
                String aName = arg3 != null ? arg3 : "default";
                org.bukkit.Location loc = pl.getTargetBlockExact(10) != null ? pl.getTargetBlockExact(10).getLocation() : pl.getLocation();
                var cb = this.service.getCustomBlock(loc);
                if (cb != null) loc = cb.getLocation();
                it.branduzzo.nextbedwars.blockdisplays.bdengine.BDEAnimationPlayer.stop(loc, bName, aName);
                ChatUtil.sendMessage(sender, StringMessagesValue.COMMAND_ANIMATE_STOPPED.replace("%animation%", aName).replace("%block%", bName));
                return true;
            }
            case "stopall" -> {
                if (!(sender instanceof Player pl)) {
                    ChatUtil.sendMessage(sender, StringMessagesValue.GENERIC_ONLY_PLAYERS);
                    return true;
                }
                org.bukkit.Location loc = pl.getTargetBlockExact(10) != null ? pl.getTargetBlockExact(10).getLocation() : pl.getLocation();
                var cb = this.service.getCustomBlock(loc);
                if (cb != null) loc = cb.getLocation();
                it.branduzzo.nextbedwars.blockdisplays.bdengine.BDEAnimationPlayer.stopAll(loc);
                ChatUtil.sendMessage(sender, "&aStopped all animations");
                return true;
            }
            case "sound", "playsound" -> {
                if (!sender.hasPermission(DefaultPermissions.BDC.Command.CREATE_CB)) {
                    ChatUtil.sendMessage(sender, StringMessagesValue.GENERIC_NO_PERMISSION);
                    return true;
                }
                if (arg2 == null) {
                    ChatUtil.sendMessage(sender, StringMessagesValue.COMMAND_SOUND_USAGE);
                    return true;
                }
                String bName = arg2;
                String aName = arg3 != null ? arg3 : "default";
                boolean loop = arg4 != null && (arg4.equalsIgnoreCase("loop") || arg4.equalsIgnoreCase("true"));
                if (!(sender instanceof Player pl)) {
                    ChatUtil.sendMessage(sender, StringMessagesValue.GENERIC_ONLY_PLAYERS);
                    return true;
                }
                var frames = it.branduzzo.nextbedwars.blockdisplays.bdengine.BDEAnimationRegistry.getSounds(bName, aName);
                if (frames == null) frames = it.branduzzo.nextbedwars.blockdisplays.bdengine.BDEAnimationRegistry.getAnimation(bName, aName);
                if (frames == null) {
                    ChatUtil.sendMessage(sender, StringMessagesValue.COMMAND_SOUND_NOT_FOUND.replace("%animation%", aName).replace("%block%", bName));
                    return true;
                }
                org.bukkit.Location loc = pl.getTargetBlockExact(10) != null ? pl.getTargetBlockExact(10).getLocation() : pl.getLocation();
                var cb = this.service.getCustomBlock(loc);
                if (cb != null) loc = cb.getLocation();
                if (loop) it.branduzzo.nextbedwars.blockdisplays.bdengine.BDEAnimationPlayer.playSound(this.plugin.getPlugin(), loc, bName, aName, true);
                else it.branduzzo.nextbedwars.blockdisplays.bdengine.BDEAnimationPlayer.playSound(this.plugin.getPlugin(), loc, bName, aName);
                ChatUtil.sendMessage(sender, StringMessagesValue.COMMAND_SOUND_PLAYING.replace("%animation%", aName).replace("%block%", bName).replace("%loop%", loop ? StringMessagesValue.COMMAND_ANIMATE_LOOP_SUFFIX : ""));
                return true;
            }
            case "create",
                    "spawn" -> {
                if (!sender.hasPermission(DefaultPermissions.BDC.Command.CREATE_CB)) {
                    ChatUtil.sendMessage(sender, StringMessagesValue.GENERIC_NO_PERMISSION);
                    return true;
                }
                String id = arg2;
                if (id == null || id.isBlank()) {
                    ChatUtil.sendMessage(sender, StringMessagesValue.COMMAND_CREATE_USAGE);
                    return true;
                }
                String blockName = ServerApiBlockFactory.blockNameForId(id);
                ChatUtil.sendMessage(sender, StringMessagesValue.COMMAND_CREATE_FETCHING.replace("%id%", id));
                FoliaScheduler.runAsync(this.plugin.getPlugin(), () -> {
                    try {
                        var model = ServerApiFetcher.fetch(id);
                        var file = ServerApiBlockFactory.createBlockFile(this.plugin.getPlugin(), blockName, model);
                        FoliaScheduler.runGlobal(this.plugin.getPlugin(), () -> {
                            this.service.getStorage().reloadAll(() -> {
                                ChatUtil.sendMessage(sender, StringMessagesValue.COMMAND_CREATE_CREATED.replace("%id%", id).replace("%block%", blockName).replace("%file%", file.getFileName().toString()));
                                if (sender instanceof Player p) {
                                    var ab = this.service.getStorage().getAbstractCustomBlock(blockName);
                                    if (ab != null) {
                                        ItemStack item = ab.getItem();
                                        item.setAmount(1);
                                        CommandUtil.applyPlaceholdersForItem(item, p);
                                        ItemUtil.distributeItem(p, item);
                                        ChatUtil.sendMessage(p, StringMessagesValue.COMMAND_CREATE_RECEIVED.replace("%block%", blockName));
                                    }
                                }
                            });
                        });
                    } catch (BDEngineModelNotFoundException e) {
                        FoliaScheduler.runGlobal(this.plugin.getPlugin(), () -> ChatUtil.sendMessage(sender, StringMessagesValue.COMMAND_CREATE_NOT_FOUND.replace("%id%", id).replace("%error%", e.getMessage())));
                    } catch (InvalidResponseException e) {
                        FoliaScheduler.runGlobal(this.plugin.getPlugin(), () -> ChatUtil.sendMessage(sender, StringMessagesValue.COMMAND_CREATE_FAILED.replace("%error%", e.getMessage())));
                    } catch (Exception e) {
                        FoliaScheduler.runGlobal(this.plugin.getPlugin(), () -> ChatUtil.sendMessage(sender, StringMessagesValue.COMMAND_CREATE_ERROR.replace("%error%", e.getMessage())));
                        e.printStackTrace();
                    }
                });
                return true;
            }
            case "custom-block" -> {

                if (arg2 == null) {
                    return false;
                }

                switch (arg2) {

                    case "give" -> {

                        if (!sender.hasPermission(DefaultPermissions.BDC.Command.GIVE_CB)) {
                            ChatUtil.sendMessage(sender, StringMessagesValue.GENERIC_NO_PERMISSION);
                            return true;
                        }

                        if (arg3 == null) {
                            ChatUtil.sendMessage(sender, StringMessagesValue.GENERIC_NOT_SPECIFY_BLOCK);
                            return true;
                        }

                        if (!service.getStorage().containsAbstractCustomBlock(arg3)) {
                            ChatUtil.sendMessage(sender, StringMessagesValue.COMMAND_BLOCK_NOT_EXISTS.replace("%customblock_name%", arg3));
                            return true;
                        }

                        AbstractCustomBlock abstractCustomBlock = service.getStorage().getAbstractCustomBlock(arg3);
                        ItemStack customBlockItem = abstractCustomBlock.getItem();

                        byte amount = 1;

                        if (arg5 != null) {
                            try {
                                amount = Byte.parseByte(arg5);
                            } catch (NumberFormatException ignore) {
                                ChatUtil.sendMessage(sender, StringMessagesValue.GENERIC_INVALID_NUMBER);
                                return true;
                            }

                            if (amount < 1) {
                                ChatUtil.sendMessage(sender, StringMessagesValue.GENERIC_INVALID_NUMBER);
                                return true;
                            }
                        }

                        customBlockItem.setAmount(amount);

                        if (arg4 == null) {
                            if (player == null) {
                                ChatUtil.sendMessage(sender, StringMessagesValue.COMMAND_CUSTOM_BLOCK_GIVE_NO_PLAYER);
                                return true;
                            } else {
                                CommandUtil.applyPlaceholdersForItem(customBlockItem, player);
                                CommandUtil.applyPlaceholdersForCommand(abstractCustomBlock, customBlockItem, player);

                                ItemUtil.distributeItem(player, customBlockItem);

                                ChatUtil.sendMessage(player,
                                        StringMessagesValue.COMMAND_CUSTOM_BLOCK_GIVE_PLAYER_RECEIVE
                                                .replace("%customblock_name%", arg3)
                                                .replace("%amount%", String.valueOf(amount))
                                );
                            }
                        } else {
                            switch (arg4) {
                                case "@a" -> {
                                    for (Player recipient : Bukkit.getOnlinePlayers()) {
                                        CommandUtil.applyPlaceholdersForItem(customBlockItem, recipient);
                                        CommandUtil.applyPlaceholdersForCommand(abstractCustomBlock, customBlockItem, recipient);

                                        ItemUtil.distributeItem(recipient, customBlockItem);
                                    }
                                    ChatUtil.sendMessage(sender, "&bYou have given the &l%s&ox%s&r&b block to all players", arg3, amount);
                                    return true;
                                }
                                case "@s" -> {
                                    if (player == null) {
                                        ChatUtil.sendMessage(sender, StringMessagesValue.GENERIC_ONLY_PLAYERS_TARGET);
                                        return true;
                                    }

                                    CommandUtil.applyPlaceholdersForItem(customBlockItem, player);
                                    CommandUtil.applyPlaceholdersForCommand(abstractCustomBlock, customBlockItem, player);

                                    ItemUtil.distributeItem(player, customBlockItem);

                                    ChatUtil.sendMessage(sender, "&bYou have received the &l%s&ox%s&r&b block", arg3, amount);
                                    return true;
                                }
                                case "@r" -> {
                                    Player randomPlayer = TargetSelectorType.getRandomPlayer();

                                    if (randomPlayer == null) {
                                        ChatUtil.sendMessage(sender, StringMessagesValue.GENERIC_SERVER_EMPTY);
                                        return true;
                                    }

                                    CommandUtil.applyPlaceholdersForItem(customBlockItem, randomPlayer);
                                    CommandUtil.applyPlaceholdersForCommand(abstractCustomBlock, customBlockItem, randomPlayer);

                                    ItemUtil.distributeItem(randomPlayer, customBlockItem);

                                    ChatUtil.sendMessage(sender, "&bYou have given the &l%s&ox%s&r&b block to random player %s", arg3, amount, randomPlayer.getName());
                                    return true;
                                }
                                case "@p" -> {
                                    if (player == null) {
                                        ChatUtil.sendMessage(sender, StringMessagesValue.GENERIC_ONLY_PLAYERS_TARGET);
                                        return true;
                                    }

                                    Player nearestPlayer = TargetSelectorType.getNearestPlayer(player.getLocation(), player);

                                    CommandUtil.applyPlaceholdersForItem(customBlockItem, nearestPlayer);
                                    CommandUtil.applyPlaceholdersForCommand(abstractCustomBlock, customBlockItem, nearestPlayer);

                                    ItemUtil.distributeItem(nearestPlayer, customBlockItem);

                                    ChatUtil.sendMessage(sender, "&bYou have given the &l%s&ox%s&r&b block to nearest player %s", arg3, amount, nearestPlayer.getName());
                                    return true;
                                }
                                default -> {
                                    Player recipient = Bukkit.getPlayer(arg4);

                                    if (recipient == null) {
                                        ChatUtil.sendMessage(sender, StringMessagesValue.GENERIC_INVALID_SELECTOR);
                                        return true;
                                    }

                                    CommandUtil.applyPlaceholdersForItem(customBlockItem, recipient);
                                    CommandUtil.applyPlaceholdersForCommand(abstractCustomBlock, customBlockItem, recipient);

                                    ItemUtil.distributeItem(recipient, customBlockItem);

                                    ChatUtil.sendMessage(sender, "&bYou have given the &l%s&ox%s&r&b block to player %s", arg3, amount, recipient.getName());
                                    return true;
                                }
                            }
                        }
                    }
                    /*case "editfile" -> {

                        if (arg3 == null) {
                            ChatUtil.sendMessage(sender, StringMessagesValue.GENERIC_NOT_SPECIFY_BLOCK);
                            return true;
                        }

                        if (arg4 == null) {
                            ChatUtil.sendMessage(sender, StringMessagesValue.GENERIC_NOT_SPECIFY_PARAMETER);
                            return true;
                        }

                        switch (arg4) {
                            case "sides-count" -> {
                                if (arg5 == null) {
                                    return false;
                                }

                                int value = 4;

                                try {
                                    value = Integer.parseInt(arg5);
                                } catch (NumberFormatException ignored) {
                                }

                                setCustomBlockParameter(arg3, List.of(arg4), value, sender);
                            }
                            case "display" -> {
                                if (arg5 == null) {
                                    return false;
                                }

                                switch (arg5) {
                                    case "glowing" -> {
                                        if (arg6 == null) {
                                            return false;
                                        }

                                        setCustomBlockParameter(arg3, List.of(arg4, arg5), arg6, sender);
                                    }
                                    case "glow-color-override" -> {
                                        if (arg6 == null) {
                                            return false;
                                        }

                                        int value = 1;

                                        try {
                                            value = Integer.parseInt(arg6);
                                        } catch (NumberFormatException ignored) {
                                        }

                                        setCustomBlockParameter(arg3, List.of(arg4, arg5), value, sender);
                                    }
                                }
                            }
                            case "central-material" -> {

                                if (arg5 == null) {
                                    return false;
                                }

                                setCustomBlockParameter(arg3, List.of(arg4), arg5, sender);

                            }
                            case "item" -> {

                                if (arg5 == null) {
                                    return false;
                                }

                                switch (arg5) {
                                    case "material" -> {
                                        if (arg6 == null) {
                                            return false;
                                        }

                                        setCustomBlockParameter(arg3, List.of(arg4, arg5), arg6, sender);
                                    }
                                    case "name" -> {
                                        if (arg6 == null) {
                                            return false;
                                        }

                                        String name = String.join(" ", Arrays.copyOfRange(args, 5, args.length));

                                        setCustomBlockParameter(arg3, List.of(arg4, arg5), name, sender);
                                    }
                                    case "enchantments" -> {
                                        if (arg6 == null) {
                                            return false;
                                        }

                                        int level = 1;

                                        try {
                                            level = Integer.parseInt(
                                                    Optional.ofNullable(arg7).orElse("1")
                                            );
                                        } catch (NumberFormatException ignored) {
                                        }

                                        setCustomBlockParameter(arg3, List.of(arg4, arg5, arg6), level, sender);
                                    }
                                    case "item-flags" -> {
                                        if (arg6 == null) {
                                            return false;
                                        }

                                        setCustomBlockParameter(arg3, List.of(arg4, arg5), List.of(arg6), sender);
                                    }
                                    case "lore" -> {

                                        if (arg6 == null) {
                                            return false;
                                        }

                                        String lore = String.join(" ", Arrays.copyOfRange(args, 5, args.length));

                                        setCustomBlockParameter(arg3, List.of(arg4, arg5), List.of(lore), sender);
                                    }
                                    case "skullmeta" -> {

                                        if (arg6 == null) {
                                            return false;
                                        }

                                        switch (arg6) {
                                            case "url" -> {

                                                if (arg7 == null) {
                                                    return false;
                                                }

                                                setCustomBlockParameter(arg3, List.of(arg4, arg5, arg6), arg7, sender);
                                            }
                                            default -> {
                                                return false;
                                            }
                                        }
                                    }
                                    default -> {
                                        return false;
                                    }
                                }
                            }
                            *//*case "interaction" -> {

                                if (arg5 == null) {
                                    return false;
                                }

                                switch (arg5) {
                                    case "width", "height" -> {

                                        if (arg6 == null) {
                                            return false;
                                        }

                                        float value = 1.001F;

                                        try {
                                            value = Float.parseFloat(arg6);
                                        } catch (NumberFormatException ignored) {
                                        }

                                        setCustomBlockParameter(List.of(arg1, arg3, arg4, arg5), value, sender);
                                    }
                                    case "command" -> {

                                        if (arg6 == null) {
                                            return false;
                                        }

                                        String interactionCommand = String.join(" ", Arrays.copyOfRange(args, 5, args.length));

                                        setCustomBlockParameter(List.of(arg1, arg3, arg4, arg5), List.of(interactionCommand), sender);
                                    }
                                    case "command-source" -> {

                                        if (arg6 == null) {
                                            return false;
                                        }

                                        setCustomBlockParameter(List.of(arg1, arg3, arg4, arg5), arg6, sender);
                                    }
                                }
                            }*//*
                            case "sound" -> {
                                if (arg5 == null) {
                                    return false;
                                }

                                switch (arg5) {
                                    case "break", "place" -> {
                                        if (arg6 == null) {
                                            return false;
                                        }

                                        switch (arg6) {
                                            case "volume", "pitch" -> {
                                                if (arg7 == null) {
                                                    return false;
                                                }

                                                float value = 1F;

                                                try {
                                                    value = Float.parseFloat(arg7);
                                                } catch (NumberFormatException ignored) {
                                                }

                                                setCustomBlockParameter(arg3, List.of(arg4, arg5, arg6), value, sender);
                                            }
                                            case "sound-type" -> {
                                                if (arg7 == null) {
                                                    return false;
                                                }

                                                setCustomBlockParameter(arg3, List.of(arg4, arg5, arg6), arg7, sender);
                                            }
                                        }
                                    }
                                }
                            }
                            default -> {
                                return false;
                            }
                        }
                    }*/
                    default -> {
                        return false;
                    }
                }
            }
            case "erasecbdata" -> {

                if (!sender.hasPermission(DefaultPermissions.BDC.Command.ERASE_CB_DATA)) {
                    ChatUtil.sendMessage(sender, StringMessagesValue.GENERIC_NO_PERMISSION);
                    return true;
                }

                if (player == null) {
                    ChatUtil.sendMessage(sender, StringMessagesValue.GENERIC_ONLY_PLAYERS_TARGET);
                    return true;
                }

                if (args.length != 7) {
                    ChatUtil.sendMessage(sender, StringMessagesValue.GENERIC_INVALID_COORDINATES);
                    return true;
                }

                try {
                    eraseCbData(new BoundingBox
                                    (
                                            Double.valueOf(arg2),
                                            Double.valueOf(arg3),
                                            Double.valueOf(arg4),
                                            Double.valueOf(arg5),
                                            Double.valueOf(arg6),
                                            Double.valueOf(arg7)
                                    ),
                            player
                    );
                } catch (NumberFormatException exception) {
                    ChatUtil.sendMessage(sender, StringMessagesValue.GENERIC_INVALID_NUMBER_FORMAT);
                    return true;
                }
            }
            case "reload" -> {

                if (!sender.hasPermission(DefaultPermissions.BDC.Command.RELOAD)) {
                    ChatUtil.sendMessage(sender, StringMessagesValue.GENERIC_NO_PERMISSION);
                    return true;
                }

                if (arg2 == null) {
                    plugin.reloadConfig();
                    ChatUtil.sendMessage(sender, StringMessagesValue.COMMAND_RELOAD_SUCCESS);
                } else {
                    this.plugin.getCustomBlockService().getStorage().reload(arg2);
                    ChatUtil.sendMessage(sender, StringMessagesValue.COMMAND_RELOAD_BLOCK_SUCCESS.replace("%block%", arg2));
                }
            }
            default -> {
                return false;
            }
        }


        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        List<String> result = new ArrayList<>();
        /*Material[] materials = Material.values();
        List<Material> blockMaterials = Arrays.stream(materials).filter(Material::isBlock).toList();
        List<Enchantment> enchantments = List.of(Enchantment.values());
        List<ItemFlag> itemFlags = List.of(ItemFlag.values());
        Sound[] sounds = Sound.values();*/

        List<String> blocksNames = service.getStorage().getNames();

        String arg1 = null;
        String arg2 = null;
        String arg3 = null;
        String arg4 = null;
        String arg5 = null;
        String arg6 = null;
        String arg7 = null;
        Player player = null;

        try {
            arg1 = args[0];
            arg2 = args[1];
            arg3 = args[2];
            arg4 = args[3];
            arg5 = args[4];
            arg6 = args[5];
            arg7 = args[6];
        } catch (ArrayIndexOutOfBoundsException ignored) {
        }

        if (sender instanceof Player) {
            player = (Player) sender;
        }

        String erasecbdata = "erasecbdata";
        if (args.length == 1) {
            StringUtil.copyPartialMatches(arg1, List.of(
                    "custom-block",
                    erasecbdata,
                    "reload",
                    "create",
                    "spawn",
                    "animate",
                    "stop",
                    "stopall",
                    "sound",
                    "playsound"
            ), result);
        } else if (args.length == 2) {
            if (arg1.equals(erasecbdata)) {
                if (player != null) {
                    int x = player.getLocation().getBlockX();
                    int y = player.getLocation().getBlockY();
                    int z = player.getLocation().getBlockZ();

                    StringUtil.copyPartialMatches(arg2, Collections.singletonList(x + " " + y + " " + z), result);
                }
            } else if (arg1.equals("custom-block")) {
                StringUtil.copyPartialMatches(arg2, List.of(
                        "give"/*,
                        "set"*/
                ), result);
            } else if (arg1.equals("reload")) {
                StringUtil.copyPartialMatches(arg2, blocksNames, result);
            } else if (arg1.equals("animate")) {
                StringUtil.copyPartialMatches(arg2, blocksNames, result);
            }
        } else if (args.length == 3) {
            if (arg1.equals(erasecbdata)) {

                if (player != null) {
                    int y = player.getLocation().getBlockY();
                    int z = player.getLocation().getBlockZ();

                    StringUtil.copyPartialMatches(arg3, Collections.singletonList(y + " " + z), result);
                }
            } else if (arg2.equals("give")/* || arg2.equals("set")*/) {
                StringUtil.copyPartialMatches(arg3, blocksNames, result);
            }
        } else if (args.length == 4) {
            if (arg1.equals(erasecbdata)) {
                if (player != null) {
                    int z = player.getLocation().getBlockZ();

                    StringUtil.copyPartialMatches(arg4, Collections.singletonList("" + z), result);
                }
            } /*else if (arg2.equals("set")) {

                StringUtil.copyPartialMatches(arg4, List.of(
                        "central-material",
                        "item",
                        *//*"interactions",*//*
                        "sound",
                        "display",
                        "sides-count"
                ), result);
            }*/ else if (arg2.equals("give")) {

                List<String> tabComplete = new ArrayList<>();

                tabComplete.addAll(List.of(
                        "@a",
                        "@p",
                        "@r",
                        "@s"
                ));

                tabComplete.addAll(Bukkit.getOnlinePlayers().stream().map(Player::getName).toList());

                StringUtil.copyPartialMatches(arg4, tabComplete, result);
            }
        } else if (args.length == 5) {
            if (arg1.equals(erasecbdata)) {
                if (player != null) {
                    int x = player.getLocation().getBlockX();
                    int y = player.getLocation().getBlockY();
                    int z = player.getLocation().getBlockZ();

                    StringUtil.copyPartialMatches(arg5, Collections.singletonList(x + " " + y + " " + z), result);
                }
            } /*else if (arg4.equals("central-material")) {
                StringUtil.copyPartialMatches(arg5, blockMaterials.stream().map(Object::toString).toList(), result);
            } else if (arg4.equals("item")) {
                StringUtil.copyPartialMatches(arg5, List.of(
                        "material",
                        "name",
                        "enchantments",
                        "item-flags",
                        "lore",
                        "skullmeta"
                ), result);
            } *//*else if (arg4.equals("interactions")) {
                StringUtil.copyPartialMatches(arg5, List.of(
                        "width",
                        "height",
                        "command",
                        "command-source"
                ), result);
            }*//* else if (arg4.equals("display")) {
                StringUtil.copyPartialMatches(arg5, List.of(
                        "glowing",
                        "glow-color-override"
                ), result);
            } else if (arg4.equals("sound")) {
                StringUtil.copyPartialMatches(arg5, List.of(
                        "break",
                        "place"
                ), result);
            }*/
        } else if (args.length == 6) {
            if (arg1.equals(erasecbdata)) {
                if (player != null) {
                    int y = player.getLocation().getBlockY();
                    int z = player.getLocation().getBlockZ();

                    StringUtil.copyPartialMatches(arg6, Collections.singletonList(y + " " + z), result);
                }

            } /*else if (arg5.equals("material")) {
                StringUtil.copyPartialMatches(arg6, Arrays.stream(materials).map(Object::toString).toList(), result);
            } else if (arg5.equals("enchantments")) {
                StringUtil.copyPartialMatches(arg6, enchantments.stream().map(enchantment -> enchantment.getKey().getKey().toUpperCase()).toList(), result);
            } else if (arg5.equals("item-flags")) {
                StringUtil.copyPartialMatches(arg6, itemFlags.stream().map(Object::toString).toList(), result);
            } else if (arg5.equals("skullmeta")) {
                StringUtil.copyPartialMatches(arg6, List.of("url"), result);
            } else if (arg5.equals("command-source")) {
                StringUtil.copyPartialMatches(arg6, List.of("player", "console"), result);
            } else if (arg5.equals("glowing")) {
                StringUtil.copyPartialMatches(arg6, List.of("true", "false"), result);
            } else if (arg5.equals("break") || arg5.equals("place")) {
                StringUtil.copyPartialMatches(arg6, List.of("sound-type", "volume", "pitch"), result);
            }*/
        } else if (args.length == 7) {
            if (arg1.equals(erasecbdata)) {
                if (player != null) {
                    int z = player.getLocation().getBlockZ();

                    StringUtil.copyPartialMatches(arg7, Collections.singletonList("" + z), result);
                }
            } /*else if (arg6.equals("sound-type")) {
                StringUtil.copyPartialMatches(arg7, Arrays.stream(sounds).map(Enum::name).toList(), result);
            }*/
        }


        return result;
    }


    private void eraseCbData(BoundingBox boundingBox, Player sender) {
        WorldSelection worldSelection = new WorldSelection(boundingBox, sender.getWorld());

        List<Entity> displayEntities = new ArrayList<>();
        List<Entity> interactions = new ArrayList<>();
        List<Entity> collisions = new ArrayList<>();


        sender.getWorld().getNearbyEntities(
                boundingBox,
                entity -> entity instanceof Display || entity instanceof Interaction || entity instanceof Shulker
        ).forEach(entity -> {
            if (entity instanceof Display) {
                displayEntities.add(entity);
            } else if (entity instanceof Interaction) {
                interactions.add(entity);
            } else if (entity instanceof Shulker) {
                collisions.add(entity);
            }
        });


        final int[] blocksCount = {0};

        worldSelection.getLocations().forEach(location -> {
                    CustomBlockData customBlockData = new CustomBlockData(location.getBlock(), BlockDisplayCreator.getHost());

                    if (customBlockData.has(CustomBlockKey.NAME)) {
                        customBlockData.remove(CustomBlockKey.NAME);
                        customBlockData.remove(CustomBlockKey.SERVICE_CLASS);
                        customBlockData.remove(CustomBlockKey.LOCATION);
                        customBlockData.remove(CustomBlockKey.CUSTOM_BLOCK_UUID);
                        customBlockData.remove(CustomBlockKey.DISPLAY_UUID);
                        customBlockData.remove(CustomBlockKey.INTERACTION_UUID);
                        customBlockData.remove(CustomBlockKey.COLLISION_UUID);
                        customBlockData.remove(CustomBlockKey.INTERACTION_IDENTIFIER);
                        customBlockData.remove(CustomBlockKey.COLLISION_IDENTIFIER);
                        customBlockData.remove(CustomBlockKey.BLOCK_ROTATION);
                        customBlockData.remove(CustomBlockKey.ITEM);
                        customBlockData.remove(CustomBlockKey.DISPLAY_SPAWN_COMMAND);
                        blocksCount[0]++;
                    }
                }
        );

        displayEntities.forEach(Entity::remove);
        interactions.forEach(Entity::remove);
        collisions.forEach(Entity::remove);

        ChatUtil.sendMessage(sender, "&6Killed %d display entities", displayEntities.size());
        ChatUtil.sendMessage(sender, "&6Killed %d interaction entities", interactions.size());
        ChatUtil.sendMessage(sender, "&6Killed %d collision entities", collisions.size());
        ChatUtil.sendMessage(sender, "&6Cleared %d custom blocks", blocksCount[0]);
    }
}

