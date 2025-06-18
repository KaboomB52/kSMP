package net.minebo.smp.cobalt.data;

import org.bukkit.inventory.ItemStack;

public class LoggerData {
    public final ItemStack[] contents;
    public final ItemStack[] armor;

    public LoggerData(ItemStack[] contents, ItemStack[] armor) {
        this.contents = contents;
        this.armor = armor;
    }
}
