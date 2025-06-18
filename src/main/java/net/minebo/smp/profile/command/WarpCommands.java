package net.minebo.smp.profile.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.annotation.*;
import net.minebo.smp.profile.ProfileManager;
import net.minebo.smp.profile.construct.Profile;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;

@CommandAlias("go|warp")
public class WarpCommands extends BaseCommand {

    @CatchUnknown
    @HelpCommand
    public void onHelpCommand(CommandSender sender, CommandHelp help) {
        help.showHelp();
    }

    @Default
    @CommandCompletion("@warps")
    @Syntax("<warp>")
    public void onGo(Player player, String name) {
        Profile profile = ProfileManager.getProfileByPlayer(player);
        if (profile == null) return;

        Location warp = profile.getWarp(name);
        if (warp == null) {
            player.sendMessage(ChatColor.RED + "Warp '" + name + "' not found.");
            return;
        }

        player.teleport(warp);
        player.sendMessage(ChatColor.GREEN + "Teleported to warp " + ChatColor.GOLD + name + ChatColor.GREEN + ".");
    }

    @Subcommand("list")
    public void onList(Player player) {
        Profile profile = ProfileManager.getProfileByPlayer(player);
        if (profile == null || profile.warps.isEmpty()) {
            player.sendMessage(ChatColor.RED + "You have no warps set.");
            return;
        }

        player.sendMessage(ChatColor.GREEN + "Your Warps:");
        for (Map.Entry<String, Location> entry : profile.warps.entrySet()) {
            Location loc = entry.getValue();
            player.sendMessage(ChatColor.GRAY + "- " + ChatColor.GOLD + entry.getKey() + ChatColor.DARK_GRAY + " (" + ChatColor.WHITE +
                    loc.getBlockX() + ", " +
                    loc.getBlockY() + ", " +
                    loc.getBlockZ() + ChatColor.DARK_GRAY + ")");
        }
    }

    @Subcommand("set")
    @Syntax("<warp>")
    public void onSet(Player player, String name) {
        Profile profile = ProfileManager.getProfileByPlayer(player);
        if (profile == null) return;

        Location currentLocation = player.getLocation();

        if (profile.getWarp(name) != null) {
            profile.warps.put(name, currentLocation); // overwrite directly
            player.sendMessage(ChatColor.YELLOW + "Warp " + ChatColor.GOLD + name + ChatColor.YELLOW + " has been overwritten.");
        } else {
            if (profile.warps.size() >= getWarpLimit(player)) {
                player.sendMessage(ChatColor.RED + "You have reached your warp limit.");
                return;
            }

            profile.addWarp(name, currentLocation);
            player.sendMessage(ChatColor.GRAY + "Warp " + ChatColor.GOLD + name + ChatColor.GRAY + " set at your current location.");
        }

        ProfileManager.saveProfile(profile);
    }

    @Subcommand("delete")
    @CommandCompletion("@warps")
    @Syntax("<warp>")
    public void onDelete(Player player, String name) {
        Profile profile = ProfileManager.getProfileByPlayer(player);
        if (profile == null) return;

        boolean success = profile.removeWarp(name);
        if (success) {
            player.sendMessage(ChatColor.GRAY + "Warp " + ChatColor.GOLD + name + ChatColor.GRAY + " has been " + ChatColor.RED + "deleted" + ChatColor.GRAY + ".");
            ProfileManager.saveProfile(profile);
        } else {
            player.sendMessage(ChatColor.RED + "No warp with that name was found.");
        }
    }

    private int getWarpLimit(Player player) {
        return 5; // change later
    }

}
