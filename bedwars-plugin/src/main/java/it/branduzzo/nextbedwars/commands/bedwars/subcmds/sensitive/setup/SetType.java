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

package it.branduzzo.nextbedwars.commands.bedwars.subcmds.sensitive.setup;

import it.branduzzo.nextbedwars.BedWars;
import it.branduzzo.nextbedwars.api.command.ParentCommand;
import it.branduzzo.nextbedwars.api.command.SubCommand;
import it.branduzzo.nextbedwars.api.configuration.ConfigPath;
import it.branduzzo.nextbedwars.api.server.SetupType;
import it.branduzzo.nextbedwars.arena.Misc;
import it.branduzzo.nextbedwars.arena.SetupSession;
import it.branduzzo.nextbedwars.configuration.Permissions;
import net.md_5.bungee.api.chat.ClickEvent;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

import static it.branduzzo.nextbedwars.BedWars.config;

public class SetType extends SubCommand {

    public SetType(ParentCommand parent, String name) {
        super(parent, name);
        setArenaSetupCommand(true);
        setPermission(Permissions.PERMISSION_SETUP_ARENA);
    }

    private static final List<String> available = Arrays.asList("Solo", "Doubles", "3v3v3v3", "4v4v4v4");

    @Override
    public boolean execute(String[] args, CommandSender s) {
        if (s instanceof ConsoleCommandSender) return false;
        Player p = (Player) s;
        SetupSession ss = SetupSession.getSession(p.getUniqueId());
        if (ss == null) {
            s.sendMessage("§c ▪ §7You're not in a setup session!");
            return true;
        }
        if (args.length == 0) {
            sendUsage(p);
        } else {
            if (!available.contains(args[0])) {
                sendUsage(p);
                return true;
            }
            List<String> groups = BedWars.config.getYml().getStringList(ConfigPath.GENERAL_CONFIGURATION_ARENA_GROUPS);
            String input = args[0].substring(0, 1).toUpperCase() + args[0].substring(1).toLowerCase();
            if (!groups.contains(input)) {
                groups.add(input);
                BedWars.config.set(ConfigPath.GENERAL_CONFIGURATION_ARENA_GROUPS, groups);
            }
            if (input.equals("Solo")) {
                ss.getConfig().set("maxInTeam", 1);
            } else if (input.equalsIgnoreCase("Doubles")) {
                ss.getConfig().set("maxInTeam", 2);
            } else if (input.equalsIgnoreCase("3v3v3v3")) {
                ss.getConfig().set("maxInTeam", 3);
            } else if (input.equalsIgnoreCase("4v4v4v4")) {
                ss.getConfig().set("maxInTeam", 4);
            }
            ss.getConfig().set("group", input);
            p.sendMessage("§6 ▪ §7Arena group changed to: §d" + input);
            if (ss.getSetupType() == SetupType.ASSISTED) {
                Bukkit.dispatchCommand(p, getParent().getName());
            }
        }
        return true;
    }

    @Override
    public List<String> getTabComplete() {
        List<String> groups = BedWars.config.getYml().getStringList(ConfigPath.GENERAL_CONFIGURATION_ARENA_GROUPS);
        available.forEach(available -> {
            if (!groups.contains(available)) {
                groups.add(available);
            }
        });
        return config.getYml().getStringList(ConfigPath.GENERAL_CONFIGURATION_ARENA_GROUPS);
    }

    private void sendUsage(Player p) {
        p.sendMessage("§9 ▪ §7Usage: " + getParent().getName() + " " + getSubCommandName() + " <type>");
        p.sendMessage("§9Available types: ");
        for (String st : available) {
            p.spigot().sendMessage(Misc.msgHoverClick("§1 ▪ §e" + st + " §7(click to set)", "§dClick to make the arena " + st, "/" + getParent().getName() + " " + getSubCommandName() + " " + st, ClickEvent.Action.RUN_COMMAND));
        }
    }

    @Override
    public boolean canSee(CommandSender s, it.branduzzo.nextbedwars.api.BedWars api) {
        if (s instanceof ConsoleCommandSender) return false;

        Player p = (Player) s;
        if (!SetupSession.isInSetupSession(p.getUniqueId())) return false;

        return hasPermission(s);
    }
}
