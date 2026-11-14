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

package it.branduzzo.nextbedwars.levels.internal;

import it.branduzzo.nextbedwars.util.Sched;
import it.branduzzo.nextbedwars.BedWars;
import it.branduzzo.nextbedwars.api.events.player.PlayerXpGainEvent;
import it.branduzzo.nextbedwars.api.language.Language;
import it.branduzzo.nextbedwars.api.language.Messages;
import it.branduzzo.nextbedwars.arena.Arena;
import it.branduzzo.nextbedwars.configuration.LevelsConfig;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

public class PerMinuteTask {

    private final int xp = LevelsConfig.levels.getInt("xp-rewards.per-minute");

    private BukkitTask task;

    /**
     * Create a new per minute xp reward.
     */
    public PerMinuteTask(Arena arena) {
        if (xp < 1){
            return;
        }
        task = Sched.runTaskTimer(BedWars.plugin, () -> {
            for (Player p : arena.getPlayers()) {
                PlayerLevel.getLevelByPlayer ( p.getUniqueId () ).addXp ( xp, PlayerXpGainEvent.XpSource.PER_MINUTE );
                p.sendMessage ( Language.getMsg ( p, Messages.XP_REWARD_PER_MINUTE ).replace ( "{xp}", String.valueOf ( xp ) ) );
            }
        }, 60 * 20, 60 * 20);
    }

    /**
     * Cancel task.
     */
    public void cancel() {
        if (task != null) {
            task.cancel();
        }
    }
}
