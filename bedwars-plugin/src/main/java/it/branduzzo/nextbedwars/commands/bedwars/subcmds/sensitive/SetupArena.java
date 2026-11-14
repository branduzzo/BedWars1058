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

import it.branduzzo.nextbedwars.BedWars;
import it.branduzzo.nextbedwars.api.command.ParentCommand;
import it.branduzzo.nextbedwars.api.command.SubCommand;
import it.branduzzo.nextbedwars.arena.Arena;
import it.branduzzo.nextbedwars.arena.Misc;
import it.branduzzo.nextbedwars.arena.SetupSession;
import it.branduzzo.nextbedwars.commands.bedwars.MainCommand;
import it.branduzzo.nextbedwars.configuration.Permissions;
import net.md_5.bungee.api.chat.ClickEvent;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.List;

import static it.branduzzo.nextbedwars.arena.Arena.getArenaByName;

public class SetupArena extends SubCommand {

    public SetupArena(ParentCommand parent, String name) {
        super(parent, name);
        setPriority(2);
        setDisplayInfo(Misc.msgHoverClick("§6 ▪ §7/" + it.branduzzo.nextbedwars.commands.bedwars.MainCommand.getInstance().getName() + " setupArena §6<worldName>", "§fCreate or edit an arena.\n'_' and '-' will not be displayed in the arena's name.",
                "/" + it.branduzzo.nextbedwars.commands.bedwars.MainCommand.getInstance().getName() + " setupArena ", ClickEvent.Action.SUGGEST_COMMAND));
        showInList(true);
        setPermission(Permissions.PERMISSION_SETUP_ARENA);
    }

    @Override
    public boolean execute(String[] args, CommandSender s) {
        if (s instanceof ConsoleCommandSender) return false;
        Player p = (Player) s;
        if (!MainCommand.isLobbySet(p)) return true;
        if (args.length != 1) {
            p.sendMessage("§c▪ §7Usage: §o/" + getParent().getName() + " " + getSubCommandName() + " <mapName>");
            return true;
        }
        if (!args[0].equals(args[0].toLowerCase())) {
            p.sendMessage("§c▪ §c" + args[0] + ChatColor.GRAY + " mustn't contain capital letters! Rename your folder to: " + ChatColor.GREEN + args[0].toLowerCase());
            return true;
        }
        if (args[0].contains("+")) {
            p.sendMessage("§c▪ §7" + args[0] + " mustn't contain this symbol: " + ChatColor.RED + "+");
            return true;
        }
        //if (!BedWars.getAPI().getRestoreAdapter().isWorld(args[0])) {
        //    p.sendMessage("§c▪ §7" + args[0] + " doesn't exist!");
        //    return true;
        //}
        if (getArenaByName(args[0]) != null && !BedWars.autoscale) {
            p.sendMessage("§c▪ §7Please disable it first!");
            return true;
        }
        if (SetupSession.isInSetupSession(p.getUniqueId())) {
            p.sendMessage("§c ▪ §7You're already in a setup session!");
            return true;
        }
        new SetupSession(p, args[0]);
        return true;
    }

    @Override
    public List<String> getTabComplete() {
        return BedWars.getAPI().getRestoreAdapter().getWorldsList();
    }

    @Override
    public boolean canSee(CommandSender s, it.branduzzo.nextbedwars.api.BedWars api) {
        if (s instanceof ConsoleCommandSender) return false;

        Player p = (Player) s;
        if (Arena.isInArena(p)) return false;

        if (SetupSession.isInSetupSession(p.getUniqueId())) return false;
        return hasPermission(s);
    }
}
