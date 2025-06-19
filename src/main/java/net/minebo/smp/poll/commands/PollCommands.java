package net.minebo.smp.poll.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Subcommand;
import co.aikar.commands.bukkit.contexts.OnlinePlayer;
import net.minebo.smp.poll.PollManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandAlias("poll")
public class PollCommands extends BaseCommand {

    @Subcommand("start")
    public void startPoll(CommandSender sender) {
        if(sender instanceof Player) {
            PollManager.startPoll((Player) sender, PollManager.getPollTypeByName("Day"));
        }
    }
}
