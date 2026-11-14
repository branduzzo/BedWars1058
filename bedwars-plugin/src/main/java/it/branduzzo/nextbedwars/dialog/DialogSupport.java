package it.branduzzo.nextbedwars.dialog;

import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import it.branduzzo.nextbedwars.BedWars;
import it.branduzzo.nextbedwars.api.arena.IArena;
import it.branduzzo.nextbedwars.api.arena.team.ITeam;
import it.branduzzo.nextbedwars.api.events.spectator.SpectatorTeleportToPlayerEvent;
import it.branduzzo.nextbedwars.api.language.Language;
import it.branduzzo.nextbedwars.api.language.Messages;
import it.branduzzo.nextbedwars.arena.Arena;
import it.branduzzo.nextbedwars.configuration.Sounds;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickCallback;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public final class DialogSupport {

    private static final int DIALOG_PROTOCOL = 771;
    private static final boolean AVAILABLE = detect();

    private DialogSupport() {
    }

    private static boolean detect() {
        try {
            Class.forName("io.papermc.paper.dialog.Dialog");
            Class.forName("io.papermc.paper.registry.data.dialog.DialogBase");
            return true;
        } catch (Throwable throwable) {
            return false;
        }
    }

    public static boolean canUse(Player player) {
        if (!AVAILABLE || player == null) {
            return false;
        }
        if (isBedrock(player)) {
            return false;
        }
        int protocol = player.getProtocolVersion();
        return protocol == -1 || protocol >= DIALOG_PROTOCOL;
    }

    private static boolean logged;

    private static boolean isBedrock(Player player) {
        String brand = player.getClientBrandName();
        if (brand != null && brand.toLowerCase(Locale.ROOT).contains("geyser")) {
            return true;
        }
        if (Bukkit.getPluginManager().getPlugin("floodgate") == null) {
            return false;
        }
        try {
            Class<?> api = Class.forName("org.geysermc.floodgate.api.FloodgateApi");
            Object instance = api.getMethod("getInstance").invoke(null);
            return (boolean) api.getMethod("isFloodgatePlayer", java.util.UUID.class)
                    .invoke(instance, player.getUniqueId());
        } catch (Throwable throwable) {
            return false;
        }
    }

    private static Component legacy(String text) {
        if (text == null) {
            return Component.empty();
        }
        return LegacyComponentSerializer.legacySection().deserialize(
                org.bukkit.ChatColor.translateAlternateColorCodes('&', text));
    }

    private static Component text(Player player, String path) {
        return legacy(Language.getMsg(player, path));
    }

    private interface ClickAction {
        void run(Player player);
    }

    private static void onPlayerThread(Player player, Runnable action) {
        it.branduzzo.nextbedwars.util.Sched.runAtLocation(BedWars.plugin, player.getLocation(), action);
    }

    private static ActionButton button(Component label, Component tooltip, int width, ClickAction action) {
        return ActionButton.builder(label)
                .tooltip(tooltip)
                .width(width)
                .action(DialogAction.customClick((response, audience) -> {
                    if (audience instanceof Player clicker) {
                        action.run(clicker);
                    }
                }, ClickCallback.Options.builder().uses(1).build()))
                .build();
    }

    public static boolean openArenaSelector(Player player, String group) {
        if (!canUse(player)) {
            return false;
        }
        List<IArena> arenas = new ArrayList<>();
        for (IArena arena : Arena.getArenas()) {
            if (group == null || group.equalsIgnoreCase("default") || arena.getGroup().equalsIgnoreCase(group)) {
                arenas.add(arena);
            }
        }
        List<ActionButton> buttons = new ArrayList<>();
        for (IArena arena : Arena.getSorted(arenas)) {
            Component label = legacy(Language.getMsg(player, Messages.ARENA_GUI_ARENA_CONTENT_NAME)
                    .replace("{name}", arena.getDisplayName())
                    .replace("{map_name}", arena.getArenaName()));
            Component tooltip = legacy(arena.getDisplayStatus(Language.getPlayerLanguage(player)))
                    .append(Component.text(" " + arena.getPlayers().size() + "/" + arena.getMaxPlayers(), NamedTextColor.WHITE));
            buttons.add(button(label, tooltip, 200, clicker -> onPlayerThread(clicker, () -> {
                Sounds.playSound("arena-selector-open", clicker);
                clicker.performCommand("bw join " + arena.getArenaName());
            })));
            if (buttons.size() >= 12) {
                break;
            }
        }
        if (buttons.isEmpty()) {
            return false;
        }
        Component body = Component.text(arenas.size() + " arene disponibili", NamedTextColor.GRAY);
        Dialog dialog = Dialog.create(builder -> builder.empty()
                .base(DialogBase.builder(text(player, Messages.ARENA_GUI_INV_NAME))
                        .body(Collections.singletonList(DialogBody.plainMessage(body, 300)))
                        .canCloseWithEscape(true)
                        .pause(false)
                        .build())
                .type(DialogType.multiAction(buttons).columns(2).build()));
        player.showDialog(dialog);
        return true;
    }

    public static boolean openTest(Player player) {
        if (!canUse(player)) {
            return false;
        }
        Dialog dialog = Dialog.create(builder -> builder.empty()
                .base(DialogBase.builder(Component.text("NextBedWars"))
                        .body(Collections.singletonList(DialogBody.plainMessage(Component.text("Dialog attivo"), 250)))
                        .canCloseWithEscape(true)
                        .pause(false)
                        .build())
                .type(DialogType.notice()));
        player.showDialog(dialog);
        return true;
    }

    public static boolean openStats(Player player) {
        if (!canUse(player)) {
            return false;
        }
        if (BedWars.config.getYml().getConfigurationSection(it.branduzzo.nextbedwars.api.configuration.ConfigPath.GENERAL_CONFIGURATION_STATS_PATH) == null) {
            return false;
        }
        List<DialogBody> body = new ArrayList<>();
        for (String key : BedWars.config.getYml()
                .getConfigurationSection(it.branduzzo.nextbedwars.api.configuration.ConfigPath.GENERAL_CONFIGURATION_STATS_PATH).getKeys(false)) {
            if (it.branduzzo.nextbedwars.api.configuration.ConfigPath.GENERAL_CONFIGURATION_STATS_GUI_SIZE.contains(key)) {
                continue;
            }
            String name = it.branduzzo.nextbedwars.arena.Misc.replaceStatsPlaceholders(player,
                    Language.getMsg(player, Messages.PLAYER_STATS_GUI_PATH + "-" + key + "-name"), true);
            body.add(DialogBody.plainMessage(legacy(name), 300));
            for (String line : Language.getList(player, Messages.PLAYER_STATS_GUI_PATH + "-" + key + "-lore")) {
                body.add(DialogBody.plainMessage(legacy(
                        it.branduzzo.nextbedwars.arena.Misc.replaceStatsPlaceholders(player, line, true)), 300));
            }
        }
        if (body.isEmpty()) {
            return false;
        }
        Dialog dialog = Dialog.create(builder -> builder.empty()
                .base(DialogBase.builder(text(player, Messages.PLAYER_STATS_GUI_INV_NAME))
                        .body(body)
                        .canCloseWithEscape(true)
                        .pause(false)
                        .build())
                .type(DialogType.notice()));
        player.showDialog(dialog);
        return true;
    }

    public static boolean openSpectatorTeleporter(Player player) {
        if (!canUse(player)) {
            return false;
        }
        IArena arena = Arena.getArenaByPlayer(player);
        if (arena == null || !arena.isSpectator(player)) {
            return false;
        }
        List<ActionButton> buttons = new ArrayList<>();
        for (Player target : arena.getPlayers()) {
            ITeam team = arena.getTeam(target);
            Component label = Component.text(target.getName(), NamedTextColor.WHITE);
            if (team != null) {
                label = label.append(LegacyComponentSerializer.legacySection()
                        .deserialize(" " + team.getColor().chat() + team.getDisplayName(Language.getPlayerLanguage(player))));
            }
            Component tooltip = Component.text("HP: " + (int) Math.ceil(target.getHealth()) + " ❤", NamedTextColor.RED);
            final Player victim = target;
            buttons.add(button(label, tooltip, 150, clicker -> onPlayerThread(clicker, () -> teleport(clicker, victim))));
            if (buttons.size() >= 21) {
                break;
            }
        }
        if (buttons.isEmpty()) {
            return false;
        }
        Dialog dialog = Dialog.create(builder -> builder.empty()
                .base(DialogBase.builder(text(player, Messages.ARENA_SPECTATOR_TELEPORTER_GUI_NAME))
                        .canCloseWithEscape(true)
                        .pause(false)
                        .build())
                .type(DialogType.multiAction(buttons).columns(3).build()));
        player.showDialog(dialog);
        return true;
    }

    private static void teleport(Player spectator, Player target) {
        IArena arena = Arena.getArenaByPlayer(spectator);
        SpectatorTeleportToPlayerEvent event = new SpectatorTeleportToPlayerEvent(spectator, target, arena);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled() || !target.isOnline() || target.isDead()) {
            return;
        }
        spectator.teleport(target);
        Sounds.playSound("spectator-gui-click", spectator);
    }
}
