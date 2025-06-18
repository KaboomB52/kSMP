package net.minebo.smp.util;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionType;

public class ItemUtil {

    public static String getItemId(ItemStack item) {
        if(item.getType() == Material.POTION || item.getType() == Material.SPLASH_POTION || item.getType() == Material.LINGERING_POTION){
            return getPotionId(item);
        }

        return item.getType().name().toLowerCase();
    }

    public static String getPotionId(ItemStack item) {
        PotionMeta meta = (PotionMeta) item.getItemMeta();
        if (meta == null) return "unknown";

        PotionType type = meta.getBasePotionType();
        String suffix = item.getType() == Material.SPLASH_POTION ? "s" : "";

        switch (type) {
            case WATER: return "waterp" + suffix;
            case MUNDANE: return "mundanep" + suffix;
            case THICK: return "thickp" + suffix;
            case AWKWARD: return "awkwardp" + suffix;

            case NIGHT_VISION: return "nvp" + suffix;
            case LONG_NIGHT_VISION: return "nvpe" + suffix;

            case INVISIBILITY: return "invp" + suffix;
            case LONG_INVISIBILITY: return "invpe" + suffix;

            case LEAPING: return "ljp1" + suffix;
            case LONG_LEAPING: return "ljp1e" + suffix;
            case STRONG_LEAPING: return "ljp2" + suffix;

            case FIRE_RESISTANCE: return "frp1" + suffix;
            case LONG_FIRE_RESISTANCE: return "frp1e" + suffix;

            case SWIFTNESS: return "swp1" + suffix;
            case LONG_SWIFTNESS: return "swp1e" + suffix;
            case STRONG_SWIFTNESS: return "swp2" + suffix;

            case SLOWNESS: return "slp1" + suffix;
            case LONG_SLOWNESS: return "slp1e" + suffix;
            case STRONG_SLOWNESS: return "slp2" + suffix;

            case WATER_BREATHING: return "wbp1" + suffix;
            case LONG_WATER_BREATHING: return "wbp1e" + suffix;

            case HEALING: return "hp1" + suffix;
            case STRONG_HEALING: return "hp2" + suffix;

            case HARMING: return "dp1" + suffix;
            case STRONG_HARMING: return "dp2" + suffix;

            case POISON: return "pp1" + suffix;
            case LONG_POISON: return "pp1e" + suffix;
            case STRONG_POISON: return "pp2" + suffix;

            case REGENERATION: return "rp1" + suffix;
            case LONG_REGENERATION: return "rp1e" + suffix;
            case STRONG_REGENERATION: return "rp2" + suffix;

            case STRENGTH: return "strp1" + suffix;
            case LONG_STRENGTH: return "strp1e" + suffix;
            case STRONG_STRENGTH: return "strp2" + suffix;

            case WEAKNESS: return "wp1" + suffix;
            case LONG_WEAKNESS: return "wp1e" + suffix;

            case LUCK: return "luckp" + suffix;

            case TURTLE_MASTER: return "tmp1" + suffix;
            case LONG_TURTLE_MASTER: return "tmp1e" + suffix;
            case STRONG_TURTLE_MASTER: return "tmp2" + suffix;

            case SLOW_FALLING: return "sfp1" + suffix;
            case LONG_SLOW_FALLING: return "sfp1e" + suffix;

            case WIND_CHARGED: return "windp" + suffix;
            case WEAVING: return "weavingp" + suffix;
            case OOZING: return "oozingp" + suffix;
            case INFESTED: return "infestedp" + suffix;

            default:
                return type.name().toLowerCase() + suffix;
        }
    }

