package net.minebo.smp.classes.objects;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;

@Getter @Setter
public class Energy {

    private Player player;
    private int energy;

    public Energy(Player player) {
        this.player = player;
        this.energy = 0;
    }

}
