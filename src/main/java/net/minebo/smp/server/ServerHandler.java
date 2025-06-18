package net.minebo.smp.server;

import lombok.Getter;
import net.minebo.smp.kSMP;
import net.minebo.smp.profile.ProfileManager;
import net.minebo.smp.profile.construct.Profile;
import net.minebo.smp.server.listener.SpawnListener;
import net.minebo.smp.server.task.SpawnTask;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class ServerHandler {
    @Getter
    private static Map<String, SpawnTask> spawnTasks;

    public static void init(){
        spawnTasks = new HashMap<>();

        Bukkit.getPluginManager().registerEvents(new SpawnListener(), kSMP.instance);
    }

    public static void startSpawnCommandTask(final Player player) {
        Profile profile = ProfileManager.getProfileByPlayer(player);

        player.sendMessage(ChatColor.YELLOW.toString() + "Teleporting you to spawn in 10 seconds.");

        BukkitTask taskid = new BukkitRunnable() {

            int seconds = 10;

            @Override
            public void run() {
                if (player.hasMetadata("frozen")) {
                    player.sendMessage(ChatColor.YELLOW + "Your teleport to spawn has cancelled since you are frozen.");
                    cancel();
                    return;
                }

                seconds--;

                if (seconds == 0) {
                    if (spawnTasks.containsKey(player.getName())) {
                        spawnTasks.remove(player.getName());
                        player.teleport(Bukkit.getWorld("world").getSpawnLocation());
                        player.sendMessage(ChatColor.YELLOW + "You have been teleported to spawn!");

                        player.setHealth(20);
                        player.setFoodLevel(20);
                        player.setSaturation(20);

                        cancel();
                    }
                }

            }
        }.runTaskTimer(kSMP.instance, 20L, 20L);

        spawnTasks.put(player.getName(), new SpawnTask(taskid.getTaskId(), System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(10)));
    }

    public static Pair<String, String> getLeaderboardPlacementByStat(String stat, int placement) {
        // Defensive copy if you don't want to mutate the original list, otherwise sort in place
        List<Profile> profiles = ProfileManager.getRegisteredProfiles();

        profiles.sort((a, b) -> {
            switch (stat.toLowerCase()) {
                case "kills":
                    return Integer.compare(b.kills, a.kills);
                case "deaths":
                    return Integer.compare(b.deaths, a.deaths);
                case "gold":
                    return Double.compare(b.gold, a.gold);
                case "killstreak":
                    return Integer.compare(b.killStreak, a.killStreak);
                case "playtime":
                    return Long.compare(b.playtime, a.playtime);
                default:
                    return 0;
            }
        });

        if (placement < 1 || placement > profiles.size()) {
            return null;
        }

        Profile p = profiles.get(placement - 1);
        String statValue;
        switch (stat.toLowerCase()) {
            case "kills":
                statValue = String.valueOf(p.kills);
                break;
            case "deaths":
                statValue = String.valueOf(p.deaths);
                break;
            case "gold":
                statValue = String.valueOf(ChatColor.GOLD + "⛃" + ChatColor.YELLOW + p.getFormattedBalance()); // Cast to int for Pair<Integer>
                break;
            case "killstreak":
                statValue = String.valueOf(p.killStreak);
                break;
            case "playtime":
                statValue = String.valueOf(p.getFormattedPlaytime()); // Cast to int for Pair<Integer>
                break;
            default:
                statValue = ChatColor.RED + "None";
        }

        return Pair.of(Bukkit.getOfflinePlayer(p.uuid).getName(), statValue);
    }
}
