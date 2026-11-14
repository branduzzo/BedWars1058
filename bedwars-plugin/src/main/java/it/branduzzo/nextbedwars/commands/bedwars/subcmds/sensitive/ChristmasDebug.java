package it.branduzzo.nextbedwars.commands.bedwars.subcmds.sensitive;

import it.branduzzo.nextbedwars.api.BedWars;
import it.branduzzo.nextbedwars.api.arena.IArena;
import it.branduzzo.nextbedwars.api.arena.team.ITeam;
import it.branduzzo.nextbedwars.api.command.ParentCommand;
import it.branduzzo.nextbedwars.api.command.SubCommand;
import it.branduzzo.nextbedwars.arena.Arena;
import it.branduzzo.nextbedwars.arena.Misc;
import it.branduzzo.nextbedwars.christmas.ChristmasConfig;
import it.branduzzo.nextbedwars.christmas.ChristmasManager;
import it.branduzzo.nextbedwars.christmas.IceBridge;
import it.branduzzo.nextbedwars.christmas.LootTable;
import it.branduzzo.nextbedwars.christmas.SnowmanGuard;
import it.branduzzo.nextbedwars.configuration.Permissions;
import net.md_5.bungee.api.chat.ClickEvent;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class ChristmasDebug extends SubCommand {

    public ChristmasDebug(ParentCommand parent, String name) {
        super(parent, name);
        setPriority(12);
        showInList(true);
        setPermission(Permissions.PERMISSION_RELOAD);
        setDisplayInfo(Misc.msgHoverClick("§a \u25aa §f/" + getParent().getName() + " debug §7<loot|tree|snowman|bridge|trap|reload|info>",
                "§fDebug delle feature natalizie.", "/" + getParent().getName() + " debug", ClickEvent.Action.RUN_COMMAND));
    }

    @Override
    public boolean execute(String[] args, CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used in game.");
            return true;
        }
        if (args.length == 0) {
            help(player);
            return true;
        }
        IArena arena = Arena.getArenaByPlayer(player);
        ITeam team = arena == null ? null : arena.getTeam(player);
        switch (args[0].toLowerCase()) {
            case "loot":
            case "present":
                if (arena == null) {
                    player.sendMessage(ChatColor.RED + "Devi essere in un'arena.");
                    return true;
                }
                ChristmasManager.spawnDropFor(arena);
                player.sendMessage(ChatColor.GREEN + "Regalo della renna spawnato nell'arena " + arena.getArenaName() + ".");
                break;
            case "loottest":
                LootTable.open(player, arena, player.getLocation());
                player.sendMessage(ChatColor.GREEN + "Loot di prova generato.");
                break;
            case "tree":
                if (arena == null || team == null) {
                    player.sendMessage(ChatColor.RED + "Devi essere in un'arena con un team.");
                    return true;
                }
                ChristmasManager.spawnTree(arena, team);
                player.sendMessage(ChatColor.GREEN + "Albero di Natale spawnato alla tua base.");
                break;
            case "snowman":
                SnowmanGuard.spawn(player);
                player.sendMessage(ChatColor.GREEN + "Pupazzo di neve spawnato.");
                break;
            case "bridge":
                IceBridge.build(player);
                player.sendMessage(ChatColor.GREEN + "Ponte di ghiaccio creato.");
                break;
            case "trap":
                if (team == null) {
                    player.sendMessage(ChatColor.RED + "Devi essere in un'arena con un team.");
                    return true;
                }
                ChristmasManager.armTrap(team);
                player.sendMessage(ChatColor.GREEN + "Trappola natalizia armata per il tuo team.");
                break;
            case "item":
                ItemStack ice = new ItemStack(Material.ICE);
                ChristmasManager.tagItem(ice, "ice-bridge");
                player.getInventory().addItem(ice);
                player.sendMessage(ChatColor.GREEN + "Ice Bridge aggiunto all'inventario.");
                break;
            case "dialog":
                player.sendMessage(ChatColor.GREEN + "protocol=" + player.getProtocolVersion()
                        + " brand=" + player.getClientBrandName()
                        + " canUse=" + it.branduzzo.nextbedwars.dialog.DialogSupport.canUse(player));
                boolean shown;
                try {
                    shown = it.branduzzo.nextbedwars.dialog.DialogSupport.openTest(player);
                } catch (Throwable throwable) {
                    shown = false;
                    player.sendMessage(ChatColor.RED + "Errore dialog: " + throwable);
                }
                player.sendMessage(ChatColor.GREEN + "dialog mostrato: " + shown);
                break;
            case "reload":
                ChristmasManager.reload();
                player.sendMessage(ChatColor.GREEN + "Configurazione Christmas ricaricata.");
                break;
            case "info":
                ChristmasConfig config = ChristmasManager.config();
                player.sendMessage(ChatColor.GREEN + "Christmas: " + (config.enabled() ? "attivo" : "disattivo")
                        + " | drop: " + config.getInt(ChristmasConfig.DROP_INTERVAL) + "s"
                        + " | item shop: " + LootTable.countShopItems()
                        + " | regali attivi: " + ChristmasManager.presents().size()
                        + " | alberi attivi: " + ChristmasManager.trees().size());
                player.sendMessage(ChatColor.GRAY + "Palette: " + config.getString(ChristmasConfig.THEME_PRIMARY)
                        + " / " + config.getString(ChristmasConfig.THEME_SECONDARY)
                        + " / " + config.getString(ChristmasConfig.THEME_ACCENT));
                break;
            default:
                help(player);
                break;
        }
        return true;
    }

    private void help(Player player) {
        player.sendMessage(ChatColor.GREEN + "NextBedWars Christmas debug");
        player.sendMessage(ChatColor.GRAY + "/bw debug loot §7- spawna il regalo della renna");
        player.sendMessage(ChatColor.GRAY + "/bw debug loottest §7- apre subito il loot");
        player.sendMessage(ChatColor.GRAY + "/bw debug tree §7- albero alla tua base");
        player.sendMessage(ChatColor.GRAY + "/bw debug snowman §7- pupazzo di neve");
        player.sendMessage(ChatColor.GRAY + "/bw debug bridge §7- ponte di ghiaccio");
        player.sendMessage(ChatColor.GRAY + "/bw debug trap §7- arma la trappola");
        player.sendMessage(ChatColor.GRAY + "/bw debug item §7- dà l'Ice Bridge");
        player.sendMessage(ChatColor.GRAY + "/bw debug info §7- stato feature");
        player.sendMessage(ChatColor.GRAY + "/bw debug reload §7- ricarica christmas.yml");
    }

    @Override
    public List<String> getTabComplete() {
        return List.of("loot", "loottest", "tree", "snowman", "bridge", "trap", "item", "info", "reload");
    }

    @Override
    public boolean canSee(CommandSender s, BedWars api) {
        return hasPermission(s);
    }
}
