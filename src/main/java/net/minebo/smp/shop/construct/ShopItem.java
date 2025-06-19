package net.minebo.smp.shop.construct;

import lombok.AllArgsConstructor;
import lombok.Data;
import net.minebo.cobalt.util.ItemUtil;
import net.minebo.smp.shop.ShopManager;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

@AllArgsConstructor
@Data
public class ShopItem {
    public String name;
    public UUID owner;
    public Double price;

    public UUID id;

    public ItemStack item; // Ensure this is always an ItemStack and not an Optional.

    public ShopItem(UUID owner, Double price, ItemStack item) {
        this.name = ItemUtil.getItemId(item);
        this.price = price;
        this.item = item; // Ensure item is an ItemStack, never Optional or null.
        this.owner = owner;
        this.id = getUnusedUUID(); // Ensure unique UUID generation

    }

    // Generates a unique UUID for a new ShopItem
    public UUID getUnusedUUID() {
        UUID testFor = UUID.randomUUID();

        // Check if UUID already exists
        if (ShopManager.getItemByUUID(testFor) != null) {
            return getUnusedUUID();
        }

        return testFor;
    }
}

