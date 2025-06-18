package net.minebo.smp.profile.construct;

import lombok.AllArgsConstructor;
import net.minebo.smp.profile.ProfileManager;
import org.bukkit.Location;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.UUID;

@AllArgsConstructor
public class Profile {

    public UUID uuid;
    public Double gold;

    public Integer kills = 0;
    public Integer killStreak = 0;
    public Integer deaths = 0;

    public Integer diamonds = 0;

    public long playtime = 0; // Total playtime

    public HashMap<String, Location> warps;

    public Boolean dieOnLogin = false;

    public Profile(UUID uuid) {
        this.uuid = uuid;
        this.gold = 0.00;
        this.warps = new HashMap<>();
    }

    public void addDiamonds(Integer diamonds) {
        this.diamonds += diamonds;
    }

    public void addBalance(double amount) {
        if (amount > 0) {
            gold += amount;
        }
    }

    public boolean subtractBalance(double amount) {
        if (amount > 0 && gold >= amount) {
            gold -= amount;
            return true;
        }
        return false;
    }

    public void setBalance(double amount) {
        if (amount >= 0) {
            gold = amount;
        }
    }

    public double getBalance() {
        return gold;
    }

    public double getFormattedBalance() {
        DecimalFormat df = new DecimalFormat("#.00");
        return Double.parseDouble(df.format(gold));
    }

    public void toggleDieOnLogin() {
        dieOnLogin = !dieOnLogin;
        ProfileManager.saveProfile(this);
    }

    public void addKill() {
        kills++;
        ProfileManager.saveProfile(this);
    }

    public void addDeath() {
        deaths++;
        ProfileManager.saveProfile(this);
    }

    public void addKillStreak() {
        killStreak++;
        ProfileManager.saveProfile(this);
    }

    public void resetKillstreak() {
        killStreak = 0;
        ProfileManager.saveProfile(this);
    }

    public Boolean addWarp(String warpName, Location location) {
        // Check if any existing warp name matches case-insensitively
        for (String key : warps.keySet()) {
            if (key.equalsIgnoreCase(warpName)) {
                return false;  // Warp already exists, case-insensitive check
            }
        }
        warps.put(warpName, location);
        return true;
    }

    public Location getWarp(String warpName) {
        // Iterate through the warp keys and return the matching one (case-insensitive)
        for (String key : warps.keySet()) {
            if (key.equalsIgnoreCase(warpName)) {
                return warps.get(key);
            }
        }
        return null;
    }

    public Boolean removeWarp(String warpName) {
        // Check if the warp exists with case-insensitive comparison and remove it
        for (String key : warps.keySet()) {
            if (key.equalsIgnoreCase(warpName)) {
                warps.remove(key);
                return true;  // Removed the matching warp
            }
        }
        return false;  // No matching warp found
    }

    public String getFormattedPlaytime() {
        long seconds = playtime; // NO division by 1000
        long minutes = seconds / 60;
        long hours = minutes / 60;
        minutes %= 60;
        seconds %= 60;
        return (hours != 0 ? hours + "h " : "") + (minutes != 0 ? minutes + "m " : "") + seconds + "s";
    }

}
