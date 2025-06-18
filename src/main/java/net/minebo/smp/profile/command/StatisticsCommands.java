package net.minebo.smp.profile.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import net.minebo.smp.profile.ProfileManager;
import net.minebo.smp.profile.construct.Profile;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.List;


public class StatisticsCommands extends BaseCommand {

    @CommandAlias("stats|statistics")
    @CommandCompletion("@players")
    @Syntax("<player>")
    public void onStatsCommand(Player player, @Optional OfflinePlayer offlinePlayer) {

        if(offlinePlayer != null) {
            if(ProfileManager.getProfileByUUID(offlinePlayer.getUniqueId()) != null) {
                Profile profile = ProfileManager.getProfileByUUID(offlinePlayer.getUniqueId());

                List<String> msg = List.of(
                        "",
                        offlinePlayer.getName() + ChatColor.YELLOW + "'s Statistics",
                        "",
                        ChatColor.YELLOW + "Kills: " + ChatColor.RESET + profile.kills,
                        ChatColor.YELLOW + "Deaths: " + ChatColor.RESET + profile.deaths,
                        ChatColor.YELLOW + "KillStreak: " + ChatColor.RESET + profile.killStreak,
                        "",
                        ChatColor.YELLOW + "Playtime: " + ChatColor.RESET + profile.getFormattedPlaytime(),
                        ""
                );

                msg.forEach(player::sendMessage);
                return;
            } else {
                player.sendMessage(ChatColor.RED + offlinePlayer.getName() + " does not have a profile.");
                return;
            }
        }

        if(ProfileManager.getProfileByPlayer(player) != null) {
            Profile profile = ProfileManager.getProfileByPlayer(player);

            List<String> msg = List.of(
                    "",
                    player.getName() + ChatColor.YELLOW + "'s Statistics",
                    "",
                    ChatColor.YELLOW + "Kills: " + ChatColor.RESET + profile.kills,
                    ChatColor.YELLOW + "Deaths: " + ChatColor.RESET + profile.deaths,
                    ChatColor.YELLOW + "KillStreak: " + ChatColor.RESET + profile.killStreak,
                    "",
                    ChatColor.YELLOW + "Playtime: " + ChatColor.RESET + profile.getFormattedPlaytime(),
                    ""
            );

            msg.forEach(player::sendMessage);
        } else {
            player.sendMessage(ChatColor.RED + "Your profile hasn't loaded correctly, please try relogging or asking an admin if this continues.");
        }
    }

    @CommandAlias("statstop")
    @Syntax("<kills|deaths> [page]")
    @CommandCompletion("kills|deaths|gold|killstreak|playtime @nothing")
    public void onStatsTopCommand(Player player, @Optional String type, @Optional String pageStr) {
        if (type == null || (!type.equalsIgnoreCase("kills") && !type.equalsIgnoreCase("deaths") && !type.equalsIgnoreCase("gold")) && !type.equalsIgnoreCase("killstreak") && !type.equalsIgnoreCase("playtime")) type = "kills";

        int page = 1;
        try {
            if (pageStr != null) page = Integer.parseInt(pageStr);
        } catch (NumberFormatException ignored) {}

        final int entriesPerPage = 10;
        List<Profile> profiles = ProfileManager.getRegisteredProfiles(); // Assumes you have this

        if (profiles == null || profiles.isEmpty()) {
            player.sendMessage(ChatColor.RED + "No stats found.");
            return;
        }

        // Sort profiles
        String finalType = type; // lambda is an ass
        profiles.sort((a, b) -> {
            return switch (finalType.toLowerCase()) {
                case "kills"      -> Integer.compare(b.kills, a.kills);
                case "deaths"     -> Integer.compare(b.deaths, a.deaths);
                case "gold"       -> Double.compare(b.gold, a.gold);
                case "killstreak" -> Integer.compare(b.killStreak, a.killStreak);
                case "playtime"   -> Long.compare(b.playtime, a.playtime);
                default           -> 0;
            };
        });

        int totalPages = (int) Math.ceil((double) profiles.size() / entriesPerPage);
        page = Math.max(1, Math.min(page, totalPages));

        int start = (page - 1) * entriesPerPage;
        int end = Math.min(start + entriesPerPage, profiles.size());

        player.sendMessage(ChatColor.YELLOW + "Top " + finalType + " - Page " + page + "/" + totalPages);

        for (int i = start; i < end; i++) {
            Profile p = profiles.get(i);
            String name = Bukkit.getOfflinePlayer(p.uuid).getName(); // You may need to cache or store player names in the Profile class
            int stat = finalType.equalsIgnoreCase("kills") ? p.kills : finalType.equalsIgnoreCase("killstreak") ? p.killStreak : p.deaths;
            double statGold = -1;
            String statPt = "";

            if(finalType.equalsIgnoreCase("gold")) {
                statGold = p.gold;
            }

            if(finalType.equalsIgnoreCase("playtime")) {
                statPt = p.getFormattedPlaytime();
            }

            if(statPt != "") { player.sendMessage(ChatColor.GREEN + "#" + (i + 1) + " " + ChatColor.RESET + name + ChatColor.GRAY + ": " + ChatColor.YELLOW + statPt); }
            else if(statGold != -1) { player.sendMessage(ChatColor.GREEN + "#" + (i + 1) + " " + ChatColor.RESET + name + ChatColor.GRAY + ": " + ChatColor.YELLOW + statGold); }
            else player.sendMessage(ChatColor.GREEN + "#" + (i + 1) + " " + ChatColor.RESET + name + ChatColor.GRAY + ": " + ChatColor.YELLOW + stat);
        }

        player.sendMessage(ChatColor.YELLOW + "Use /statstop " + finalType + " [page] to view more.");
    }

}
