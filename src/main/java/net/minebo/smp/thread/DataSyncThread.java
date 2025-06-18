package net.minebo.smp.thread;

import com.google.common.base.Stopwatch;
import net.minebo.smp.profile.ProfileManager;
import net.minebo.smp.team.TeamManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.concurrent.TimeUnit;

public class DataSyncThread extends BukkitRunnable {

    @Override
    public void run() {

        // Only save online profiles.

        Stopwatch stopwatch = Stopwatch.createUnstarted();

        stopwatch.start();

        Bukkit.getOnlinePlayers().forEach(p -> {ProfileManager.saveProfile(ProfileManager.getProfileByPlayer(p));});
        TeamManager.teams.forEach(TeamManager::saveTeam);

        stopwatch.stop();

        Bukkit.getOnlinePlayers().forEach(player -> {
            if(player.hasPermission("basic.admin")) {
                player.sendMessage(ChatColor.YELLOW + "[DataSync] " + ChatColor.GRAY + "Saved " + Bukkit.getOnlinePlayers().size() + " profiles and " + TeamManager.teams.size() + ChatColor.GRAY + " teams!" + ChatColor.RESET + " (" + stopwatch.elapsed(TimeUnit.MILLISECONDS) + "ms)");
            }
        });
    }

}
