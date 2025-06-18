package net.minebo.smp.classes.objects;

import lombok.Getter;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Getter
public enum MinerUpgrade {

    NONE(0, Collections.emptyList()),
    HANDY(250, Collections.singletonList(new PotionEffect(PotionEffectType.SPEED, -1, 0))),
    APPRENTICE(500, Arrays.asList(new PotionEffect(PotionEffectType.SPEED, -1, 0), new PotionEffect(PotionEffectType.HASTE, -1, 2))),
    MASTER(750, Arrays.asList(new PotionEffect(PotionEffectType.SPEED, -1, 0), new PotionEffect(PotionEffectType.HASTE, -1, 2),
            new PotionEffect(PotionEffectType.SATURATION, -1, 0), new PotionEffect(PotionEffectType.RESISTANCE, -1, 0))),
    EXTREME(1500, Arrays.asList(new PotionEffect(PotionEffectType.SPEED, -1, 1), new PotionEffect(PotionEffectType.HASTE, -1, 3),
            new PotionEffect(PotionEffectType.SATURATION, -1, 2), new PotionEffect(PotionEffectType.RESISTANCE, -1, 1))),
    ULTIMATE(2500, Arrays.asList(new PotionEffect(PotionEffectType.SPEED, -1, 1), new PotionEffect(PotionEffectType.HASTE, -1, 3),
            new PotionEffect(PotionEffectType.SATURATION, -1, 2), new PotionEffect(PotionEffectType.RESISTANCE, -1, 1), new PotionEffect(PotionEffectType.REGENERATION, -1, 0)));


    private final int diamondsNeeded;
    private final List<PotionEffect> potionEffects;

    MinerUpgrade(int diamondsNeeded, List<PotionEffect> potionEffects) {
        this.diamondsNeeded = diamondsNeeded;
        this.potionEffects = potionEffects;
    }

    public static MinerUpgrade getLevelBasedOnDiamonds(int diamonds) {
        MinerUpgrade current = NONE;
        for (MinerUpgrade upgrade : values()) {
            if (upgrade == NONE || upgrade.getDiamondsNeeded() > diamonds) {
                continue;
            }
            current = upgrade;
        }
        return current;
    }
}
