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

package it.branduzzo.nextbedwars.commands.bedwars.subcmds.regular;

import it.branduzzo.nextbedwars.BedWars;
import it.branduzzo.nextbedwars.api.arena.GameState;
import it.branduzzo.nextbedwars.api.arena.IArena;
import it.branduzzo.nextbedwars.api.command.ParentCommand;
import it.branduzzo.nextbedwars.api.command.SubCommand;
import it.branduzzo.nextbedwars.api.configuration.ConfigPath;
import it.branduzzo.nextbedwars.api.language.Messages;
import it.branduzzo.nextbedwars.arena.Arena;
import it.branduzzo.nextbedwars.arena.SetupSession;
import it.branduzzo.nextbedwars.commands.bedwars.MainCommand;
import it.branduzzo.nextbedwars.configuration.Permissions;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.List;

import static it.branduzzo.nextbedwars.api.language.Language.getMsg;

public class CmdStart extends SubCommand {

    public CmdStart(ParentCommand parent, String name) {
        super(parent, name);
        setPriority(15);
        showInList(true);
        setDisplayInfo(it.branduzzo.nextbedwars.commands.bedwars.MainCommand.createTC("§6 ▪ §7/"+ MainCommand.getInstance().getName()+" "+getSubCommandName()+" §8 - §eforce start an arena",
                "/"+getParent().getName()+" "+getSubCommandName(), "§fForcestart an arena.\n§fPermission: §c"+Permissions.PERMISSION_FORCESTART));
    }

    @Override
    public boolean execute(String[] args, CommandSender s) {
        if (s instanceof ConsoleCommandSender) return false;
        Player p = (Player) s;
        IArena a = Arena.getArenaByPlayer(p);
        if (a == null){
            p.sendMessage(getMsg(p, Messages.COMMAND_FORCESTART_NOT_IN_GAME));
            return true;
        }
        if (!a.isPlayer(p)){
            p.sendMessage(getMsg(p, Messages.COMMAND_FORCESTART_NOT_IN_GAME));
            return true;
        }
        if (!(p.hasPermission(Permissions.PERMISSION_ALL) || p.hasPermission(Permissions.PERMISSION_FORCESTART))){
            p.sendMessage(getMsg(p, Messages.COMMAND_FORCESTART_NO_PERM));
            return true;
        }
        if (a.getStatus() == GameState.playing) return true;
        if (a.getStatus() == GameState.restarting) return true;
        if (a.getStartingTask() == null){
            if (args.length == 1 && args[0].equalsIgnoreCase("debug") && s.isOp()){
                a.changeStatus(GameState.starting);
                BedWars.debug = true;
            } else {
                return true;
            }
        }
        if (a.getStartingTask().getCountdown() < 5) return true;
        a.getStartingTask().setCountdown(5);
        p.sendMessage(getMsg(p, Messages.COMMAND_FORCESTART_SUCCESS));
        return true;
    }

    @Override
    public List<String> getTabComplete() {
        return null;
    }

    @Override
    public boolean canSee(CommandSender s, it.branduzzo.nextbedwars.api.BedWars api) {
        if (s instanceof ConsoleCommandSender) return false;

        Player p = (Player) s;

        IArena a = Arena.getArenaByPlayer(p);
        if (a != null){
            GameState status = a.getStatus();
            if (status == GameState.waiting || status == GameState.starting){
                if (!a.isPlayer(p)) return false;
            } else {
                return false;
            }
        } else {
            return false;
        }

        if (SetupSession.isInSetupSession(p.getUniqueId())) return false;

        return s.hasPermission(Permissions.PERMISSION_FORCESTART);
    }
}
