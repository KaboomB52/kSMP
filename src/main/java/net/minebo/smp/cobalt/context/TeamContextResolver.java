package net.minebo.smp.cobalt.context;

import co.aikar.commands.BukkitCommandExecutionContext;
import co.aikar.commands.InvalidCommandArgument;
import co.aikar.commands.contexts.ContextResolver;
import net.minebo.smp.team.TeamManager;
import net.minebo.smp.team.construct.Team;

public class TeamContextResolver implements ContextResolver<Team, BukkitCommandExecutionContext> {

    @Override
    public Team getContext(BukkitCommandExecutionContext commandExecutionContext) throws InvalidCommandArgument {
        String name = commandExecutionContext.popFirstArg();
        Team team = TeamManager.getTeamByName(name);
        if (team != null) return team;
        throw new InvalidCommandArgument("No team matching " + name + " could be found.");
    }

}
