package net.minebo.smp;

import net.minebo.cobalt.acf.ACFCommandController;
import net.minebo.cobalt.acf.ACFManager;
import net.minebo.cobalt.cooldown.CooldownHandler;
import net.minebo.cobalt.cooldown.construct.Cooldown;
import net.minebo.cobalt.gson.Gson;
import net.minebo.cobalt.scoreboard.ScoreboardHandler;
import net.minebo.smp.classes.ClassManager;
import net.minebo.smp.classes.listeners.ClassListener;
import net.minebo.smp.cobalt.ScoreboardImpl;
import net.minebo.smp.cobalt.completion.TeamCompletionHandler;
import net.minebo.smp.cobalt.completion.WarpCompletionHandler;
import net.minebo.smp.cobalt.context.TeamContextResolver;
import net.minebo.smp.cobalt.cooldown.CombatTagTimer;
import net.minebo.smp.cobalt.cooldown.EnderPearlCooldown;
import net.minebo.smp.hook.kSMPPlaceholderExpansion;
import net.minebo.smp.listener.*;
import net.minebo.smp.mongo.MongoManager;
import net.minebo.smp.poll.PollManager;
import net.minebo.smp.profile.ProfileManager;
import net.minebo.smp.recipe.RecipeManager;
import net.minebo.smp.server.ServerHandler;
import net.minebo.smp.shop.ShopManager;
import net.minebo.smp.team.TeamManager;
import net.minebo.smp.team.construct.Team;
import net.minebo.smp.thread.DataSyncThread;
import net.minebo.smp.thread.PlaytimeThread;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class kSMP extends JavaPlugin {

    public static kSMP instance;

    public static ACFManager acfManager;
    public static CooldownHandler cooldownHandler;

    @Override
    public void onEnable() {
        instance = this;
        this.saveDefaultConfig();

        if(Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")){
            new kSMPPlaceholderExpansion().register(); // We will find a solution when the time comes... - Ian
        }

        cooldownHandler = new CooldownHandler(this);
        cooldownHandler.registerCooldown("Enderpearl", new EnderPearlCooldown());
        cooldownHandler.registerCooldown("Combat Tag", new CombatTagTimer());

        cooldownHandler.registerCooldown("Archer Tag", new Cooldown());
        cooldownHandler.registerCooldown("Archer Sugar", new Cooldown());
        cooldownHandler.registerCooldown("Archer Feather", new Cooldown());

        cooldownHandler.registerCooldown("Rogue Sugar", new Cooldown());
        cooldownHandler.registerCooldown("Rogue Feather", new Cooldown());

        cooldownHandler.registerCooldown("Bard Effect", new Cooldown());

        acfManager = new ACFManager(this);

        ACFCommandController.registerCompletion("teams", new TeamCompletionHandler());
        ACFCommandController.registerCompletion("warps", new WarpCompletionHandler());
        ACFCommandController.registerContext(Team.class, new TeamContextResolver());
        ACFCommandController.registerAll(this);

        Gson.init();

        registerManagers();

        registerListeners();

        new ScoreboardHandler(new ScoreboardImpl(), this);

        new PlaytimeThread().runTaskTimer(this, 20L, 20L);
        new DataSyncThread().runTaskTimer(this, 20L,  10L * 60L * 20L);

    }

    @Override
    public void onDisable() {
        TeamManager.teams.forEach(TeamManager::saveTeam);
        ProfileManager.profiles.forEach(ProfileManager::saveProfile);
    }

    public void registerListeners(){
        Bukkit.getPluginManager().registerEvents(new GeneralListener(), this);
        Bukkit.getPluginManager().registerEvents(new ChatFormatListener(), this);
        Bukkit.getPluginManager().registerEvents(new StatTrackListener(), this);
        Bukkit.getPluginManager().registerEvents(new TeamProtListener(), this);
        Bukkit.getPluginManager().registerEvents(new ClassListener(), this);
    }

    public void registerManagers(){
        MongoManager.init(getConfig().getString("mongo.uri"), getConfig().getString("mongo.database"));
        TeamManager.init();
        ProfileManager.init();
        ServerHandler.init();
        ShopManager.init();
        RecipeManager.init();
        ClassManager.init();
        PollManager.init();
    }

}
