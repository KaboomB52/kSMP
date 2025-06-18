package net.minebo.smp.thread;

import net.minebo.smp.profile.ProfileManager;
import net.minebo.smp.profile.construct.Profile;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

public class PlaytimeThread extends BukkitRunnable {

    @Override
    public void run() {
        Bukkit.getOnlinePlayers().forEach(player -> {
            Profile profile = ProfileManager.getProfileByPlayer(player);
            if (profile != null) {
                profile.playtime += 1;
            }
        });
    }

}
