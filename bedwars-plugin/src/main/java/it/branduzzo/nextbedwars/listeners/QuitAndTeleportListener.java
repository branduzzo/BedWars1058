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

package it.branduzzo.nextbedwars.listeners;

import it.branduzzo.nextbedwars.util.Sched;
import it.branduzzo.nextbedwars.BedWars;
import it.branduzzo.nextbedwars.api.arena.IArena;
import it.branduzzo.nextbedwars.api.configuration.ConfigPath;
import it.branduzzo.nextbedwars.api.language.Language;
import it.branduzzo.nextbedwars.api.server.ServerType;
import it.branduzzo.nextbedwars.arena.Arena;
import it.branduzzo.nextbedwars.arena.LastHit;
import it.branduzzo.nextbedwars.arena.SetupSession;
import it.branduzzo.nextbedwars.arena.team.BedWarsTeam;
import it.branduzzo.nextbedwars.commands.bedwars.subcmds.regular.CmdStats;
import it.branduzzo.nextbedwars.sidebar.SidebarService;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

import static it.branduzzo.nextbedwars.BedWars.*;

public class QuitAndTeleportListener implements Listener {

    @EventHandler
    public void onLeave(@NotNull PlayerQuitEvent e) {
        Player p = e.getPlayer();
        // Remove from arena
        IArena a = Arena.getArenaByPlayer(p);
        if (a != null) {
            if (a.isPlayer(p)) {
                a.removePlayer(p, true);
            } else if (a.isSpectator(p)) {
                a.removeSpectator(p, true);
            }
        }

        //Save preferred language
        if (Language.getLangByPlayer().containsKey(p.getUniqueId())) {
            final UUID u = p.getUniqueId();
            Sched.runTaskAsynchronously(plugin, () -> {
                String iso = Language.getLangByPlayer().get(p.getUniqueId()).getIso();
                if (Language.isLanguageExist(iso)) {
                    if (BedWars.config.getYml().getStringList(ConfigPath.GENERAL_CONFIGURATION_DISABLED_LANGUAGES).contains(iso))
                        iso = Language.getDefaultLanguage().getIso();
                    BedWars.getRemoteDatabase().setLanguage(u, iso);
                }
                Language.getLangByPlayer().remove(p.getUniqueId());
            });
        }

        if (getServerType() != ServerType.SHARED) {
            e.setQuitMessage(null);
        }
        // Manage internal parties
        if (getParty().isInternal()) {
            if (getParty().hasParty(p)) {
                getParty().removeFromParty(p);
            }
        }
        // Check if was doing a setup and remove the session
        SetupSession ss = SetupSession.getSession(p.getUniqueId());
        if (ss != null) {
            ss.cancel();
        }

        SidebarService.getInstance().remove(e.getPlayer());

        BedWarsTeam.reSpawnInvulnerability.remove(e.getPlayer().getUniqueId());

        LastHit lh = LastHit.getLastHit(p);
        if (lh != null) {
            lh.remove();
        }

        CmdStats.getStatsCoolDown().remove(e.getPlayer().getUniqueId());
    }

    /**
     * Handle players teleported outside.
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onWorldChange(@NotNull PlayerChangedWorldEvent e) {

        // if player was teleported outside arena
        IArena arena = Arena.getArenaByPlayer(e.getPlayer());

        if (null == arena) {
            return;
        }

        if (e.getPlayer().getWorld().getName().equals(arena.getWorldName())) {
            return;
        }

        if (arena.isPlayer(e.getPlayer())) {
            // it will teleport you to the lobby world or cached location
            arena.removePlayer(e.getPlayer(), false);
        }

        if (arena.isSpectator(e.getPlayer())) {
            // it will teleport you to the lobby world or cached location
            arena.removeSpectator(e.getPlayer(), false);
        }
    }
}
