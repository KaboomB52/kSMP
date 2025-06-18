package net.minebo.smp.classes.runnables;

import lombok.Getter;
import lombok.Setter;
import net.minebo.smp.kSMP;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;

@Getter @Setter
public class RestoreBardEffectsTask extends BukkitRunnable
{
    private Player player;
    private PotionEffect effect;

    public RestoreBardEffectsTask(Player player, PotionEffect toGive, int paramInt) {
        this.player = player;
        this.effect = toGive;
        runTaskLater(kSMP.instance, paramInt);
    }

    public void run() {
        Bukkit.getScheduler().runTask(kSMP.instance, () -> {
            player.removePotionEffect(effect.getType());
            player.addPotionEffect(effect);
        });
    }

}