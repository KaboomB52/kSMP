package net.minebo.smp.shop.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import co.aikar.commands.annotation.Optional;
import net.minebo.smp.shop.ShopManager;
import net.minebo.smp.shop.construct.ShopItem;
import net.minebo.smp.util.BedrockUtil;
import net.minebo.smp.util.ItemUtil;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

@CommandAlias("myshop")
public class MyShopCommand extends BaseCommand {

    private static final int ITEMS_PER_PAGE = 5;

    @Default
    @Syntax("[page]")
    public void onMyShop(Player player, @Optional Integer pageOptional) {
        UUID playerUUID = player.getUniqueId();

        List<ShopItem> myItems = ShopManager.shopItems.stream()
                .filter(item -> item.getOwner().equals(playerUUID))
                .collect(Collectors.toList());

        if (myItems.isEmpty()) {
            player.sendMessage(ChatColor.GRAY + "You have no items listed in the market.");
            return;
        }

        // Group by item name and price
        Map<String, Integer> groupedItems = new LinkedHashMap<>();
        Map<String, Double> priceLookup = new HashMap<>();

        for (ShopItem item : myItems) {
            String key = item.getName().toUpperCase() + "|" + item.getPrice(); // key by name + price
            groupedItems.put(key, groupedItems.getOrDefault(key, 0) + 1);
            priceLookup.put(key, item.getPrice());
        }

        List<String> groupedDisplay = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : groupedItems.entrySet()) {
            String[] parts = entry.getKey().split("\\|");
            String itemName = parts[0];
            double price = priceLookup.get(entry.getKey());
            int quantity = entry.getValue();

            groupedDisplay.add(ChatColor.DARK_GRAY + "* " + ChatColor.YELLOW + quantity + "x " + itemName +
                    ChatColor.GRAY + " | Price: " + (BedrockUtil.isOnBedrock(player) ? "" : ChatColor.GOLD + "⛃") + ChatColor.YELLOW + price);
        }

        int totalPages = (int) Math.ceil(groupedDisplay.size() / (double) ITEMS_PER_PAGE);
        int page = pageOptional != null ? Math.max(1, Math.min(pageOptional, totalPages)) : 1;

        int start = (page - 1) * ITEMS_PER_PAGE;
        int end = Math.min(start + ITEMS_PER_PAGE, groupedDisplay.size());

        player.sendMessage(ChatColor.DARK_GRAY + "" + ChatColor.STRIKETHROUGH + "------------------------");
        player.sendMessage(ChatColor.GOLD + "Your Shop Listings " + ChatColor.GRAY + "(Page " + page + "/" + totalPages + ")");
        player.sendMessage("");

        for (int i = start; i < end; i++) {
            player.sendMessage(groupedDisplay.get(i));
        }

        player.sendMessage("");
        player.sendMessage(ChatColor.GRAY + "Use " + ChatColor.YELLOW + "/myshop <page>" + ChatColor.GRAY + " to view other pages.");
        player.sendMessage(ChatColor.DARK_GRAY + "" + ChatColor.STRIKETHROUGH + "------------------------");
    }

}
