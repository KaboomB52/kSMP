package net.minebo.smp.team.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.annotation.*;
import co.aikar.commands.bukkit.contexts.OnlinePlayer;
import net.minebo.smp.team.TeamManager;
import net.minebo.smp.team.construct.Team;
import net.minebo.smp.team.construct.TeamRole;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@CommandAlias("team|t")
public class TeamCommands extends BaseCommand {

    @Default
    @CatchUnknown
    @HelpCommand
    public void onHelpCommand(CommandSender sender, CommandHelp help) {
        help.showHelp();
    }

    @Subcommand("create")
    @Syntax("<name>")
    public void createTeamCommand(Player sender, String name){
        Team playerTeam = TeamManager.getTeamByPlayer(sender);

        if(playerTeam != null) {
            sender.sendMessage(ChatColor.RED + "You are already in a team.");
            return;
        }

        if(name == null) {
            sender.sendMessage(ChatColor.RED + "You must specify a team name.");
            return;
        }

        if (name.matches(".*[^a-zA-Z0-9].*")) {
            sender.sendMessage(ChatColor.RED + "You cannot create a team with a non-alphanumeric name!");
            return;
        }

        if(name.length() > 14) {
            sender.sendMessage(ChatColor.RED + "Your team name can't be longer than 14 characters.");
            return;
        }

        if(TeamManager.getTeamByName(name) != null) {
            sender.sendMessage(ChatColor.RED + "A team with this name already exists.");
            return;
        }

        TeamManager.registerTeam(new Team(name, sender));
        sender.sendMessage(ChatColor.GREEN + "You have created a team!");

        Bukkit.getOnlinePlayers().forEach(player -> {
            player.sendMessage(ChatColor.YELLOW + "Team " + ChatColor.GOLD + name + ChatColor.YELLOW + " has been created by " + ChatColor.RESET + sender.getDisplayName() + ChatColor.YELLOW + '.');
        });
    }

    @Subcommand("disband")
    public void deleteTeamCommand(Player sender){
        Team playerTeam = TeamManager.getTeamByPlayer(sender);

        if(playerTeam == null){
            sender.sendMessage(ChatColor.RED + "You are not in a team.");
            return;
        }

        if(playerTeam.getRole(sender.getUniqueId()) != TeamRole.LEADER) {
            sender.sendMessage(ChatColor.RED + "You are not the leader of " + playerTeam.name + ".");
            return;
        }

        TeamManager.unRegisterTeam(playerTeam);

        Bukkit.getOnlinePlayers().forEach(player -> {
            player.sendMessage(ChatColor.YELLOW + "Team " + ChatColor.GOLD + playerTeam.name + ChatColor.YELLOW + " has been disbanded by " + ChatColor.RESET + sender.getDisplayName() + ChatColor.YELLOW + '.');
        });

    }

    @Subcommand("disband")
    @Syntax("<team>")
    @CommandCompletion("@teams")
    @CommandPermission("basic.admin")
    public void disbandOtherTeamCommand(Player sender, Team team){
        sender.sendMessage(ChatColor.GREEN + "You have disbanded the team " + ChatColor.WHITE + team.name + ChatColor.GREEN + ".");

        team.sendMessageToMembers(ChatColor.RED + "Your team was disbanded by an admin.");

        TeamManager.unRegisterTeam(team);
    }

    @Subcommand("chat|c")
    @Syntax("<msg>")
    public void onChatCommand(Player sender, String message){
        Team playerTeam = TeamManager.getTeamByPlayer(sender);

        if(playerTeam == null) {
            sender.sendMessage(ChatColor.RED + "You are not in a team.");
            return;
        }

        if(message == null || message.isEmpty()) {
            sender.sendMessage(ChatColor.RED + "You must send a message.");
            return;
        }

        playerTeam.sendMessageToMembers(sender, message);
    }

