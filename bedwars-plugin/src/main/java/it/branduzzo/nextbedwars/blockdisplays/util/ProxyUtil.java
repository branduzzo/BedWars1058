package it.branduzzo.nextbedwars.blockdisplays.util;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import it.branduzzo.nextbedwars.blockdisplays.BlockDisplayCreator;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.command.CommandSender;

public final class ProxyUtil {
    private ProxyUtil() {}

    public static boolean isProxyCommand(String cmd) {
        String lower = cmd.toLowerCase().trim();
        if (lower.startsWith("velocity:") || lower.startsWith("proxy:") || lower.startsWith("bungee:") || lower.startsWith("bungeecord:") || lower.startsWith("waterfall:")) return true;
        if (lower.startsWith("queue ") || lower.equals("queue") || lower.startsWith("queue:")) return true;
        if (lower.startsWith("server ") || lower.equals("server")) return true;
        return false;
    }

    public static String stripPrefix(String cmd) {
        int idx = cmd.indexOf(':');
        if (idx >= 0) return cmd.substring(idx + 1).trim();
        return cmd.trim();
    }

    public static boolean sendProxyCommand(CommandSender sender, String fullCommand) {
        String proxyCmd = stripPrefix(fullCommand);
        if (proxyCmd.startsWith("/")) proxyCmd = proxyCmd.substring(1);
        if (proxyCmd.toLowerCase().startsWith("queue ") && !isProxyCommand(fullCommand)) proxyCmd = fullCommand.trim();
        if (proxyCmd.startsWith("/")) proxyCmd = proxyCmd.substring(1);
        Player player = null;
        if (sender instanceof Player p) player = p;
        else {
            if (!Bukkit.getOnlinePlayers().isEmpty()) player = Bukkit.getOnlinePlayers().iterator().next();
            else return false;
        }
        try {
            Bukkit.getLogger().info("[NextBedWars blocks] Forwarding proxy command '" + proxyCmd + "' for " + player.getName());
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF(player.getUniqueId().toString());
            out.writeUTF(proxyCmd);
            player.sendPluginMessage(BlockDisplayCreator.getHost(), "bblockdisplays:proxy", out.toByteArray());
            ByteArrayDataOutput out2 = ByteStreams.newDataOutput();
            out2.writeUTF(proxyCmd);
            player.sendPluginMessage(BlockDisplayCreator.getHost(), "velocity:main", out2.toByteArray());
            player.sendPluginMessage(BlockDisplayCreator.getHost(), "velocity:player_command", out2.toByteArray());
            ByteArrayDataOutput out3 = ByteStreams.newDataOutput();
            out3.writeUTF("Forward");
            out3.writeUTF("ALL");
            out3.writeUTF("VelocityCommand");
            ByteArrayDataOutput msg = ByteStreams.newDataOutput();
            msg.writeUTF(player.getUniqueId().toString());
            msg.writeUTF(proxyCmd);
            byte[] msgBytes = msg.toByteArray();
            out3.writeShort(msgBytes.length);
            out3.write(msgBytes);
            try { player.sendPluginMessage(BlockDisplayCreator.getHost(), "bungeecord:main", out3.toByteArray()); } catch (Exception ignored) {}
            return true;
        } catch (Exception e) {
            Bukkit.getLogger().warning("[NextBedWars blocks] Proxy forward failed: " + e.getMessage());
            return false;
        }
    }

    public static boolean sendVelocityDirect(Player player, String command) {
        if (command.startsWith("/")) command = command.substring(1);
        try {
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF(command);
            player.sendPluginMessage(BlockDisplayCreator.getHost(), "velocity:player_command", out.toByteArray());
            return true;
        } catch (Exception e) { return false; }
    }
}
