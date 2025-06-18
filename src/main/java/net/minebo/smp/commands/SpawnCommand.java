package net.minebo.smp.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Default;
import net.minebo.smp.profile.ProfileManager;
import net.minebo.smp.profile.construct.Profile;
import net.minebo.smp.server.ServerHandler;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandAlias("spawn")
public class SpawnCommand extends BaseCommand {

    @Default
    public void onSpawnCommand(CommandSender sender) {
        if(sender instanceof Player) {

            Player player = (Player) sender;

            if(ProfileManager.getProfileByPlayer(player) == null) {
                player.sendMessage(ChatColor.RED + "You do not have a profile, try reconnecting or contact an administrator.");
                return;
            }

            if(player.hasPermission("basic.staff")) {
                player.sendMessage(ChatColor.YELLOW + "You bypassed the spawn timer since you are a staff member.");
                player.teleport(Bukkit.getWorld("world").getSpawnLocation());

                Profile profile = ProfileManager.getProfileByPlayer(player);

                player.setHealth(20);
                player.setFoodLevel(20);
                player.setSaturation(20);

                if(!profile.hasSpawnProtection()) {
                    profile.spawnProtection = true;
                    player.sendMessage(ChatColor.GREEN + "Your spawn protection has been enabled.");
                }

                return;
            }

            if (player.hasMetadata("frozen")) {
                sender.sendMessage(ChatColor.RED + "You can't teleport while you're frozen!");
                return;
            }

            if (ServerHandler.getSpawnTasks().containsKey(sender.getName())) {
                sender.sendMessage(ChatColor.RED + "You are already teleporting to spawn.");
                return; // dont potato and let them spam spawn
            }

            ServerHandler.startSpawnCommandTask(player);
        }
    }
}
