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

import it.branduzzo.nextbedwars.api.arena.GameState;
import it.branduzzo.nextbedwars.api.events.gameplay.GameStateChangeEvent;
import it.branduzzo.nextbedwars.arena.Arena;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class AutoscaleListener implements Listener {

    @EventHandler
    public void onPlaying(GameStateChangeEvent e) {
        if (e.getNewState() == GameState.playing && Arena.canAutoScale(e.getArena().getArenaName())) {
            if (Arena.getGamesBeforeRestart() > 1){
                new Arena(e.getArena().getArenaName(), null);
            }
        }
    }
}
