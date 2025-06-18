package net.minebo.smp.shop.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Syntax;
import co.aikar.commands.bukkit.contexts.OnlinePlayer;
import net.minebo.smp.shop.ShopManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

@CommandAlias("sell")
public class SellCommand extends BaseCommand {

    @Default
    @Syntax("<price>")
    public void onSell(Player player, double price) {

        // Get the item the player is holding
        ItemStack itemInHand = player.getInventory().getItemInMainHand();

        // Ensure the player is holding something
        if (itemInHand == null || itemInHand.getType() == Material.AIR) {
            player.sendMessage(ChatColor.RED + "You must be holding an item to sell it.");
            return;
        }

        // Calculate the total price based on the amount of the item
        int amount = itemInHand.getAmount();
        double totalPrice = price * amount;

        // Validate the price
        if (totalPrice <= 0) {
            player.sendMessage(ChatColor.RED + "The price must be a positive number.");
            return;
        }

        ShopManager.sellItemInHand(player, price);
    }

}
