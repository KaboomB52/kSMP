package net.minebo.smp.listener;

import com.google.common.util.concurrent.AbstractScheduledService;
import net.minebo.smp.profile.ProfileManager;
import net.minebo.smp.profile.construct.Profile;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Locale;
import java.util.Objects;
import java.util.UUID;


/*
    This is going to be a Listener that updates stats on Profiles.
 */
public class StatTrackListener implements Listener {

    @EventHandler
    public void onPlayerKill(PlayerDeathEvent e) {
        e.setDeathMessage(null);

        Player slainPlayer = e.getEntity();
        Player killerPlayer = slainPlayer.getKiller();

        if (killerPlayer == null || killerPlayer == slainPlayer) return;

        Profile killer = ProfileManager.getProfileByPlayer(killerPlayer);
        Profile slain = ProfileManager.getProfileByPlayer(slainPlayer);

        if (killer == null || slain == null) return;

        killer.addKill();
        killer.addKillStreak();
        slain.resetKillstreak();
        slain.addDeath();
    }

}
