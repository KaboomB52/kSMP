package net.minebo.smp.cobalt.completion;

import co.aikar.commands.BukkitCommandIssuer;
import co.aikar.commands.CommandCompletions;
import co.aikar.commands.CommandCompletionContext;
import co.aikar.commands.InvalidCommandArgument;
import net.minebo.smp.profile.ProfileManager;
import net.minebo.smp.profile.construct.Profile;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class WarpCompletionHandler implements CommandCompletions.CommandCompletionHandler {
    @Override
    public Collection<String> getCompletions(CommandCompletionContext context) throws InvalidCommandArgument {
        List<String> completions = new ArrayList<>();

        BukkitCommandIssuer issuer = (BukkitCommandIssuer) context.getIssuer();
        Player player = issuer.getPlayer();

        if (player == null) return completions;

        Profile profile = ProfileManager.getProfileByPlayer(player);
        if (profile == null) return completions;

        completions.addAll(profile.warps.keySet());
        return completions;
    }
}


