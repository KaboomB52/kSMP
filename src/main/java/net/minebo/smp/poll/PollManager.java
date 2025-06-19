package net.minebo.smp.poll;

import net.minebo.smp.poll.construct.Poll;
import net.minebo.smp.poll.construct.PollType;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.reflections.Reflections;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

public class PollManager {

    public static Poll activePoll;

    public static List<PollType> pollTypes = new ArrayList<>();

    public static void init() {
        registerPollTypes();
    }

    public static void startPoll(Player player, PollType pollType) {

        if(PollManager.activePoll != null){
            player.sendMessage(ChatColor.RED + "There is already a poll running.");
            return;
        }

        PollManager.activePoll = new Poll(player, pollType);
        PollManager.activePoll.startPoll();
    }

    public static void registerPollTypes() {
        new Reflections("net.minebo.smp.poll.types").getSubTypesOf(PollType.class).stream().forEach(clazz -> {
            try {
                pollTypes.add(clazz.getDeclaredConstructor().newInstance());
            } catch (InstantiationException | NoSuchMethodException | InvocationTargetException |
                     IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public static PollType getPollTypeByName(String name) {
        for (PollType pollType : pollTypes) {
            if(pollType.name.equalsIgnoreCase(name)) {
                return pollType;
            }
        }
        return null;
    };

}
