package net.minebo.smp.cobalt.completion;

import co.aikar.commands.CommandCompletionContext;
import co.aikar.commands.CommandCompletions;
import co.aikar.commands.InvalidCommandArgument;
import net.minebo.smp.team.TeamManager;
import net.minebo.smp.team.construct.Team;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class TeamCompletionHandler implements CommandCompletions.CommandCompletionHandler {
    @Override
    public Collection<String> getCompletions(CommandCompletionContext context) throws InvalidCommandArgument {
        List<String> completions = new ArrayList<>();

        for(Team team : TeamManager.teams) {
            completions.add(team.name);
        }

        return completions;
    }
}
