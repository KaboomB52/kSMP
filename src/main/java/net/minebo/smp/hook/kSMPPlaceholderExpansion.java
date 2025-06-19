package net.minebo.smp.hook;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.clip.placeholderapi.expansion.Relational;
import net.minebo.smp.profile.ProfileManager;
import net.minebo.smp.profile.construct.Profile;
import net.minebo.smp.server.ServerHandler;
import net.minebo.smp.team.TeamManager;
import net.minebo.smp.team.construct.Team;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class kSMPPlaceholderExpansion extends PlaceholderExpansion implements Relational {
    @Override
    public String getIdentifier() {
        return "ksmp";
    }

    @Override
    public String getAuthor() {
        return "Ian Rich";
    }

    @Override
    public String getVersion() {
        return "1.0";
    }

    @Override
    public String onPlaceholderRequest(Player player, String query) {
        if (query.equalsIgnoreCase("nametag")) {
            return getFactionTag(player, player); // use player as both sender and viewer
        }

        if (query.contains("statstop")) {
            String[] parts = query.split("_");
            if (parts.length == 3) {
                try {
                    int place = Integer.parseInt(parts[2]);
                    Pair<String, String> statPair = ServerHandler.getLeaderboardPlacementByStat(parts[1], place);
                    if (statPair != null) {
                        return ChatColor.GREEN + "#" + place + " " + ChatColor.WHITE + statPair.getKey() + " " + ChatColor.YELLOW + statPair.getValue();
                    } else {
                        return ChatColor.RED + "No data";
                    }
                } catch (NumberFormatException e) {
                    return ChatColor.RED + "Invalid number";
                }
            }
        }

        if (query.contains("topname")) {
            String[] parts = query.split("_");
            if (parts.length == 2) {
                Pair<String, String> statPair = ServerHandler.getLeaderboardPlacementByStat(parts[1], 1);
                if (statPair != null) {
                    return statPair.getKey();
                } else {
                    return "Unknown";
                }
            }
        }

        return "";
    }

    @Override
    public String onPlaceholderRequest(Player viewer, Player target, String query) {
        if (query.equalsIgnoreCase("nametag")) {
            return getFactionTag(viewer, target);
        }
        return "";
    }

    public String getFactionTag(Player viewer, Player target) {
        Team viewerFaction = TeamManager.getTeamByPlayer(viewer);
        Team targetFaction = TeamManager.getTeamByPlayer(target);

        if (targetFaction == null || viewerFaction == null) {
            return "&c";
        }

        if (viewer.getUniqueId().equals(target.getUniqueId()) || viewerFaction.name.equalsIgnoreCase(targetFaction.name)) {
            return "&2";
        }

        return "&c";
    }
}