    @Subcommand("show|i|who|info")
    @Syntax("<team>")
    @CommandCompletion("@teams")
    public void onShowCommand(Player sender, @Optional String query){

        if(query != null && Bukkit.getPlayer(query) != null) {
            Team playerTeam = TeamManager.getTeamByPlayer(Bukkit.getPlayer(query));

            if(playerTeam == null) {
                sender.sendMessage(ChatColor.RED + Bukkit.getPlayer(query).getName() + " is not in a team.");
                return;
            }

            sender.sendMessage(playerTeam.generateInfoMessage(sender));

            return;
        }

        if(query == null) {
            Team playerTeam = TeamManager.getTeamByPlayer(sender);

            if(playerTeam == null) {
                sender.sendMessage(ChatColor.RED + "You are not in a team.");
                return;
            }

            sender.sendMessage(playerTeam.generateInfoMessage(sender));
        } else {
            Team queriedTeam = TeamManager.getTeamByName(query);

            if(queriedTeam == null) {
                sender.sendMessage(ChatColor.RED + query + " is not a team.");
                return;
            }

            sender.sendMessage(queriedTeam.generateInfoMessage(sender));
        }
    }

    @Subcommand("invite")
    @Syntax("<player>")
    @CommandCompletion("@players")
    public void onInviteCommand(Player sender, OfflinePlayer player){
        Team playerTeam = TeamManager.getTeamByUUID(sender.getUniqueId());

        if(playerTeam == null) {
            sender.sendMessage(ChatColor.RED + "You are not in a team.");
            return;
        }

        if(TeamManager.getTeamByUUID(player.getUniqueId()) == playerTeam){
            sender.sendMessage(ChatColor.RED + player.getName() + " is already in your team.");
            return;
        }

        if(playerTeam.invited.contains(player.getUniqueId())) {
            sender.sendMessage(ChatColor.RED + player.getName() + "is already invited to your team.");
            return;
        }

        TeamRole senderRole = playerTeam.getRole(sender.getUniqueId());

        if(senderRole != TeamRole.LEADER && senderRole != TeamRole.CAPTAIN) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to invite players to your team.");
            return;
        }

        sender.sendMessage(ChatColor.YELLOW + "You have invited " + ChatColor.GOLD + player.getName() + ChatColor.YELLOW + " to your team.");

        Player target = Bukkit.getPlayer(player.getUniqueId());

        if(target != null && target.isOnline()) {
            player.getPlayer().sendMessage(ChatColor.GOLD + "You have been invited to " + ChatColor.YELLOW + playerTeam.name + ChatColor.GOLD + ", type " + ChatColor.YELLOW + "/t join " + playerTeam.name + ChatColor.GOLD + " to join!");
        }

