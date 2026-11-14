package it.branduzzo.nextbedwars.arena.upgrades;

import it.branduzzo.nextbedwars.api.arena.IArena;
import it.branduzzo.nextbedwars.api.arena.team.ITeam;
import it.branduzzo.nextbedwars.api.events.gameplay.GameEndEvent;
import it.branduzzo.nextbedwars.api.events.player.PlayerLeaveArenaEvent;
import it.branduzzo.nextbedwars.api.events.server.ArenaDisableEvent;
import it.branduzzo.nextbedwars.api.events.team.TeamEliminatedEvent;
import it.branduzzo.nextbedwars.api.events.upgrades.UpgradeBuyEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class HealPoolListner implements Listener {
    @EventHandler
    public void onTeamUpgrade(UpgradeBuyEvent e){
        if (e.getTeamUpgrade().getName().contains("heal-pool")){
            IArena a = e.getArena();
            if (a == null) return;
            ITeam bwt = a.getTeam(e.getPlayer());
            if (bwt == null) return;
            if (!HealPoolTask.exists(a, bwt)){
                new HealPoolTask(bwt);
            }
        }
    }

    @EventHandler
    public void onDisable(ArenaDisableEvent e){
        HealPoolTask.removeForArena(e.getWorldName());
    }

    @EventHandler
    public void onEnd(GameEndEvent e) {
        HealPoolTask.removeForArena(e.getArena());
    }

    @EventHandler
    public void teamDead(TeamEliminatedEvent e)
    {
        HealPoolTask.removeForTeam(e.getTeam());
    }

    @EventHandler
    public void LastLeave(PlayerLeaveArenaEvent event){
        if (event.getArena().getPlayers().isEmpty())
            HealPoolTask.removeForArena(event.getArena());
    }
}
