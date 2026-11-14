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

package it.branduzzo.nextbedwars.api.events.player;

import it.branduzzo.nextbedwars.api.arena.IArena;
import it.branduzzo.nextbedwars.api.arena.team.ITeam;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlayerInvisibilityPotionEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private Type type;
    private Player player;
    private IArena arena;
    private ITeam team;

    /**
     * This event is called when invisibility potions are managed by Bed-Wars.
     */
    public PlayerInvisibilityPotionEvent(Type type, ITeam team, Player player, IArena arena) {
        this.type = type;
        this.player = player;
        this.arena = arena;
        this.team = team;
    }


    public enum Type {
        ADDED, REMOVED
    }

    public Type getType() {
        return type;
    }

    public IArena getArena() {
        return arena;
    }

    public Player getPlayer() {
        return player;
    }

    public ITeam getTeam() {
        return team;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
