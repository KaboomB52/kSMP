package net.minebo.smp.classes.runnables;

import net.minebo.smp.classes.ClassManager;
import net.minebo.smp.classes.ClassType;
import org.bukkit.entity.Player;

public class ClassActivateTask implements Runnable
{
    private Player player;
    private ClassType classType;

    public ClassActivateTask(Player player, ClassType classType) {
        this.player = player;
        this.classType = classType;
    }

    @Override
    public void run() {
        ClassManager.activateClass(player, classType);
    }
}
