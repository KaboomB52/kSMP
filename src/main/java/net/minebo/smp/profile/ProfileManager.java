package net.minebo.smp.profile;

import com.mongodb.client.FindIterable;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import net.minebo.cobalt.gson.Gson;
import net.minebo.smp.kSMP;
import net.minebo.smp.mongo.MongoManager;
import net.minebo.smp.profile.construct.Profile;
import net.minebo.smp.profile.listener.ProfileListener;
import net.minebo.smp.util.Logger;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.*;

import static com.mongodb.client.model.Filters.eq;

public class ProfileManager {

    public static List<Profile> profiles;

    public static File profilesFolder = new File(kSMP.instance.getDataFolder(), "internal/profiles");

    public static void init() {
        profiles = new ArrayList<>();

        Logger.log("Loading teams...");

        loadProfilesFromMongo(); // Big Function

        Logger.log("Loaded " + profiles.size() + " profiles from mongo.");

        Bukkit.getPluginManager().registerEvents(new ProfileListener(), kSMP.instance);
    }

    public static List<Profile> getRegisteredProfiles() {
        return profiles;
    }

    public static void registerProfile(Profile profile) {
        Logger.log("Registered profile for: \"" + profile.uuid + "\"");
        profiles.add(profile);
    }

    public static void unRegisterProfile(Profile profile){
        Logger.log("Unregistered profile for: \"" + profile.uuid + "\"");
        profiles.remove(profile);
        deleteProfileFromMongo(profile);
    }

    public static Profile getProfileByUUID(UUID uuid) {
        return profiles.stream()
                .filter(profile -> profile.uuid.equals(uuid))
                .findFirst()
                .orElse(null);
    }

    public static Profile getProfileByPlayer(Player player){
        return getProfileByUUID(player.getUniqueId());
    }

    public static void deleteProfileFromMongo(Profile profile) {
        MongoManager.teamCollection.deleteOne(Filters.eq("uuid", profile.uuid.toString()));
    }

    public static void loadProfilesFromMongo() {
        FindIterable<Document> documents = MongoManager.profileCollection.find();

        for (Document doc : documents) {
            try {
                UUID uuid = UUID.fromString(doc.getString("uuid"));
                loadProfile(uuid);
            } catch (Exception e) {
                Bukkit.getLogger().severe("Failed to load a profile document: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public static void saveProfile(Profile profile) {
        Document doc = new Document()
                .append("uuid", profile.uuid.toString())
                .append("gold", profile.gold)
                .append("kills", profile.kills)
                .append("killStreak", profile.killStreak)
                .append("deaths", profile.deaths)
                .append("diamonds", profile.diamonds)
                .append("playtime", profile.playtime)
                .append("dieOnLogin", profile.dieOnLogin);

        // Serialize warps
        Document warpsDoc = new Document();
        for (Map.Entry<String, Location> entry : profile.warps.entrySet()) {
            warpsDoc.append(entry.getKey(), Gson.GSON.toJson(entry.getValue()));
        }
        doc.append("warps", warpsDoc);

        // Upsert profile by UUID
        MongoManager.profileCollection.replaceOne(
                Filters.eq("uuid", profile.uuid.toString()),
                doc,
                new ReplaceOptions().upsert(true)
        );
    }

    public static Profile loadProfile(UUID uuid) {
        Document doc = MongoManager.profileCollection.find(eq("uuid", uuid.toString())).first();

        if (doc == null) {
            Bukkit.getLogger().warning("Profile not found in MongoDB for UUID: " + uuid);
            return null;
        }

        double gold = doc.getDouble("gold");
        int kills = doc.getInteger("kills", 0);
        int killStreak = doc.getInteger("killStreak", 0);
        int deaths = doc.getInteger("deaths", 0);
        int diamonds = doc.getInteger("diamonds", 0);
        Number playtimeNumber = (Number) doc.getOrDefault("playtime", 0);
        long playtime = playtimeNumber.longValue();
        boolean dieOnLogin = doc.getBoolean("dieOnLogin", false);

        // Deserialize warps
        HashMap<String, Location> warps = new HashMap<>();
        Document warpsDoc = (Document) doc.get("warps");
        if (warpsDoc != null) {
            for (String warpName : warpsDoc.keySet()) {
                String json = warpsDoc.getString(warpName);
                Location location = Gson.GSON.fromJson(json, Location.class);
                warps.put(warpName, location);
            }
        }

        Profile profile = new Profile(uuid);
        profile.setBalance(gold);
        profile.kills = kills;
        profile.killStreak = killStreak;
        profile.deaths = deaths;
        profile.diamonds = diamonds;
        profile.playtime = playtime;
        profile.warps = warps;
        profile.dieOnLogin = dieOnLogin;

        // Store in memory (if needed)
        profiles.add(profile); // or ProfileManager.profiles.put(uuid, profile)

        Bukkit.getLogger().info("Loaded profile from MongoDB: " + uuid);
        return profile;
    }

}
