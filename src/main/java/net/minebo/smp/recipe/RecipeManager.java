package net.minebo.smp.recipe;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.ItemMeta;

public class RecipeManager {

    public static void init(){
        registerRecipes();
    }

    public static void registerRecipes() {
        // Cactus Soup
        ItemStack cactusSoup = new ItemStack(Material.MUSHROOM_STEW);
        ItemMeta cactusMeta = cactusSoup.getItemMeta();
        cactusSoup.setItemMeta(cactusMeta);

        ShapelessRecipe cactusRecipe = new ShapelessRecipe(new NamespacedKey("mcraidz", "cactus_soup"), cactusSoup);
        cactusRecipe.addIngredient(2, Material.CACTUS);
        cactusRecipe.addIngredient(Material.BOWL);
        Bukkit.addRecipe(cactusRecipe);

        // Cocoa Bean Soup
        ItemStack cocoaSoup = new ItemStack(Material.MUSHROOM_STEW);
        ItemMeta cocoaMeta = cocoaSoup.getItemMeta();
        cocoaSoup.setItemMeta(cocoaMeta);

        ShapelessRecipe cocoaRecipe = new ShapelessRecipe(new NamespacedKey("mcraidz", "cocoa_soup"), cocoaSoup);
        cocoaRecipe.addIngredient(2, Material.COCOA_BEANS);
        cocoaRecipe.addIngredient(Material.BOWL);
        Bukkit.addRecipe(cocoaRecipe);
    }

}