        playerTeam.invited.add(player.getUniqueId());
    }

    @Subcommand("uninvite")
    @Syntax("<player>")
    @CommandCompletion("@players")
    public void onUnInviteCommand(Player sender, OfflinePlayer player){
        Team playerTeam = TeamManager.getTeamByUUID(sender.getUniqueId());

        if(playerTeam == null) {
            sender.sendMessage(ChatColor.RED + "You are not in a team.");
            return;
        }

        if(!playerTeam.invited.contains(player.getUniqueId())) {
            sender.sendMessage(ChatColor.RED + player.getName() + " isn't invited to your team.");
            return;
        }

        TeamRole senderRole = playerTeam.getRole(sender.getUniqueId());

        if(senderRole != TeamRole.LEADER && senderRole != TeamRole.CAPTAIN) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to uninvite players to your team.");
            return;
        }

        sender.sendMessage(ChatColor.YELLOW + "You have uninvited " + ChatColor.GOLD + player.getName() + ChatColor.YELLOW + " to your team.");

        playerTeam.invited.remove(player.getUniqueId());
    }

    @Subcommand("join")
    @Syntax("<team>")
    @CommandCompletion("@teams")
    public void onJoinCommand(Player sender, Team team){

        if(!team.invited.contains(sender.getUniqueId())) {
            sender.sendMessage(ChatColor.RED + "You haven't been invited to this team.");
            return;
        }

        team.invited.remove(sender.getUniqueId());
        team.members.put(sender.getUniqueId(), TeamRole.MEMBER);

        team.sendMessageToMembers(ChatColor.RESET + sender.getDisplayName() + ChatColor.YELLOW + " has joined the team.");

    }

    @Subcommand("sethq")
    public void setHQCommand(Player sender){

        Team playerTeam = TeamManager.getTeamByPlayer(sender);

        if(playerTeam == null) {
            sender.sendMessage(ChatColor.RED + "You are not in a team.");
            return;
        }

        TeamRole senderRole = playerTeam.getRole(sender.getUniqueId());

        if(senderRole != TeamRole.LEADER && senderRole != TeamRole.CAPTAIN) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to uninvite players to your team.");
            return;
        }

        Location loc = sender.getLocation();

        playerTeam.headquarters = loc;

        playerTeam.sendMessageToMembers(sender.getDisplayName() + ChatColor.YELLOW + " has set the HQ location to " + ChatColor.GOLD + loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ() + ChatColor.YELLOW + ".");
    }

    @Subcommand("setrally")
    public void setRallyCommand(Player sender){

        Team playerTeam = TeamManager.getTeamByPlayer(sender);

        if(playerTeam == null) {
            sender.sendMessage(ChatColor.RED + "You are not in a team.");
            return;
        }

        Location loc = sender.getLocation();

        playerTeam.rally = loc;

        playerTeam.sendMessageToMembers(sender.getDisplayName() + ChatColor.YELLOW + " has set the Rally location to " + ChatColor.GOLD + loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ() + ChatColor.YELLOW + ".");
    }

    @Subcommand("hq|home")
    public void gotoHQCommand(Player sender){

        Team playerTeam = TeamManager.getTeamByPlayer(sender);

        if(playerTeam == null) {
            sender.sendMessage(ChatColor.RED + "You are not in a team.");
            return;
        }

        sender.teleport(playerTeam.headquarters);
        sender.sendMessage(ChatColor.YELLOW + "You have been sent to " + ChatColor.GOLD + playerTeam.name + ChatColor.YELLOW + "'s headquarters.");
    }

    @Subcommand("rally")
    public void gotoRallyCommand(Player sender){

        Team playerTeam = TeamManager.getTeamByPlayer(sender);

        if(playerTeam == null) {
            sender.sendMessage(ChatColor.RED + "You are not in a team.");
            return;
        }

        sender.teleport(playerTeam.rally);
        sender.sendMessage(ChatColor.YELLOW + "You have been sent to " + ChatColor.GOLD + playerTeam.name + ChatColor.YELLOW + "'s rally location.");
    }

    @Subcommand("promote")
    @Syntax("<player>")
    @CommandCompletion("@players")
    public void onPromoteCommand(Player sender, OfflinePlayer player){
        Team playerTeam = TeamManager.getTeamByUUID(sender.getUniqueId());

        if(playerTeam == null) {
            sender.sendMessage(ChatColor.RED + "You are not in a team.");
            return;
        }

        TeamRole senderRole = playerTeam.getRole(sender.getUniqueId());

        if(senderRole != TeamRole.LEADER) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to promote players on your team.");
            return;
        }

        TeamRole receiverRole = playerTeam.getRole(player.getUniqueId());

        if(!playerTeam.members.containsKey(player.getUniqueId())) {
            sender.sendMessage( ChatColor.RED + player.getName() + " is not a member of your team.");
            return;
        }

        if(receiverRole == TeamRole.CAPTAIN) {
            sender.sendMessage(ChatColor.RED + "You cannot promote players to a role higher than captain.");
            return;
        }

        sender.sendMessage(ChatColor.YELLOW + "You have promoted " + ChatColor.GOLD + player.getName() + ChatColor.YELLOW + " to " + ChatColor.AQUA + "Captain" + ChatColor.YELLOW + ".");

        if(player.isOnline()) player.getPlayer().sendMessage(ChatColor.YELLOW + "You have been promoted to " + ChatColor.AQUA + "Captain" + ChatColor.YELLOW + ".");

        playerTeam.members.put(player.getUniqueId(), TeamRole.CAPTAIN);
    }

    @Subcommand("demote")
    @Syntax("<player>")
    @CommandCompletion("@players")
    public void onDemoteCommand(Player sender, OfflinePlayer player){
        Team playerTeam = TeamManager.getTeamByUUID(sender.getUniqueId());

        if(playerTeam == null) {
            sender.sendMessage(ChatColor.RED + "You are not in a team.");
            return;
        }

        TeamRole senderRole = playerTeam.getRole(sender.getUniqueId());

        if(senderRole != TeamRole.LEADER) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to promote players on your team.");
            return;
        }

        TeamRole receiverRole = playerTeam.getRole(player.getUniqueId());

        if(!playerTeam.members.containsKey(player.getUniqueId())) {
            sender.sendMessage( ChatColor.RED + player.getName() + " is not a member of your team.");
            return;
        }

        sender.sendMessage(ChatColor.YELLOW + "You have demoted " + ChatColor.GOLD + player.getName() + ChatColor.YELLOW + " to " + ChatColor.GREEN + "Member" + ChatColor.YELLOW + ".");

        if(player.isOnline()) player.getPlayer().sendMessage(ChatColor.YELLOW + "You have been demoted to " + ChatColor.GREEN + "Member" + ChatColor.YELLOW + ".");

        playerTeam.members.put(player.getUniqueId(), TeamRole.MEMBER);
    }

    @Subcommand("kick")
    @Syntax("<player>")
    @CommandCompletion("@players")
    public void onKickCommand(Player sender, OfflinePlayer player){
        Team playerTeam = TeamManager.getTeamByUUID(sender.getUniqueId());

        if(playerTeam == null) {
            sender.sendMessage(ChatColor.RED + "You are not in a team.");
            return;
        }

        TeamRole senderRole = playerTeam.getRole(sender.getUniqueId());

        if(senderRole != TeamRole.CAPTAIN && senderRole != TeamRole.LEADER) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to remove players from your team.");
            return;
        }

        TeamRole receiverRole = playerTeam.getRole(player.getUniqueId());

        if(!playerTeam.members.containsKey(player.getUniqueId())) {
            sender.sendMessage( ChatColor.RED + player.getName() + " is not a member of your team.");
            return;
        }

        if(receiverRole == TeamRole.CAPTAIN && senderRole != TeamRole.LEADER) {
            sender.sendMessage(ChatColor.RED + "You cannot kick that player.");
            return;
        }

        if(receiverRole == TeamRole.LEADER) {
            sender.sendMessage(ChatColor.RED + "You cannot kick the leader of the team.");
            return;
        }

        sender.sendMessage(ChatColor.YELLOW + "You have kicked " + ChatColor.GOLD + player.getName() + ChatColor.YELLOW + " from your faction!");

        if(player.isOnline()) player.getPlayer().sendMessage(ChatColor.RED + "You have been kicked from " + playerTeam.name + ".");

        playerTeam.members.remove(player.getUniqueId());
    }

    @Subcommand("leave")
    public void onLeaveCommand(Player sender){
        Team playerTeam = TeamManager.getTeamByUUID(sender.getUniqueId());

        if(playerTeam == null) {
            sender.sendMessage(ChatColor.RED + "You are not in a team.");
            return;
        }

        if(playerTeam.getRole(sender.getUniqueId()) == TeamRole.LEADER) {
            sender.sendMessage(ChatColor.RED + "You cannot leave the team as you are a leader.");
            return;
        }

        playerTeam.sendMessageToMembers(ChatColor.GOLD + sender.getName() + ChatColor.YELLOW + " has left the team.");

        playerTeam.members.remove(sender.getUniqueId());
    }

    @Subcommand("leader")
    @Syntax("<player>")
    @CommandCompletion("@players")
    public void onLeaderCommand(Player sender, OfflinePlayer player){
        Team playerTeam = TeamManager.getTeamByUUID(sender.getUniqueId());

        if(playerTeam == null) {
            sender.sendMessage(ChatColor.RED + "You are not in a team.");
            return;
        }

        if(!playerTeam.members.containsKey(player.getUniqueId())) {
            sender.sendMessage( ChatColor.RED + player.getName() + " is not a member of your team.");
            return;
        }

        if(sender == player.getPlayer()){
            sender.sendMessage(ChatColor.RED + "You cannot promote yourself to leader.");
            return;
        }

        TeamRole senderRole = playerTeam.getRole(sender.getUniqueId());

        if(senderRole != TeamRole.LEADER) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to promote players to leader.");
            return;
        }

        playerTeam.sendMessageToMembers(ChatColor.GOLD + player.getName() + ChatColor.YELLOW + " has been promoted to " + ChatColor.LIGHT_PURPLE + "Leader" + ChatColor.YELLOW + ".");

        if(player.isOnline()) player.getPlayer().sendMessage(ChatColor.YELLOW + "You have been promoted to " + ChatColor.LIGHT_PURPLE + "Leader" + ChatColor.YELLOW + ".");

        playerTeam.members.put(sender.getUniqueId(), TeamRole.MEMBER);
        playerTeam.members.put(player.getUniqueId(), TeamRole.LEADER);
    }

}

