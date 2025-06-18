package net.minebo.smp.classes;

import net.minebo.smp.kSMP;
import net.minebo.smp.classes.objects.Energy;
import net.minebo.smp.classes.objects.MinerUpgrade;
import net.minebo.smp.classes.runnables.RestoreBardEffectsTask;
import net.minebo.smp.profile.ProfileManager;
import net.minebo.smp.team.TeamManager;
import net.minebo.smp.team.construct.Team;
import org.apache.commons.lang3.text.WordUtils;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

public class ClassManager {

    public static final Map<UUID, ClassType> activeClass = new HashMap<>();
    private static final Map<String, RestoreBardEffectsTask> restoreBardHoldEffects = new HashMap<>();
    public static final Map<UUID, Energy> bardEnergy = new HashMap<>();
    public static final Map<UUID, Energy> archerEnergy = new HashMap<>();
    public static final Map<UUID, Energy> rogueEnergy = new HashMap<>();
    public static final Map<UUID, MinerUpgrade> minerUpgrades = new HashMap<>();

    public static final List<String> placed = new ArrayList<>();
    private static final List<Material> bardMaterials = Arrays.asList(Material.SUGAR, Material.BLAZE_POWDER, Material.IRON_INGOT, Material.FEATHER, Material.SPIDER_EYE, Material.MAGMA_CREAM, Material.GHAST_TEAR);

