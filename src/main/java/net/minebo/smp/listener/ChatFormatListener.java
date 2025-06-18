package net.minebo.smp.listener;

import net.minebo.smp.team.TeamManager;
import net.minebo.smp.team.construct.Team;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatFormatListener implements Listener {

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        event.setCancelled(true);

        Player player = event.getPlayer();

        String prefix;
        String tag;

        if(player.hasMetadata("rankPrefix")) prefix = player.getMetadata("rankPrefix").get(0).asString(); else prefix = "";
        if(player.hasMetadata("activeTag")) tag = player.getMetadata("activeTag").get(0).asString(); else tag = "";

        event.getRecipients().forEach(viewer -> {
            String factionTag = getFactionTag(player, viewer);
            String formatted = ChatColor.translateAlternateColorCodes('&',
                    factionTag + tag + prefix + player.getName() + "&7: &f" + event.getMessage());
            viewer.sendMessage(formatted);
        });

    }

    public String getFactionTag(Player sender, Player viewer) {
        Team senderFaction = TeamManager.getTeamByPlayer(sender);
        Team viewerFaction = TeamManager.getTeamByPlayer(viewer);

        if (senderFaction == null) {
            return "&6[&e-&6]";
        }

        if(viewerFaction == null) {
            return "&6[&e" + senderFaction.name + "&6]";
        }

        if (sender.getUniqueId().equals(viewer.getUniqueId()) || senderFaction.name.equalsIgnoreCase(viewerFaction.name)) {
            return "&6[&a" + senderFaction.name + "&6]";
        } else {
            return "&6[&e" + senderFaction.name + "&6]";
        }
    }
}
