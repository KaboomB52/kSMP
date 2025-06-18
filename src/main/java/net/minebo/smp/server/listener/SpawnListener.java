package net.minebo.smp.server.listener;

import net.minebo.smp.kSMP;
import net.minebo.smp.server.ServerHandler;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class SpawnListener implements Listener {

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        if (event.getFrom().getBlockX() == event.getTo().getBlockX() && event.getFrom().getBlockY() == event.getTo().getBlockY() && event.getFrom().getBlockZ() == event.getTo().getBlockZ()) {
            return;
        }

        if (ServerHandler.getSpawnTasks().containsKey(event.getPlayer().getName())) {
            kSMP.instance.getServer().getScheduler().cancelTask(ServerHandler.getSpawnTasks().get(event.getPlayer().getName()).getTaskId());
            ServerHandler.getSpawnTasks().remove(event.getPlayer().getName());
            player.sendMessage(ChatColor.YELLOW + "Your teleport to spawn has cancelled since you moved.");
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();

            if (ServerHandler.getSpawnTasks().containsKey(player.getName())) {
                kSMP.instance.getServer().getScheduler().cancelTask(ServerHandler.getSpawnTasks().get(player.getName()).getTaskId());
                ServerHandler.getSpawnTasks().remove(player.getName());
                player.sendMessage(ChatColor.YELLOW + "Your teleport to spawn has cancelled since you took damage.");
            }
        }
    }
}
