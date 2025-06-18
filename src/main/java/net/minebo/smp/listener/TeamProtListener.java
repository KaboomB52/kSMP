package net.minebo.smp.listener;

import net.minebo.smp.team.TeamManager;
import net.minebo.smp.team.construct.Team;
import org.bukkit.ChatColor;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class TeamProtListener implements Listener {

    @EventHandler
    public void onAttack(EntityDamageByEntityEvent e) {
        if(e.getDamager().getType() == EntityType.PLAYER) {
            Player attacker = (Player) e.getDamager();

            if(e.getEntity().getType() == EntityType.PLAYER) {
                Player target = (Player) e.getEntity();

                if(TeamManager.getTeamByPlayer(target) != null && TeamManager.getTeamByPlayer(attacker) != null) {
                    Team targetTeam = TeamManager.getTeamByPlayer(target);
                    Team attackerTeam = TeamManager.getTeamByPlayer(attacker);

                    if(targetTeam == attackerTeam) {
                        e.setCancelled(true);
                        attacker.sendMessage(ChatColor.RED + "You can't attack team members!");
                    }
                }
            }
        }
    }
}
