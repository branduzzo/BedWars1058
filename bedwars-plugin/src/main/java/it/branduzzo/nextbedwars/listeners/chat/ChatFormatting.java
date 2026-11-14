/*
 * BedWars1058 - A bed wars mini-game.
 * Copyright (C) 2021 Andrei Dascălu
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 * Contact e-mail: andrew.dascalu@gmail.com
 */

package it.branduzzo.nextbedwars.listeners.chat;

import it.branduzzo.nextbedwars.BedWars;
import it.branduzzo.nextbedwars.api.arena.GameState;
import it.branduzzo.nextbedwars.api.arena.IArena;
import it.branduzzo.nextbedwars.api.arena.team.ITeam;
import it.branduzzo.nextbedwars.api.configuration.ConfigPath;
import it.branduzzo.nextbedwars.api.language.Language;
import it.branduzzo.nextbedwars.api.language.Messages;
import it.branduzzo.nextbedwars.api.server.ServerType;
import it.branduzzo.nextbedwars.arena.Arena;
import it.branduzzo.nextbedwars.commands.shout.ShoutCommand;
import it.branduzzo.nextbedwars.configuration.Permissions;
import it.branduzzo.nextbedwars.support.papi.SupportPAPI;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static it.branduzzo.nextbedwars.BedWars.*;
import static it.branduzzo.nextbedwars.api.language.Language.getMsg;
import static it.branduzzo.nextbedwars.api.language.Language.getPlayerLanguage;

public class ChatFormatting implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void onChat(AsyncPlayerChatEvent e) {
        if (e == null) return;
        Player p = e.getPlayer();

        // in shared mode we don't want messages from outside the arena to be seen in game
        if (getServerType() == ServerType.SHARED && Arena.getArenaByPlayer(p) == null) {
            e.getRecipients().removeIf(pl -> Arena.getArenaByPlayer(pl) != null);
            return;
        }

        // handle chat color. we would need to work on permission inheritance
        if (Permissions.hasPermission(p, Permissions.PERMISSION_CHAT_COLOR, Permissions.PERMISSION_VIP, Permissions.PERMISSION_ALL)) {
            e.setMessage(ChatColor.translateAlternateColorCodes('&', e.getMessage()));
        }

        Language language = getPlayerLanguage(p);

        // handle lobby world for multi arena
        if (getServerType() == ServerType.MULTIARENA && p.getWorld().getName().equalsIgnoreCase(BedWars.getLobbyWorld())) {
            setRecipients(e, p.getWorld().getPlayers());
        }

        // handle arena chat
        if (Arena.getArenaByPlayer(p) != null) {
            IArena a = Arena.getArenaByPlayer(p);

            // spectator chat
            if (a.isSpectator(p)) {
                setRecipients(e, a.getSpectators());
                e.setFormat(parsePHolders(language.m(Messages.FORMATTING_CHAT_SPECTATOR), p, null));
                return;
            }

            // arena lobby chat
            if (a.getStatus() == GameState.waiting || a.getStatus() == GameState.starting) {
                setRecipients(e, a.getPlayers());
                e.setFormat(parsePHolders(language.m(Messages.FORMATTING_CHAT_WAITING), p, null));
                return;
            }

            ITeam team = a.getTeam(p);
            String msg = e.getMessage();

            // shout format
            if (isShouting(msg, language)) {
                if (!(p.hasPermission(Permissions.PERMISSION_SHOUT_COMMAND) || p.hasPermission(Permissions.PERMISSION_ALL))) {
                    e.setCancelled(true);
                    p.sendMessage(Language.getMsg(p, Messages.COMMAND_NOT_FOUND_OR_INSUFF_PERMS));
                    return;
                }
                if (ShoutCommand.isShoutCooldown(p)) {
                    e.setCancelled(true);
                    p.sendMessage(language.m(Messages.COMMAND_COOLDOWN)
                            .replace("{seconds}", String.valueOf(Math.round(ShoutCommand.getShoutCooldown(p))))
                    );
                    return;
                }
                ShoutCommand.updateShout(p);
                setRecipients(e, a.getPlayers(), a.getSpectators());
                msg = clearShout(msg, language);
                if (msg.isEmpty()) {
                    e.setCancelled(true);
                    return;
                }
                e.setMessage(msg);
                e.setFormat(parsePHolders(language.m(Messages.FORMATTING_CHAT_SHOUT), p, team));
                return;
            }

            // player team chat
            if (a.getMaxInTeam() == 1) {
                setRecipients(e, a.getPlayers(), a.getSpectators());
            } else {
                setRecipients(e, team.getMembers());
            }
            e.setFormat(parsePHolders(language.m(Messages.FORMATTING_CHAT_TEAM), p, team));
            return;
        }

        // multi arena lobby chat
        e.setFormat(parsePHolders(language.m(Messages.FORMATTING_CHAT_LOBBY), p, null));
    }

    private static String parsePHolders(String content, Player player, @Nullable ITeam team) {
        content = content
                .replace("{vPrefix}", getChatSupport().getPrefix(player))
                .replace("{vSuffix}", getChatSupport().getSuffix(player))
                .replace("{playername}", player.getName())
                .replace("{level}", getLevelSupport().getLevel(player))
                .replace("{player}", player.getDisplayName());
        if (team != null) {
            String teamFormat = getMsg(player, Messages.FORMAT_PAPI_PLAYER_TEAM_TEAM)
                    .replace("{TeamColor}", team.getColor().chat() + "")
                    .replace("{TeamName}", team.getDisplayName(Language.getPlayerLanguage(player)).toUpperCase());
            content = content.replace("{team}", teamFormat);
        }
        return SupportPAPI.getSupportPAPI().replace(player, content).replace("{message}", "%2$s");
    }

    private static boolean isShouting(String msg, Language lang) {
        return msg.startsWith("!") || msg.startsWith("shout") ||
                msg.startsWith("SHOUT") || msg.startsWith(lang.m(Messages.MEANING_SHOUT));
    }

    private static String clearShout(String msg, Language lang) {
        if (msg.startsWith("!")) msg = msg.replaceFirst("!", "");
        if (msg.startsWith("SHOUT")) msg = msg.replaceFirst("SHOUT", "");
        if (msg.startsWith("shout")) msg = msg.replaceFirst("shout", "");
        if (msg.startsWith(lang.m(Messages.MEANING_SHOUT))) {
            msg = msg.replaceFirst(lang.m(Messages.MEANING_SHOUT), "");
        }
        return msg.trim();
    }

    @SafeVarargs
    public static void setRecipients(AsyncPlayerChatEvent event, List<Player>... target) {
        if (!config.getBoolean(ConfigPath.GENERAL_CHAT_GLOBAL)) {
            event.getRecipients().clear();
            for (List<Player> list : target) {
                event.getRecipients().addAll(list);
            }
        }
    }
}