    public static void init() {
        Bukkit.getScheduler().runTaskTimer(kSMP.instance, ClassManager::runCheck, 0L, 2L);
        Bukkit.getScheduler().runTaskTimer(kSMP.instance, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                updateArcherEnergy(player);
                updateBardEnergy(player);
                updateRogueEnergy(player);
            }
        }, 0L, 20L);
    }

    public static void runCheck() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!activeClass.containsKey(player.getUniqueId())) {
                if (player.getInventory().getArmorContents() != null) {
                    if (player.getInventory().getHelmet() != null && player.getInventory().getHelmet().getType() == Material.LEATHER_HELMET && player.getInventory().getChestplate() != null && player.getInventory().getChestplate().getType() == Material.LEATHER_CHESTPLATE && player.getInventory().getLeggings() != null && player.getInventory().getLeggings().getType() == Material.LEATHER_LEGGINGS && player.getInventory().getBoots() != null && player.getInventory().getBoots().getType() == Material.LEATHER_BOOTS) {
                        activateClass(player, ClassType.ARCHER);
                    }
                    else if (player.getInventory().getHelmet() != null && player.getInventory().getHelmet().getType() == Material.IRON_HELMET && player.getInventory().getChestplate() != null && player.getInventory().getChestplate().getType() == Material.IRON_CHESTPLATE && player.getInventory().getLeggings() != null && player.getInventory().getLeggings().getType() == Material.IRON_LEGGINGS && player.getInventory().getBoots() != null && player.getInventory().getBoots().getType() == Material.IRON_BOOTS) {
                        activateClass(player, ClassType.MINER);
                    }
                    else if (player.getInventory().getHelmet() != null && player.getInventory().getHelmet().getType() == Material.GOLDEN_HELMET && player.getInventory().getChestplate() != null && player.getInventory().getChestplate().getType() == Material.GOLDEN_CHESTPLATE && player.getInventory().getLeggings() != null && player.getInventory().getLeggings().getType() == Material.GOLDEN_LEGGINGS && player.getInventory().getBoots() != null && player.getInventory().getBoots().getType() == Material.GOLDEN_BOOTS) {
                        activateClass(player, ClassType.BARD);
                    }
                    else if (player.getInventory().getHelmet() != null && player.getInventory().getHelmet().getType() == Material.CHAINMAIL_HELMET && player.getInventory().getChestplate() != null && player.getInventory().getChestplate().getType() == Material.CHAINMAIL_CHESTPLATE && player.getInventory().getLeggings() != null && player.getInventory().getLeggings().getType() == Material.CHAINMAIL_LEGGINGS && player.getInventory().getBoots() != null && player.getInventory().getBoots().getType() == Material.CHAINMAIL_BOOTS) {
                        activateClass(player, ClassType.ROGUE);
                    }
                }
            }
            else if (!hasKitOn(player, activeClass.get(player.getUniqueId()))) {
                deactiveClass(player, true);
                activeClass.remove(player.getUniqueId());
            }
            else if (hasKitOn(player, activeClass.get(player.getUniqueId()))) {
                ClassType type = activeClass.get(player.getUniqueId());
                if (type == ClassType.MINER) {
                    if (player.getLocation().getBlockY() <= 25 && !player.hasPotionEffect(PotionEffectType.INVISIBILITY)) {
                        addPotionEffect(player, PotionEffectType.INVISIBILITY, -1, 2);
                    } else if (player.hasPotionEffect(PotionEffectType.INVISIBILITY) && player.getActivePotionEffects().stream().anyMatch(effect ->
                            effect.getType() == PotionEffectType.INVISIBILITY
                                    && effect.getAmplifier() >= 2
                                    && player.getLocation().getBlockY() > 25)) {
                        removePotionEffect(player, PotionEffectType.INVISIBILITY);
                    }
                }
                else if (type == ClassType.BARD) {
                    checkBardHoldingEffects(player);
                }
                checkEffects(player);
            }
        }
    }

    public static void updateArcherEnergy(Player player) {
        if (!archerEnergy.containsKey(player.getUniqueId())) return;
        int energy = archerEnergy.get(player.getUniqueId()).getEnergy();
        if (energy <= 99) {
            archerEnergy.get(player.getUniqueId()).setEnergy(energy + 1);
            energy += 1;
            if (energy == 100) {
                player.sendMessage(ChatColor.YELLOW + "You are at " + ChatColor.WHITE + "maximum" + ChatColor.YELLOW + " archer energy.");
            }
        }
        if (energy % 10 == 0 && energy != 100) {
            player.sendMessage(ChatColor.YELLOW + "You are at " + ChatColor.WHITE + energy + ChatColor.YELLOW + " archer energy.");
        }
    }

    public static void updateBardEnergy(Player player) {
        if (!bardEnergy.containsKey(player.getUniqueId())) return;
        int energy = bardEnergy.get(player.getUniqueId()).getEnergy();
        if (energy <= 119) {
            bardEnergy.get(player.getUniqueId()).setEnergy(energy + 1);
            energy += 1;
            if (energy == 120) {
                player.sendMessage(ChatColor.YELLOW + "You are at " + ChatColor.WHITE + "maximum" + ChatColor.YELLOW + " bard energy.");
            }
        }
        if (energy % 10 == 0 && energy != 120) {
            player.sendMessage(ChatColor.YELLOW + "You are at " + ChatColor.WHITE + energy + ChatColor.YELLOW + " bard energy.");
        }
    }

    public static void updateRogueEnergy(Player player) {
        if (!rogueEnergy.containsKey(player.getUniqueId())) return;
        int energy = rogueEnergy.get(player.getUniqueId()).getEnergy();
        if (energy <= 119) {
            rogueEnergy.get(player.getUniqueId()).setEnergy(energy + 1);
            energy += 1;
            if (energy == 120) {
                player.sendMessage(ChatColor.YELLOW + "You are at " + ChatColor.WHITE + "maximum" + ChatColor.YELLOW + " rogue energy.");
            }
        }
        if (energy % 10 == 0 && energy != 120) {
            player.sendMessage(ChatColor.YELLOW + "You are at " + ChatColor.WHITE + energy + ChatColor.YELLOW + " rogue energy.");
        }
    }

    private static void checkEffects(Player player) {
        checkEffects(player, false);
    }

    public static void checkEffects(Player player, boolean force) {
        ClassType type = activeClass.get(player.getUniqueId());
        if (type == null || type == ClassType.MINER) return;
        List<PotionEffect> effects = null;
        switch (type) {
            case BARD:
            case MAGE:
                effects = Arrays.asList(new PotionEffect(PotionEffectType.SPEED, -1, 1), new PotionEffect(PotionEffectType.RESISTANCE, -1, 1), new PotionEffect(PotionEffectType.REGENERATION, -1, 0));
                break;
            case ARCHER:
            case ILLUSIONIST:
                effects = Arrays.asList(new PotionEffect(PotionEffectType.SPEED, -1, 2), new PotionEffect(PotionEffectType.RESISTANCE, -1,2));
                break;
            case ROGUE:
                effects = Arrays.asList(new PotionEffect(PotionEffectType.SPEED, -1, 2), new PotionEffect(PotionEffectType.RESISTANCE, -1, 1), new PotionEffect(PotionEffectType.JUMP_BOOST, -1, 1));
                break;
            case FISHERMAN:
                effects = Arrays.asList(new PotionEffect(PotionEffectType.SPEED, -1, 1), new PotionEffect(PotionEffectType.RESISTANCE, -1, 1), new PotionEffect(PotionEffectType.REGENERATION, -1, 0), new PotionEffect(PotionEffectType.JUMP_BOOST, -1, 1));;
                break;
        }
        if (effects == null) return;
        for (PotionEffect effect : effects) {
            if (!force || player.getActivePotionEffects().contains(effect)) {
                continue;
            }
            player.addPotionEffect(effect);
        }
    }
    private static void checkBardHoldingEffects(Player player) {
        if (!activeClass.containsKey(player.getUniqueId()) || activeClass.get(player.getUniqueId()) != ClassType.BARD) return;
        Team team = TeamManager.getTeamByPlayer(player);
        //if (team == null || Teams.getInstance().getServerManager().getPvPTimer(player) > 0L) return;
        String parseString = getPotionEffectFromItem(player);
        if (parseString == null) return;
        String[] parse = parseString.split(":");
        String[] potionString = parse[0].split("-");
        PotionEffect effect = new PotionEffect(PotionEffectType.getByName(potionString[0].toUpperCase()), 90, Integer.parseInt(potionString[1]));
        boolean canBardUse = Boolean.parseBoolean(parse[1]);
        for (Player players : team.getOnlineMembers()) {
            if (/*Teams.getInstance().getServerManager().getPvPTimer(players)  > 0L ||*/ (activeClass.get(player.getUniqueId()) != null && activeClass.get(player.getUniqueId()) == ClassType.ARCHER && effect.getType() == PotionEffectType.SPEED))
                return;
            if ((players == player && !canBardUse) ||  players.getLocation().distanceSquared(player.getLocation()) > 20.0) {
                continue;
            }
            addBardHoldPotionEffect(players, effect);
        }
    }

    private static String getPotionEffectFromItem(Player player) {
        switch (player.getItemInHand().getType()) {
            case SUGAR:
                return "SPEED-1:false";
            case BLAZE_POWDER:
                return "INCREASE_DAMAGE-0:true";
            case IRON_INGOT:
                return "DAMAGE_RESISTANCE-0:false";
            case FEATHER:
                return "JUMP-1:true";
            case MAGMA_CREAM:
                return "FIRE_RESISTANCE-1:true";
            case GHAST_TEAR:
                return "REGENERATION-1:false";
            default:
                return null;
        }
    }

    public static void addBardHoldPotionEffect(Player player, PotionEffect toGive) {
        if (!player.hasPotionEffect(toGive.getType())) {
            Bukkit.getScheduler().runTask(kSMP.instance, () -> {
                player.addPotionEffect(toGive);
            });
            return;
        }
        for (PotionEffect effect : player.getActivePotionEffects()) {
            if (effect.getType() == toGive.getType()) {
                if (toGive.getAmplifier() < effect.getAmplifier()) {
                    return;
                }
                if (toGive.getAmplifier() == effect.getAmplifier() && toGive.getDuration() < effect.getDuration()) {
                    return;
                }
                if (toGive.getAmplifier() <= effect.getAmplifier() && (toGive.getAmplifier() != effect.getAmplifier() || toGive.getDuration() <= effect.getDuration())) {
                    continue;
                }
                if (restoreBardHoldEffects.containsKey(player.getUniqueId() + "_" + effect.getType().getName())) {
                    restoreBardHoldEffects.get(player.getUniqueId() + "_" + effect.getType().getName()).cancel();
                    RestoreBardEffectsTask restoreBardEffectsTask = new RestoreBardEffectsTask(player, restoreBardHoldEffects.get(player.getUniqueId() + "_" + effect.getType().getName()).getEffect(), toGive.getDuration() - 2);
                    restoreBardHoldEffects.put(player.getUniqueId() + "_" + effect.getType().getName(), restoreBardEffectsTask);
                } else {
                    RestoreBardEffectsTask restoreBardEffectsTask = new RestoreBardEffectsTask(player, effect, toGive.getDuration() - 2);
                    restoreBardHoldEffects.put(player.getUniqueId() + "_" + effect.getType().getName(), restoreBardEffectsTask);
                }
                player.removePotionEffect(toGive.getType());
                player.addPotionEffect(toGive);
            }
        }
    }

    public void addBardClickablePotionEffect(Player player, PotionEffect toGive) {
        if (!player.hasPotionEffect(toGive.getType())) {
            player.addPotionEffect(toGive);
            return;
        }
        for (PotionEffect potionEffect : player.getActivePotionEffects()) {
            if (potionEffect.getType().equals(toGive.getType())) {
                if (toGive.getAmplifier() < potionEffect.getAmplifier()) {
                    return;
                }
                if (toGive.getAmplifier() == potionEffect.getAmplifier() && toGive.getDuration() < potionEffect.getDuration()) {
                    return;
                }
                if (toGive.getAmplifier() <= potionEffect.getAmplifier() && (toGive.getAmplifier() != potionEffect.getAmplifier() || toGive.getDuration() <= potionEffect.getDuration())) {
                    continue;
                }
                if (restoreBardHoldEffects.containsKey(player.getUniqueId() + "_" + potionEffect.getType().getName())) {
                    restoreBardHoldEffects.get(player.getUniqueId() + "_" + potionEffect.getType().getName()).cancel();
                    RestoreBardEffectsTask restoreBardEffectsTask = new RestoreBardEffectsTask(player, this.restoreBardHoldEffects.get(player.getUniqueId() + "_" + potionEffect.getType().getName()).getEffect(), toGive.getDuration() - 2);
                    restoreBardHoldEffects.put(player.getUniqueId() + "_" + potionEffect.getType().getName(), restoreBardEffectsTask);
                    player.removePotionEffect(toGive.getType());
                    player.addPotionEffect(toGive);
                } else {
                    RestoreBardEffectsTask restoreBardEffectsTask = new RestoreBardEffectsTask(player, potionEffect, toGive.getDuration() - 2);
                    restoreBardHoldEffects.put(player.getUniqueId() + "_" + potionEffect.getType().getName(), restoreBardEffectsTask);
                    player.removePotionEffect(toGive.getType());
                    player.addPotionEffect(toGive);
                }
            }

        }
    }
    private static void addPotionEffect(Player player, PotionEffectType type, int duration, int level) {
        if (!player.hasPotionEffect(type)) {
            player.addPotionEffect(new PotionEffect(type, duration, level));
        }
    }

    private static void removePotionEffect(Player player, PotionEffectType type) {
        if (player.hasPotionEffect(type)) {
            player.removePotionEffect(type);
        }
    }

    private boolean hasArmorOn(Player player) {
        int amount = 0;
        if (player.getInventory().getHelmet() != null) {
            ++amount;
        }
        if (player.getInventory().getChestplate() != null) {
            ++amount;
        }
        if (player.getInventory().getLeggings() != null) {
            ++amount;
        }
        if (player.getInventory().getBoots() != null) {
            ++amount;
        }
        return amount == 4;
    }

    private static boolean hasKitOn(Player player, ClassType type) {
        int amount = 0;
        if (type == ClassType.BARD) {
            if (player.getInventory().getHelmet() != null && player.getInventory().getHelmet().getType() == Material.GOLDEN_HELMET) {
                ++amount;
            }
            if (player.getInventory().getChestplate() != null && player.getInventory().getChestplate().getType() == Material.GOLDEN_CHESTPLATE) {
                ++amount;
            }
            if (player.getInventory().getLeggings() != null && player.getInventory().getLeggings().getType() == Material.GOLDEN_LEGGINGS) {
                ++amount;
            }
            if (player.getInventory().getBoots() != null && player.getInventory().getBoots().getType() == Material.GOLDEN_BOOTS) {
                ++amount;
            }
        }
        else if (type == ClassType.MINER) {
            if (player.getInventory().getHelmet() != null && player.getInventory().getHelmet().getType() == Material.IRON_HELMET) {
                ++amount;
            }
            if (player.getInventory().getChestplate() != null && player.getInventory().getChestplate().getType() == Material.IRON_CHESTPLATE) {
                ++amount;
            }
            if (player.getInventory().getLeggings() != null && player.getInventory().getLeggings().getType() == Material.IRON_LEGGINGS) {
                ++amount;
            }
            if (player.getInventory().getBoots() != null && player.getInventory().getBoots().getType() == Material.IRON_BOOTS) {
                ++amount;
            }
        }
        else if (type == ClassType.ARCHER) {
            if (player.getInventory().getHelmet() != null && player.getInventory().getHelmet().getType() == Material.LEATHER_HELMET) {
                ++amount;
            }
            if (player.getInventory().getChestplate() != null && player.getInventory().getChestplate().getType() == Material.LEATHER_CHESTPLATE) {
                ++amount;
            }
            if (player.getInventory().getLeggings() != null && player.getInventory().getLeggings().getType() == Material.LEATHER_LEGGINGS) {
                ++amount;
            }
            if (player.getInventory().getBoots() != null && player.getInventory().getBoots().getType() == Material.LEATHER_BOOTS) {
                ++amount;
            }
        }
        else if (type == ClassType.ROGUE) {
            if (player.getInventory().getHelmet() != null && player.getInventory().getHelmet().getType() == Material.CHAINMAIL_HELMET) {
                ++amount;
            }
            if (player.getInventory().getChestplate() != null && player.getInventory().getChestplate().getType() == Material.CHAINMAIL_CHESTPLATE) {
                ++amount;
            }
            if (player.getInventory().getLeggings() != null && player.getInventory().getLeggings().getType() == Material.CHAINMAIL_LEGGINGS) {
                ++amount;
            }
            if (player.getInventory().getBoots() != null && player.getInventory().getBoots().getType() == Material.CHAINMAIL_BOOTS) {
                ++amount;
            }
        }
        return amount == 4;
    }

    public static boolean isBardMaterial(Material material) {
        return bardMaterials.contains(material);
    }

    public ClassType getActiveClass(Player player) {
        return activeClass.getOrDefault(player.getUniqueId(), null);
    }

    public Energy getEnergy(Player player) {
        return bardEnergy.getOrDefault(player.getUniqueId(), archerEnergy.getOrDefault(player.getUniqueId(), rogueEnergy.getOrDefault(player.getUniqueId(), new Energy(player))));
    }

    public static void activateClass(Player player, ClassType type) {
        if (activeClass.containsKey(player.getUniqueId())) return;
        deactiveClass(player, false);
        activeClass.put(player.getUniqueId(), type);
        player.sendMessage(ChatColor.YELLOW + "You have enabled the " + ChatColor.WHITE + WordUtils.capitalizeFully(type.name().toLowerCase()) + ChatColor.YELLOW + " class.");
        Color helmetColor = null;
        switch (type) {
            case BARD:
                bardEnergy.put(player.getUniqueId(), new Energy(player));
                player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(30);
                break;
            case ARCHER:
                archerEnergy.put(player.getUniqueId(), new Energy(player));
                player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(40);
                break;
            case MINER:
                player.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, -1, 1));
                player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, -1, 0));
                player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, -1, 0));
                player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(30);
                MinerUpgrade minerUpgrade = MinerUpgrade.getLevelBasedOnDiamonds(ProfileManager.getProfileByPlayer(player).diamonds);
                minerUpgrade.getPotionEffects().forEach(player::addPotionEffect);
                minerUpgrades.put(player.getUniqueId(), minerUpgrade);
                break;
            case ROGUE:
                rogueEnergy.put(player.getUniqueId(), new Energy(player));
                player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(30);
                break;
        }
        checkEffects(player, true);
        if (helmetColor != null) {
/*            PacketPlayOutEntityEquipment packet = new PacketPlayOutEntityEquipment(player.getEntityId(), 4, CraftItemStack.asNMSCopy(ItemBuilder.from(Material.LEATHER_HELMET).color(helmetColor).build()));
            for (Player players : Bukkit.getOnlinePlayers()) {
                if (player == players) {
                    continue;
                }
                ((CraftPlayer) players).getHandle().playerConnection.sendPacket(packet);

            }*/
        }
    }

    public static void deactiveClass(Player player, boolean sendMessage) {
        if (!activeClass.containsKey(player.getUniqueId())) return;
        ClassType type = activeClass.get(player.getUniqueId());
        if (sendMessage) {
            player.sendMessage(ChatColor.YELLOW + "You have disabled the " + ChatColor.WHITE + WordUtils.capitalizeFully(type.name().toLowerCase()) + ChatColor.YELLOW + " class.");
        }
        activeClass.remove(player.getUniqueId());
        bardEnergy.remove(player.getUniqueId());
        archerEnergy.remove(player.getUniqueId());
        rogueEnergy.remove(player.getUniqueId());
        restoreBardHoldEffects.remove(player.getUniqueId());
        minerUpgrades.remove(player.getUniqueId());
        player.setMaxHealth(20);
        for (PotionEffect effect : player.getActivePotionEffects()) {
            player.removePotionEffect(effect.getType());
        }
    }
}
