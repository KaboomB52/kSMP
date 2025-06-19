package net.minebo.smp.poll.construct;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.minebo.smp.kSMP;
import net.minebo.smp.poll.PollManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;

public class Poll {

    public PollType type;
    public Player sender;

    public Integer playersFor;
    public Integer playersAgainst;

    public List<Player> votedPlayers;

    public Poll(Player sender, PollType pollType) {
        this.sender = sender;
        this.type = pollType;
        this.playersFor = 0;
        this.playersAgainst = 0;
        this.votedPlayers = new ArrayList<Player>();
    }

    public static Component getPollMessage(Player sender, PollType pollType) {
        return Component.text()
                .append(Component.text(sender.getDisplayName() + " ", NamedTextColor.WHITE))
                .append(Component.text("has started a poll to ", NamedTextColor.YELLOW))
                .append(Component.text(pollType.description + ". ", NamedTextColor.GOLD))
                .append(Component.text("Use ", NamedTextColor.YELLOW))
                .append(Component.text("/vote ", NamedTextColor.GOLD))
                .append(Component.text("or click one of the ", NamedTextColor.YELLOW))
                .append(Component.text("buttons ", NamedTextColor.GOLD))
                .append(Component.text("to voice your opinions. ", NamedTextColor.YELLOW))
                .append(
                        Component.text("[Yes]", NamedTextColor.GREEN, TextDecoration.BOLD)
                                .hoverEvent(HoverEvent.showText(Component.text("Click to vote yes!", NamedTextColor.GREEN)))
                                .clickEvent(ClickEvent.runCommand("/vote yes"))
                )
                .append(Component.space())
                .append(
                        Component.text("[No]", NamedTextColor.RED, TextDecoration.BOLD)
                                .hoverEvent(HoverEvent.showText(Component.text("Click to vote no!", NamedTextColor.RED)))
                                .clickEvent(ClickEvent.runCommand("/vote no"))
                )
                .build();
    }

    public void startPoll() {
        Bukkit.getOnlinePlayers().forEach(p -> p.sendMessage(getPollMessage(sender, type)));

        BukkitTask taskid = new BukkitRunnable() {

            int seconds = 60;

            @Override
            public void run() {

                if(votedPlayers.size() == Bukkit.getOnlinePlayers().size()) {
                    if(playersFor > playersAgainst) {
                        Bukkit.broadcastMessage(ChatColor.GOLD + "All players " + ChatColor.YELLOW + "have voted " + ChatColor.GREEN + "yes " + ChatColor.YELLOW + "to " + ChatColor.GOLD + type.description + ChatColor.YELLOW + ".");
                        type.onApproved();
                    } else {
                        Bukkit.broadcastMessage(ChatColor.GOLD + "All players " + ChatColor.YELLOW + "have voted " + ChatColor.RED + "no " + ChatColor.YELLOW + "to " + ChatColor.GOLD + type.description + ChatColor.YELLOW + ".");
                    }
                    PollManager.activePoll = null;
                    this.cancel();
                    return;
                }

                if(seconds == 0){
                    if(playersFor > playersAgainst) {
                        Bukkit.broadcastMessage(ChatColor.GOLD.toString() + playersFor + " players " + ChatColor.YELLOW + "have voted " + ChatColor.GREEN + "yes " + ChatColor.YELLOW + "to " + ChatColor.GOLD + type.description + ChatColor.YELLOW + ".");
                        type.onApproved();
                    } else {
                        Bukkit.broadcastMessage(ChatColor.GOLD.toString() + playersAgainst + " players " + ChatColor.YELLOW + "have voted " + ChatColor.RED + "no " + ChatColor.YELLOW + "to " + ChatColor.GOLD + type.description + ChatColor.YELLOW + ".");
                    }
                    PollManager.activePoll = null;
                    this.cancel();
                    return;
                }

                if(seconds % 15 == 0) Bukkit.broadcastMessage(ChatColor.YELLOW + "There are " + ChatColor.GOLD + seconds + ChatColor.YELLOW + " seconds left to vote!");
                if(seconds <= 10 && seconds != 0) Bukkit.broadcastMessage(ChatColor.YELLOW + "There are " + ChatColor.GOLD + seconds + ChatColor.YELLOW + " seconds left to vote!");

                seconds--;

            }
        }.runTaskTimer(kSMP.instance, 20L, 20L);
    }

    public void addVote(Player player, Boolean vote) {

        if(votedPlayers.contains(player)){
            player.sendMessage(ChatColor.RED + "You have already voted on this poll.");
            return;
        }

        if(vote) {
            player.sendMessage(ChatColor.YELLOW + "You have voted " + ChatColor.GREEN + "yes" + ChatColor.YELLOW + " to " + ChatColor.GOLD + type.description + ChatColor.YELLOW + ".");
            Bukkit.getOnlinePlayers().stream().filter(p -> p != player).forEach(p -> p.sendMessage(player.getDisplayName() + ChatColor.YELLOW + " has voted " + ChatColor.GREEN + "yes" + ChatColor.YELLOW + " to " + ChatColor.GOLD + type.description + ChatColor.YELLOW + "."));
            playersFor++;
        } else {
            player.sendMessage(ChatColor.YELLOW + "You have voted " + ChatColor.RED + "no" + ChatColor.YELLOW + " to " + ChatColor.GOLD + type.description + ChatColor.YELLOW + ".");
            Bukkit.getOnlinePlayers().stream().filter(p -> p != player).forEach(p -> p.sendMessage(player.getDisplayName() + ChatColor.YELLOW + " has voted " + ChatColor.RED + "no" + ChatColor.YELLOW + " to " + ChatColor.GOLD + type.description + ChatColor.YELLOW + "."));
            playersAgainst++;
        }

        votedPlayers.add(player);

    }

}