    public static ItemStack getItemFromId(String id) {
        // Check if it's a known potion ID
        PotionType potionType = getPotionTypeFromId(id);
        if (potionType != null) {
            boolean splash = id.endsWith("s");
            Material mat = splash ? Material.SPLASH_POTION : Material.POTION;

            ItemStack potion = new ItemStack(mat);
            PotionMeta meta = (PotionMeta) potion.getItemMeta();

            if (meta != null) {
                meta.setBasePotionType(potionType);
                potion.setItemMeta(meta);
            }

            return potion;
        }

        // Otherwise treat as material
        try {
            Material mat = Material.matchMaterial(id.toUpperCase());
            if (mat != null) {
                return new ItemStack(mat);
            }
        } catch (IllegalArgumentException ignored) {}

        // Unknown item
        return null;
    }

    public static PotionType getPotionTypeFromId(String id) {
        boolean splash = id.endsWith("s");
        String baseId = splash ? id.substring(0, id.length() - 1) : id;

        switch (baseId) {
            case "wb": return PotionType.WATER;

            case "nvp": return PotionType.NIGHT_VISION;
            case "nvpe": return PotionType.LONG_NIGHT_VISION;

            case "invp": return PotionType.INVISIBILITY;
            case "invpe": return PotionType.LONG_INVISIBILITY;

            case "ljp1": return PotionType.LEAPING;
            case "ljp1e": return PotionType.LONG_LEAPING;
            case "ljp2": return PotionType.STRONG_LEAPING;

            case "frp1": return PotionType.FIRE_RESISTANCE;
            case "frp1e": return PotionType.LONG_FIRE_RESISTANCE;

            case "swp1": return PotionType.SWIFTNESS;
            case "swp1e": return PotionType.LONG_SWIFTNESS;
            case "swp2": return PotionType.STRONG_SWIFTNESS;

            case "slp1": return PotionType.SLOWNESS;
            case "slp1e": return PotionType.LONG_SLOWNESS;
            case "slp2": return PotionType.STRONG_SLOWNESS;

            case "wbp1": return PotionType.WATER_BREATHING;
            case "wbp1e": return PotionType.LONG_WATER_BREATHING;

            case "hp1": return PotionType.HEALING;
            case "hp2": return PotionType.STRONG_HEALING;

            case "dp1": return PotionType.HARMING;
            case "dp2": return PotionType.STRONG_HARMING;

            case "pp1": return PotionType.POISON;
            case "pp1e": return PotionType.LONG_POISON;
            case "pp2": return PotionType.STRONG_POISON;

            case "rp1": return PotionType.REGENERATION;
            case "rp1e": return PotionType.LONG_REGENERATION;
            case "rp2": return PotionType.STRONG_REGENERATION;

            case "strp1": return PotionType.STRENGTH;
            case "strp1e": return PotionType.LONG_STRENGTH;
            case "strp2": return PotionType.STRONG_STRENGTH;

            case "wp1": return PotionType.WEAKNESS;
            case "wp1e": return PotionType.LONG_WEAKNESS;

            case "lp": return PotionType.LUCK;

            case "tmp1": return PotionType.TURTLE_MASTER;
            case "tmp1e": return PotionType.LONG_TURTLE_MASTER;
            case "tmp2": return PotionType.STRONG_TURTLE_MASTER;

            case "sfp1": return PotionType.SLOW_FALLING;
            case "sfp1e": return PotionType.LONG_SLOW_FALLING;

            case "wcp": return PotionType.WIND_CHARGED;
            case "wvp": return PotionType.WEAVING;
            case "ozp": return PotionType.OOZING;
            case "ifp": return PotionType.INFESTED;

            default: return null;
        }
    }

    public static ItemStack getItemFromPotionId(String id) {
        PotionType type = getPotionTypeFromId(id);
        if (type == null) return null;

        boolean splash = id.endsWith("s");

        Material material = splash ? Material.SPLASH_POTION : Material.POTION;
        ItemStack item = new ItemStack(material);
        PotionMeta meta = (PotionMeta) item.getItemMeta();

        if (meta != null) {
            meta.setBasePotionType(type);
            item.setItemMeta(meta);
        }

        return item;
    }

}
