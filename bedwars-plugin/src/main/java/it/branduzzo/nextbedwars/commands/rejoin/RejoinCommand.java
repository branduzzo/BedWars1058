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

package it.branduzzo.nextbedwars.commands.rejoin;

import it.branduzzo.nextbedwars.api.language.Language;
import it.branduzzo.nextbedwars.api.language.Messages;
import it.branduzzo.nextbedwars.arena.ReJoin;
import it.branduzzo.nextbedwars.configuration.Permissions;
import it.branduzzo.nextbedwars.configuration.Sounds;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;

public class RejoinCommand extends BukkitCommand {

    public RejoinCommand(String name) {
        super(name);
    }

    @Override
    public boolean execute(CommandSender s, String st, String[] args) {
        if (s instanceof ConsoleCommandSender) {
            s.sendMessage("This command is for players!");
            return true;
        }

        Player p = (Player) s;

        if (!p.hasPermission(Permissions.PERMISSION_REJOIN)) {
            p.sendMessage(Language.getMsg(p, Messages.COMMAND_NOT_FOUND_OR_INSUFF_PERMS));
            return true;
        }

        ReJoin rj = ReJoin.getPlayer(p);

        if (rj == null) {
            p.sendMessage(Language.getMsg(p, Messages.REJOIN_NO_ARENA));
            Sounds.playSound("rejoin-denied", p);
            return true;
        }

        if (!rj.canReJoin()) {
            p.sendMessage(Language.getMsg(p, Messages.REJOIN_DENIED));
            Sounds.playSound("rejoin-denied", p);
            return true;
        }

        p.sendMessage(Language.getMsg(p, Messages.REJOIN_ALLOWED).replace("{arena}", rj.getArena().getDisplayName()));
        Sounds.playSound("rejoin-allowed", p);
        rj.reJoin(p);
        return true;
    }
}
