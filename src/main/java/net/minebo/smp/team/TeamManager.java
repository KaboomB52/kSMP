package net.minebo.smp.team;

import com.mongodb.client.FindIterable;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import net.minebo.cobalt.gson.Gson;
import net.minebo.smp.mongo.MongoManager;
import net.minebo.smp.team.construct.Team;
import net.minebo.smp.team.construct.TeamRole;
import net.minebo.smp.util.Logger;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.time.Instant;
import java.util.*;

public class TeamManager {

    public static List<Team> teams;

    public static void init() {

        // Initiate
        teams = new ArrayList<Team>();

        Logger.log("Loading teams...");

        loadTeamsFromMongo(); // Big Function

        Logger.log("Loaded " + teams.size() + " teams from mongo.");

    }

    public static void registerTeam(Team team){
        //Logger.log("Registered new team: \"" + team.name + "\"");
        teams.add(team);
        saveTeam(team);
    }

    public static void unRegisterTeam(Team team){
        //Logger.log("Unregistered team: \"" + team.name + "\"");
        teams.remove(team);
        deleteTeamFromMongo(team);
    }

    public static Team getTeamByUUID(UUID uuid){
        for(Team team : teams){
            if (team.members.containsKey(uuid)) {
                return team;
            }
        }

        return null;
    }

    public static Team getTeamByName(String query){
        for(Team team : teams){
            if (team.name.equalsIgnoreCase(query)) {
                return team;
            }
        }

        return null;
    }

    public static Team getTeamByPlayer(Player player){
        for(Team team : teams){
            if (team.members.containsKey(player.getUniqueId())) {
                return team;
            }
        }

        return null;
    }

    public static void loadTeamsFromMongo() {
        FindIterable<Document> documents = MongoManager.teamCollection.find();

        for (Document doc : documents) {
            try {
                loadTeam(doc);
            } catch (Exception e) {
                e.printStackTrace();
                Bukkit.getLogger().severe("Failed to load team from MongoDB: " + e.getMessage());
            }
        }
    }

    public static void deleteTeamFromMongo(Team team) {
        MongoManager.teamCollection.deleteOne(Filters.eq("uuid", team.uuid.toString()));
    }

    public static void saveTeam(Team team) {
        Document doc = new Document()
                .append("uuid", team.uuid.toString())
                .append("name", team.name)
                .append("announcement", team.announcement)
                .append("password", team.password)
                .append("creationTime", team.creationTime.toString());

        if (team.headquarters != null) {
            doc.append("headquarters", Gson.GSON.toJson(team.headquarters));
        }

        if (team.rally != null) {
            doc.append("rally", Gson.GSON.toJson(team.rally));
        }

        List<String> invited = team.invited.stream().map(UUID::toString).toList();
        doc.append("invited", invited);

        Document members = new Document();
        for (Map.Entry<UUID, TeamRole> entry : team.members.entrySet()) {
            members.append(entry.getKey().toString(), entry.getValue().name());
        }
        doc.append("members", members);

        MongoManager.teamCollection.replaceOne(
                Filters.eq("uuid", team.uuid.toString()),
                doc,
                new ReplaceOptions().upsert(true)
        );
    }

    public static void loadTeam(Document doc) {
        UUID uuid = UUID.fromString(doc.getString("uuid"));
        String name = doc.getString("name");
        String announcement = doc.getString("announcement");
        String password = doc.getString("password");
        Instant creationTime = Instant.parse(doc.getString("creationTime"));

        Location headquarters = null;
        if (doc.containsKey("headquarters")) {
            headquarters = Gson.GSON.fromJson(doc.getString("headquarters"), Location.class);
        }

        Location rally = null;
        if (doc.containsKey("rally")) {
            rally = Gson.GSON.fromJson(doc.getString("rally"), Location.class);
        }

        List<UUID> invited = new ArrayList<>();
        List<String> invitedList = doc.getList("invited", String.class, new ArrayList<>());
        for (String uuidStr : invitedList) {
            invited.add(UUID.fromString(uuidStr));
        }

        HashMap<UUID, TeamRole> members = new HashMap<>();
        Document membersDoc = doc.get("members", Document.class);
        if (membersDoc != null) {
            for (Map.Entry<String, Object> entry : membersDoc.entrySet()) {
                members.put(UUID.fromString(entry.getKey()), TeamRole.valueOf((String) entry.getValue()));
            }
        }

        Team team = new Team(name, announcement, password, creationTime, uuid, headquarters, rally, invited, members);
        teams.add(team);
        System.out.println("Loaded team: " + uuid);
    }

}
