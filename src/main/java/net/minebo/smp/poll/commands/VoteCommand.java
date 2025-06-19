package net.minebo.smp.poll.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Syntax;
import net.minebo.smp.poll.PollManager;
import net.minebo.smp.poll.construct.Poll;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandAlias("vote")
public class VoteCommand extends BaseCommand {

    @Default
    @Syntax("<yes|no>")
    @CommandCompletion("no|yes")
    public void VoteCommand(CommandSender sender, String vote) {
        if (sender instanceof Player) {
            Player player = (Player) sender;

            if(PollManager.activePoll == null) {
                sender.sendMessage(ChatColor.RED + "There isn't an active poll.");
                return;
            }

            switch (vote) {
                case "yes" -> PollManager.activePoll.addVote(player, true);
                case "no" -> PollManager.activePoll.addVote(player, false);
                default -> {
                    player.sendMessage(ChatColor.YELLOW + "Usage: " + ChatColor.GOLD + "/vote" + ChatColor.WHITE + " <yes|no>");
                }
            }
        }
    }
}
