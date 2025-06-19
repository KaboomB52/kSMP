package net.minebo.smp.poll.types;

import net.minebo.smp.poll.construct.PollType;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

public class Day extends PollType {

    public Day() {
        this.name = "Day";
        this.description = "set time to day";
    }

    @Override
    public void onApproved() {
        Bukkit.broadcastMessage(ChatColor.YELLOW + "The time has been changed to " + ChatColor.GOLD + "Day" + ChatColor.YELLOW + "!");
        Bukkit.getWorld("world").setTime(0);
    }
}
