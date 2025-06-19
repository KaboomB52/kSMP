package net.minebo.smp.poll.construct;

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

    public void startPoll() {
        Bukkit.broadcastMessage(sender.getDisplayName() + ChatColor.YELLOW + " has started a poll to " + type.description + ".");

        BukkitTask taskid = new BukkitRunnable() {

            int seconds = 60;

            @Override
            public void run() {

                if(votedPlayers.size() == Bukkit.getOnlinePlayers().size()) {
                    if(playersFor > playersAgainst) {
                        Bukkit.broadcastMessage(ChatColor.GOLD + "All players " + ChatColor.YELLOW + "have voted " + ChatColor.GREEN + "yes " + ChatColor.YELLOW + "to " + type.description + ".");
                        type.onApproved();
                    } else {
                        Bukkit.broadcastMessage(ChatColor.GOLD + "All players " + ChatColor.YELLOW + "have voted " + ChatColor.RED + "no " + ChatColor.YELLOW + "to " + type.description + ".");
                    }
                    PollManager.activePoll = null;
                    this.cancel();
                }

                if(seconds == 0){
                    if(playersFor > playersAgainst) {
                        Bukkit.broadcastMessage(ChatColor.GOLD.toString() + playersFor + " players " + ChatColor.YELLOW + "have voted " + ChatColor.GREEN + "yes " + ChatColor.YELLOW + "to " + type.description + ".");
                        type.onApproved();
                    } else {
                        Bukkit.broadcastMessage(ChatColor.GOLD.toString() + playersAgainst + " players " + ChatColor.YELLOW + "have voted " + ChatColor.RED + "no " + ChatColor.YELLOW + "to " + type.description + ".");
                    }
                    PollManager.activePoll = null;
                    this.cancel();
                }

                switch (seconds) {
                    case 45 -> Bukkit.broadcastMessage(ChatColor.YELLOW + "There are " + ChatColor.GOLD + "45" + ChatColor.YELLOW + " seconds left to vote!");
                    case 30 -> Bukkit.broadcastMessage(ChatColor.YELLOW + "There are " + ChatColor.GOLD + "30" + ChatColor.YELLOW + " seconds left to vote!");
                    case 15 -> Bukkit.broadcastMessage(ChatColor.YELLOW + "There are " + ChatColor.GOLD + "15" + ChatColor.YELLOW + " seconds left to vote!");
                    case 10 -> Bukkit.broadcastMessage(ChatColor.YELLOW + "There are " + ChatColor.GOLD + "10" + ChatColor.YELLOW + " seconds left to vote!");
                }

                if(seconds < 10 && seconds != 0) Bukkit.broadcastMessage(ChatColor.YELLOW + "There are " + ChatColor.GOLD + seconds + ChatColor.YELLOW + " seconds left to vote!");

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
            player.sendMessage(ChatColor.YELLOW + "You have voted " + ChatColor.GREEN + "yes" + ChatColor.YELLOW + " to " + type.description + ".");
            Bukkit.getOnlinePlayers().stream().filter(p -> p != player).forEach(p -> p.sendMessage(player.getDisplayName() + ChatColor.WHITE + " has voted " + ChatColor.GREEN + "yes" + ChatColor.WHITE + " to " + type.description + "."));
            playersFor++;
        } else {
            player.sendMessage(ChatColor.YELLOW + "You have voted " + ChatColor.RED + "no" + ChatColor.YELLOW + " to " + type.description + ".");
            Bukkit.getOnlinePlayers().stream().filter(p -> p != player).forEach(p -> p.sendMessage(player.getDisplayName() + ChatColor.WHITE + " has voted " + ChatColor.RED + "no" + ChatColor.WHITE + " to " + type.description + "."));
            playersAgainst++;
        }

        votedPlayers.add(player);

    }

}
