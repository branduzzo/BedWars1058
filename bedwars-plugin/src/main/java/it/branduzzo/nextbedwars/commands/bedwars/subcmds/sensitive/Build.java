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

package it.branduzzo.nextbedwars.commands.bedwars.subcmds.sensitive;

import it.branduzzo.nextbedwars.api.BedWars;
import it.branduzzo.nextbedwars.api.command.ParentCommand;
import it.branduzzo.nextbedwars.api.command.SubCommand;
import it.branduzzo.nextbedwars.arena.Arena;
import it.branduzzo.nextbedwars.arena.Misc;
import it.branduzzo.nextbedwars.arena.SetupSession;
import it.branduzzo.nextbedwars.commands.bedwars.MainCommand;
import it.branduzzo.nextbedwars.configuration.Permissions;
import net.md_5.bungee.api.chat.ClickEvent;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.List;

import static it.branduzzo.nextbedwars.listeners.BreakPlace.*;

public class Build extends SubCommand {

    public Build(ParentCommand parent, String name) {
        super(parent, name);
        setPriority(9);
        showInList(true);
        setPermission(Permissions.PERMISSION_BUILD);
        setDisplayInfo(Misc.msgHoverClick("§6 ▪ §7/" + getParent().getName() + " "+getSubCommandName()+ "         §8 - §ebuild permission", "§fEnable or disable build session \n§fso you can break or place blocks.",
                "/" + getParent().getName() + " "+getSubCommandName(), ClickEvent.Action.RUN_COMMAND));
    }

    @Override
    public boolean execute(String[] args, CommandSender s) {
        if (s instanceof ConsoleCommandSender) return false;
        Player p = (Player) s;
        if (!MainCommand.isLobbySet(p)) return true;
        if (isBuildSession(p)) {
            p.sendMessage("§6 ▪ §7You can't place and break blocks anymore!");
            removeBuildSession(p);
        } else {
            p.sendMessage("§6 ▪ §7You can place and break blocks now.");
            addBuildSession(p);
        }
        return true;
    }

    @Override
    public List<String> getTabComplete() {
        return null;
    }

    @Override
    public boolean canSee(CommandSender s, BedWars api) {
        if (s instanceof ConsoleCommandSender) return false;

        Player p = (Player) s;
        if (Arena.isInArena(p)) return false;

        if (SetupSession.isInSetupSession(p.getUniqueId())) return false;
        return hasPermission(s);
    }
}
