package net.minebo.smp.shop;

import com.mongodb.client.FindIterable;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import net.minebo.cobalt.util.ItemUtil;
import net.minebo.smp.mongo.MongoManager;
import net.minebo.smp.profile.ProfileManager;
import net.minebo.smp.profile.construct.Profile;
import net.minebo.smp.shop.construct.ShopItem;
import net.minebo.smp.util.BedrockUtil;
import net.minebo.smp.util.ItemStackUtil;
import net.minebo.smp.util.Logger;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class ShopManager {

    public static List<ShopItem> shopItems;
    public static Map<String, ShopItem> itemCache;

    public static void init(){
        shopItems = new ArrayList<ShopItem>(); // Update later
        itemCache = new HashMap<String, ShopItem>();

        Logger.log("Loading shop...");

        loadShopItemsFromMongo();

        Logger.log("Loaded " + shopItems.size() + " teams from mongo.");
    }

    public static ShopItem getItemByUUID(UUID uuid){
        return shopItems.stream()
                .filter(item -> item.id.equals(uuid))
                .findFirst()
                .orElse(null);
    }

    public static void addShopItem(ShopItem item) {
        shopItems.add(item);
        itemCache.put(item.name.toLowerCase(), item);
        saveShopItem(item);
    }

    public static void removeShopItem(ShopItem item) {
        shopItems.remove(item);
        itemCache.remove(item.name.toLowerCase());
        deleteShopItem(item);
    }

    public static ShopItem getItemByName(String name) {
        return itemCache.get(name.toLowerCase());
    }

    public static void deleteShopItem(ShopItem item) {
        MongoManager.shopCollection.deleteOne(Filters.eq("id", item.getId().toString()));
    }

    public static void loadShopItemsFromMongo() {
        FindIterable<Document> documents = MongoManager.shopCollection.find();

        for (Document doc : documents) {
            try {
                ShopItem shopItem = loadShopItem(doc);
                if (shopItem != null) {
                    addShopItem(shopItem);
                }
            } catch (Exception e) {
                e.printStackTrace();
                Bukkit.getLogger().severe("Failed to load shop item from MongoDB: " + e.getMessage());
            }
        }
    }

    private static ShopItem loadShopItem(Document doc) {
        String name = doc.getString("name");
        double price = doc.getDouble("price");
        UUID owner = UUID.fromString(doc.getString("owner"));
        UUID id = UUID.fromString(doc.getString("id"));
        String itemBase64 = doc.getString("item");

        ItemStack item = ItemStackUtil.itemStackFromBase64(itemBase64);

        ShopItem shopItem = new ShopItem(name, owner, price, id, item);
        shopItem.name = name;
        shopItem.id = id;

        return shopItem;
    }

    public static void saveShopItem(ShopItem item) {
        if (item.item == null) {
            Bukkit.getLogger().severe("ShopItem " + item.name + " has a null ItemStack.");
            return;
        }

        Document doc = new Document()
                .append("name", item.name)
                .append("price", item.price)
                .append("owner", item.owner.toString())
                .append("id", item.id.toString())
                .append("item", ItemStackUtil.itemStackToBase64(item.item));

        MongoManager.shopCollection.replaceOne(
                Filters.eq("id", item.id.toString()),
                doc,
                new ReplaceOptions().upsert(true)
        );
    }

    public static boolean buyItemByName(Player sender, String name, int amount) {
        List<ShopItem> itemsToBuy = new ArrayList<>();
        int found = 0;

        ShopItem sampleItem = getItemByName(name);
        if (sampleItem == null) {
            sender.sendMessage(ChatColor.RED + "That item was not found in the market.");
            return false;
        }

        double price = sampleItem.getPrice();

        for (ShopItem item : new ArrayList<>(shopItems)) {
            if (item.name.equalsIgnoreCase(name)) {
                itemsToBuy.add(item);
                found++;
                if (found == amount) break;
            }
        }

        if (itemsToBuy.isEmpty()) {
            sender.sendMessage(ChatColor.RED + "That item was not found in the market.");
            return false;
        }

        if (itemsToBuy.size() < amount) {
            sender.sendMessage(ChatColor.RED + "Only " + itemsToBuy.size() + " is available in the market.");
            return false;
        }

        double totalCost = price * amount;

        Profile buyerProfile = ProfileManager.getProfileByPlayer(sender);
        if (buyerProfile == null || buyerProfile.getBalance() < totalCost) {
            sender.sendMessage(ChatColor.RED + "You don't have enough balance. Total cost: " + (BedrockUtil.isOnBedrock(sender) ? "" : "⛃") + totalCost);
            return false;
        }

        buyerProfile.subtractBalance(totalCost);

        Map<UUID, Integer> itemsSoldBySeller = new HashMap<>();
        ItemStack base = null;
        int totalAmount = 0;

        for (ShopItem item : itemsToBuy) {
            // Track how many items each seller sold
            itemsSoldBySeller.put(item.owner, itemsSoldBySeller.getOrDefault(item.owner, 0) + 1);

            if (base == null) {
                base = item.item.clone();
                totalAmount = 1;
            } else if (base.isSimilar(item.item)) {
                totalAmount++;
            } else {
                sender.getInventory().addItem(item.item.clone());
                continue;
            }

            removeShopItem(item);
        }

        if (base != null) {
            base.setAmount(totalAmount);
            sender.getInventory().addItem(base);
        }

        // Pay and notify each seller
        for (Map.Entry<UUID, Integer> entry : itemsSoldBySeller.entrySet()) {
            UUID sellerUUID = entry.getKey();
            int soldAmount = entry.getValue();
            double sellerEarnings = soldAmount * price;

            Profile seller = ProfileManager.getProfileByUUID(sellerUUID);
            if (seller != null) seller.addBalance(sellerEarnings);

            Player sellerPlayer = Bukkit.getPlayer(sellerUUID);
            if (sellerPlayer != null) {
                sellerPlayer.sendMessage(sender.getDisplayName() + ChatColor.GRAY + " bought " +
                        (soldAmount == 1 ? "a " : ChatColor.YELLOW + Integer.toString(soldAmount) + ChatColor.GRAY + "x ") +
                        ChatColor.GOLD + name.toUpperCase() + ChatColor.GRAY + " from you for " +
                        (BedrockUtil.isOnBedrock(sender) ? "" : ChatColor.GOLD + "⛃") + ChatColor.YELLOW + sellerEarnings + ChatColor.GRAY + ".");
            }
        }

        sender.sendMessage(ChatColor.GRAY + "Purchased " + ChatColor.YELLOW + amount + ChatColor.GRAY + "x " +
                ChatColor.GOLD + name.toUpperCase() + ChatColor.GRAY + " for " +
                (BedrockUtil.isOnBedrock(sender) ? "" : ChatColor.GOLD + "⛃") + ChatColor.YELLOW + totalCost + ChatColor.GRAY + ".");
        return true;
    }

    public static boolean sellItemInHand(Player player, double price) {
        ItemStack held = player.getInventory().getItemInMainHand();
        if (held == null || held.getType() == Material.AIR) return false;

        int amount = held.getAmount();
        if (amount <= 0) return false;

        ItemStack single = held.clone();
        single.setAmount(1);
        single.removeEnchantments();

        for (int i = 0; i < amount; i++) {
            ShopItem item = new ShopItem(player.getUniqueId(), price, single.clone());
            addShopItem(item);
        }

        player.getInventory().setItemInMainHand(null);
        player.sendMessage(ChatColor.GRAY + "You sold " + ChatColor.YELLOW + amount + ChatColor.GRAY + "x " + ChatColor.GOLD + ItemUtil.getItemId(held).toUpperCase() + ChatColor.GRAY + " at " + (BedrockUtil.isOnBedrock(player) ? "" : ChatColor.GOLD + "⛃") + ChatColor.YELLOW + price + ChatColor.GRAY + " each.");

        return true;
    }

}
