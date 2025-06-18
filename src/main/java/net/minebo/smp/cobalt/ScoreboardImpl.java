package net.minebo.smp.cobalt;

import net.minebo.cobalt.cooldown.construct.Cooldown;
import net.minebo.cobalt.scoreboard.provider.ScoreboardProvider;
import net.minebo.cobalt.util.ServerUtil;
import net.minebo.cobalt.util.format.TimeFormatting;
import net.minebo.smp.kSMP;
import net.minebo.smp.classes.ClassManager;
import net.minebo.smp.classes.listeners.ClassListener;
import net.minebo.smp.profile.ProfileManager;
import net.minebo.smp.profile.construct.Profile;
import net.minebo.smp.server.ServerHandler;
import net.minebo.smp.server.task.SpawnTask;
import net.minebo.smp.team.TeamManager;
import net.minebo.smp.team.construct.Team;
import net.minebo.smp.util.BedrockUtil;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class ScoreboardImpl extends ScoreboardProvider {
    @Override
    public String getTitle(Player player){
        return kSMP.instance.getConfig().getString("scoreboard.title");
    }

    @Override
    public List<String> getLines(Player player) {

        List<String> lines = new ArrayList<String>();

        Team playerTeam = TeamManager.getTeamByPlayer(player);

        lines.add("");

        if(playerTeam != null) {
            lines.add("&fTeam: " + ChatColor.GREEN + playerTeam.name);
        }

        Profile profile = ProfileManager.getProfileByPlayer(player);

        if(profile != null) {
            lines.add("&fGold: " + (BedrockUtil.isOnBedrock(player) ? "" : ChatColor.GOLD + "⛃") + ChatColor.YELLOW + profile.getFormattedBalance());
        }

        if(ClassManager.activeClass.containsKey(player.getUniqueId())) {
            lines.addAll(generateClassLines(player));
        }

        if(getSpawnTeleportScore(player) != null) {
            lines.add(ChatColor.BLUE + ChatColor.BOLD.toString() + "Spawn: " + ChatColor.WHITE + getSpawnTeleportScore(player));
        }

        if(kSMP.cooldownHandler.getCooldown("Enderpearl") != null) {
            Cooldown pearlCooldown = kSMP.cooldownHandler.getCooldown("Enderpearl");
            if (pearlCooldown.onCooldown(player)) {
                lines.add(ChatColor.DARK_AQUA + ChatColor.BOLD.toString() + "Enderpearl" + ChatColor.DARK_AQUA + ": " + ChatColor.WHITE + pearlCooldown.getRemaining(player));
            }
        }

        if(kSMP.cooldownHandler.getCooldown("Combat Tag") != null) {
            Cooldown pvpTagCooldown = kSMP.cooldownHandler.getCooldown("Combat Tag");
            if (pvpTagCooldown.onCooldown(player)) {
                lines.add(ChatColor.RED + ChatColor.BOLD.toString() + "PvP Tag" + ChatColor.RED + ": " + ChatColor.WHITE + pvpTagCooldown.getRemaining(player));
            }
        }

        if(player.hasMetadata("modmode")){
            lines.add("");
            lines.add(ChatColor.AQUA + "Staff Info:");
            lines.add(ChatColor.GRAY + " * " + ChatColor.RESET + "TPS: " + ServerUtil.getColoredTPS());
            lines.add(ChatColor.GRAY + " * " + ChatColor.RESET + "Vanish: " + (player.hasMetadata("vanish") ? ChatColor.GREEN + "Yes" : ChatColor.RED + "No"));
            lines.add(ChatColor.GRAY + " * " + ChatColor.RESET + "Chat: " + (player.hasMetadata("toggleSC") ? ChatColor.GOLD + "Staff" : ChatColor.YELLOW + "Public"));
        }

        lines.add("");
        lines.add(kSMP.instance.getConfig().getString("scoreboard.url"));

        return lines;
    }

    public String getSpawnTeleportScore(Player player) {
        SpawnTask spawnTask = ServerHandler.getSpawnTasks().get(player.getName());

        if (spawnTask != null) {
            long diffMillis = spawnTask.getSpawnTime() - System.currentTimeMillis();

            if (diffMillis >= 0) {
                return TimeFormatting.getRemaining(diffMillis) + "s"; // Pass ms
            }
        }

        return null;
    }

    public List<String> generateClassLines(Player player) {
        ArrayList lines = new ArrayList<String>();

        Profile profile = ProfileManager.getProfileByPlayer(player);

        switch (ClassManager.activeClass.get(player.getUniqueId())) {

            case BARD -> {
                lines.add("Class Energy: " + ChatColor.AQUA + ClassManager.bardEnergy.get(player.getUniqueId()).getEnergy());

                Cooldown effectCooldown = kSMP.cooldownHandler.getCooldown("Bard Effect");

                if(effectCooldown.onCooldown(player)) {
                    lines.add(ChatColor.GREEN + ChatColor.BOLD.toString() + "Bard Effect" + ChatColor.GREEN + ": " + ChatColor.WHITE + effectCooldown.getRemaining(player));
                }
            }

            case MINER -> {
                lines.add("Diamonds: " + ChatColor.AQUA + profile.diamonds);
            }

            case ARCHER -> {
                lines.add("Class Energy: " + ChatColor.AQUA + ClassManager.archerEnergy.get(player.getUniqueId()).getEnergy());

                Cooldown archerSugarCooldown = kSMP.cooldownHandler.getCooldown("Archer Sugar");
                Cooldown archerFeatherCooldown = kSMP.cooldownHandler.getCooldown("Archer Feather");

                if(archerSugarCooldown.onCooldown(player)) {
                    lines.add(ChatColor.AQUA + ChatColor.BOLD.toString() + "Speed Effect" + ChatColor.AQUA + ": " + ChatColor.WHITE + archerSugarCooldown.getRemaining(player));
                }

                if(archerFeatherCooldown.onCooldown(player)) {
                    lines.add(ChatColor.LIGHT_PURPLE + ChatColor.BOLD.toString() + "Jump Effect" + ChatColor.LIGHT_PURPLE + ": " + ChatColor.WHITE + archerFeatherCooldown.getRemaining(player));
                }
            }

            case ROGUE -> {
                lines.add("Class Energy: " + ChatColor.AQUA + ClassManager.rogueEnergy.get(player.getUniqueId()).getEnergy());

                Cooldown rogueSugarCooldown = kSMP.cooldownHandler.getCooldown("Rogue Sugar");
                Cooldown rogueFeatherCooldown = kSMP.cooldownHandler.getCooldown("Rogue Feather");

                if(ClassListener.backstabCooldown.containsKey(player.getUniqueId())) {
                    long diff = ClassListener.backstabCooldown.get(player.getUniqueId()) - System.currentTimeMillis();
                    if (diff < 0) diff = 0;
                    double seconds = diff / 1000.0;
                    String formatted = String.format("%.1fs", diff / 1000.0);
                    if(seconds != 0.0) lines.add(ChatColor.YELLOW + ChatColor.BOLD.toString() + "Backstab" + ChatColor.YELLOW + ": " + ChatColor.WHITE + formatted);
                }

                if(rogueSugarCooldown.onCooldown(player)) {
                    lines.add(ChatColor.AQUA + ChatColor.BOLD.toString() + "Speed Effect" + ChatColor.AQUA + ": " + ChatColor.WHITE + rogueSugarCooldown.getRemaining(player));
                }

                if(rogueFeatherCooldown.onCooldown(player)) {
                    lines.add(ChatColor.LIGHT_PURPLE + ChatColor.BOLD.toString() + "Jump Effect" + ChatColor.LIGHT_PURPLE + ": " + ChatColor.WHITE + rogueFeatherCooldown.getRemaining(player));
                }
            }

        }

        return lines;
    }

}
