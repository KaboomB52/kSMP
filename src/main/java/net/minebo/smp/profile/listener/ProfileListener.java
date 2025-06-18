package net.minebo.smp.profile.listener;

import net.minebo.smp.profile.ProfileManager;
import net.minebo.smp.profile.construct.Profile;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class ProfileListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if(ProfileManager.getProfileByPlayer(event.getPlayer()) == null) {
            event.getPlayer().sendMessage(ChatColor.GREEN + "Generated a new profile for you.");
            ProfileManager.registerProfile(new Profile(event.getPlayer().getUniqueId()));
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        if(ProfileManager.getProfileByPlayer(event.getPlayer()) != null) {
            Profile profile = ProfileManager.getProfileByPlayer(event.getPlayer());

            ProfileManager.saveProfile(profile);
        }
    }

}
