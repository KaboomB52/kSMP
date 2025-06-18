package net.minebo.smp.classes.listeners;

import net.minebo.cobalt.cooldown.construct.Cooldown;
import net.minebo.smp.kSMP;
import net.minebo.smp.classes.ClassType;
import net.minebo.smp.classes.ClassManager;
import net.minebo.smp.classes.objects.Energy;
import net.minebo.smp.classes.objects.MinerUpgrade;
import net.minebo.smp.profile.ProfileManager;
import net.minebo.smp.profile.construct.Profile;
import net.minebo.smp.team.TeamManager;
import net.minebo.smp.team.construct.Team;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class ClassListener implements Listener {

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.getBlock().getType() == Material.DIAMOND_ORE) {
            Player player = event.getPlayer();
            Block block = event.getBlock();
            Profile profile = ProfileManager.getProfileByUUID(player.getUniqueId());
            profile.addDiamonds(1);
            if (!ClassManager.placed.remove(block.getLocation().toString())) {
                int diamondsMined = profile.diamonds;
                int count = 1;
                ClassManager.placed.add(block.getLocation().toString());

                for(int x = -5; x < 5; ++x) {
                    for(int y = -5; y < 5; ++y) {
                        for(int z = -5; z < 5; ++z) {
                            Block otherBlock = block.getLocation().clone().add(x, y, z).getBlock();
                            if (!otherBlock.equals(block) && (otherBlock.getType() == Material.DIAMOND_ORE || otherBlock.getType() == Material.DEEPSLATE_DIAMOND_ORE) && !ClassManager.placed.contains(otherBlock.getLocation().toString())) {
                                ++count;
                                ClassManager.placed.add(otherBlock.getLocation().toString());
                            }
                        }
                    }
                }

                Bukkit.broadcastMessage("[FD] " + player.getDisplayName() + ChatColor.WHITE + " just found " + ChatColor.AQUA + count + ChatColor.WHITE + " diamond" + (count == 1 ? "" : "s") + ".");
                if (ClassManager.activeClass.get(player.getUniqueId()) == ClassType.MINER) {
                    MinerUpgrade upgrade = MinerUpgrade.getLevelBasedOnDiamonds(diamondsMined);
                    if (upgrade.getDiamondsNeeded() <= 0 || ClassManager.minerUpgrades.getOrDefault(player.getUniqueId(), MinerUpgrade.NONE) == upgrade) {
                        return;
                    }

                    player.sendMessage(ChatColor.GREEN + "You have upgraded to Miner Upgrade " + StringUtils.capitalize(upgrade.name().toLowerCase()) + "!");
                    ClassManager.deactiveClass(player, false);
                    ClassManager.minerUpgrades.put(player.getUniqueId(), upgrade);
                    Bukkit.getScheduler().runTaskLater(kSMP.instance, () -> ClassManager.activateClass(player, ClassType.MINER), 5L);
                }
            }
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (event.getBlock().getType() == Material.DIAMOND_ORE) {
            ClassManager.placed.add(event.getBlock().getLocation().toString());
        }

    }

    @EventHandler
    public void onPistonExtend(BlockPistonExtendEvent event) {
        for(Block block : event.getBlocks()) {
            if (block.getType() == Material.DIAMOND_ORE) {
                ClassManager.placed.add(block.getLocation().toString());
            }
        }

    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        Entity var5 = event.getDamager();
        if (var5 instanceof Arrow arrow) {
            ProjectileSource var12 = arrow.getShooter();
            if (var12 instanceof Player damager) {
                Entity var13 = event.getEntity();
                if (var13 instanceof Player player) {
                    if (!ClassManager.activeClass.containsKey(damager.getUniqueId()) || ClassManager.activeClass.get(damager.getUniqueId()) != ClassType.ARCHER || ClassManager.activeClass.containsKey(player.getUniqueId()) && ClassManager.activeClass.get(player.getUniqueId()) == ClassType.ARCHER || player == damager) {
                        return;
                    }

                    Team team = TeamManager.getTeamByPlayer(damager);
                    if (team != null && team.getOnlineMembers().contains(player)) {
                        return;
                    }

                    damager.sendMessage(ChatColor.RED + "You have archer tagged " + player.getDisplayName() + ChatColor.RED + "! All inflicted damage will be for " + ChatColor.YELLOW + "50%" + ChatColor.RED + " more!");
                    player.sendMessage(ChatColor.RED + "You have been archer tagged by " + damager.getDisplayName() + ChatColor.RED + "!");
                    Cooldown archerTag = kSMP.cooldownHandler.getCooldown("Archer Tag");
                    archerTag.applyCooldown(player, 15L, TimeUnit.SECONDS, kSMP.instance);
                }
            }
        }

        if (event.getEntity() instanceof Player) {
            if (event.getDamager() instanceof Player) {
                Player victim = (Player) event.getEntity();
                Cooldown archerTag = kSMP.cooldownHandler.getCooldown("Archer Tag");
                if (archerTag.onCooldown(victim)) {
                    event.setDamage(event.getDamage() * (double)1.5F);
                }
            }
        }

    }

    public Material[] bardEffects = { Material.SUGAR, Material.BLAZE_POWDER, Material.FEATHER, Material.MAGMA_CREAM, Material.IRON_INGOT, Material.GHAST_TEAR };
    public Material[] archerEffects = { Material.SUGAR, Material.FEATHER };
    public Material[] rogueEffects = { Material.SUGAR, Material.FEATHER };

    @EventHandler()
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        // Uncomment for debug: Check if event is firing
        //player.sendMessage("DEBUG: Interact event fired! Action: " + event.getAction());

        if (ClassManager.activeClass.containsKey(player.getUniqueId())
                && (event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_AIR)
                && player.getInventory().getItemInMainHand().getType() != Material.AIR) {

            if (player.hasMetadata("ability_used")) return; // prevent duplicate
            player.setMetadata("ability_used", new FixedMetadataValue(kSMP.instance, true));

            Bukkit.getScheduler().runTaskLater(kSMP.instance, () -> {
                player.removeMetadata("ability_used", kSMP.instance);
            }, 1L); // remove metadata in next tick becuz spigot be whiny

            switch (ClassManager.activeClass.get(player.getUniqueId())){

                case ARCHER -> {
                    handleArcherAbility(player);
                }

                case ROGUE -> {
                    handleRogueAbility(player);
                }

                case BARD -> {
                    handleBardAbility(player);
                }
            }

        }
    }

    private void handleArcherAbility(Player player) {
        Energy energy = ClassManager.archerEnergy.get(player.getUniqueId());
        if (energy == null) return; // Safety check
        ItemStack mainHand = player.getInventory().getItemInMainHand();
        Material type = mainHand.getType();
        switch (type) {
            case SUGAR -> {
                Cooldown archerSugar = kSMP.cooldownHandler.getCooldown("Archer Sugar");
                if (archerSugar.onCooldown(player)) {
                    player.sendMessage(ChatColor.RED + "You cannot use this for another " + archerSugar.getRemaining(player) + '.');
                    return;
                }
                if (energy.getEnergy() >= 25) {
                    archerSugar.applyCooldown(player, 25, TimeUnit.SECONDS, kSMP.instance);
                    removeItemOrSetEmpty(player, mainHand);
                    ClassManager.archerEnergy.get(player.getUniqueId()).setEnergy(energy.getEnergy() - 25);
                    player.removePotionEffect(PotionEffectType.SPEED);
                    player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 160, 4));
                    Bukkit.getScheduler().runTaskLater(kSMP.instance, () -> {
                        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, -1, 2));
                    }, 165L);
                    player.sendMessage(ChatColor.YELLOW + "You have " + ChatColor.AQUA + "Speed V" + ChatColor.YELLOW + " for 8 seconds!");
                } else {
                    player.sendMessage(ChatColor.RED + "You need " + (25 - energy.getEnergy()) + " more energy to use this.");
                }
            }
            case FEATHER -> {
                Cooldown archerFeather = kSMP.cooldownHandler.getCooldown("Archer Feather");
                if (archerFeather.onCooldown(player)) {
                    player.sendMessage(ChatColor.RED + "You cannot use this for another " + archerFeather.getRemaining(player) + '.');
                    return;
                }
                if (energy.getEnergy() >= 25) {
                    archerFeather.applyCooldown(player, 25L, TimeUnit.SECONDS, kSMP.instance);
                    removeItemOrSetEmpty(player, mainHand);
                    ClassManager.archerEnergy.get(player.getUniqueId()).setEnergy(energy.getEnergy() - 25);
                    player.removePotionEffect(PotionEffectType.JUMP_BOOST);
                    player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, 160, 4));
                    player.sendMessage(ChatColor.YELLOW + "You have " + ChatColor.LIGHT_PURPLE + "Jump Boost V" + ChatColor.YELLOW + " for 8 seconds!");
                } else {
                    player.sendMessage(ChatColor.RED + "You need " + (25 - energy.getEnergy()) + " more energy to use this.");
                }
            }
            default -> {
                // Do nothing
            }
        }
    }

    private void handleRogueAbility(Player player) { // Same as Archer
        Energy energy = ClassManager.rogueEnergy.get(player.getUniqueId());
        if (energy == null) return; // Safety check
        ItemStack mainHand = player.getInventory().getItemInMainHand();
        Material type = mainHand.getType();
        switch (type) {
            case SUGAR -> {
                Cooldown archerSugar = kSMP.cooldownHandler.getCooldown("Rogue Sugar");
                if (archerSugar.onCooldown(player)) {
                    player.sendMessage(ChatColor.RED + "You cannot use this for another " + archerSugar.getRemaining(player) + '.');
                    return;
                }
                if (energy.getEnergy() >= 25) {
                    archerSugar.applyCooldown(player, 25, TimeUnit.SECONDS, kSMP.instance);
                    removeItemOrSetEmpty(player, mainHand);
                    ClassManager.rogueEnergy.get(player.getUniqueId()).setEnergy(energy.getEnergy() - 25);
                    player.removePotionEffect(PotionEffectType.SPEED);
                    player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 160, 4));
                    Bukkit.getScheduler().runTaskLater(kSMP.instance, () -> {
                        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, -1, 2));
                    }, 165L);
                    player.sendMessage(ChatColor.YELLOW + "You have " + ChatColor.AQUA + "Speed V" + ChatColor.YELLOW + " for 8 seconds!");
                } else {
                    player.sendMessage(ChatColor.RED + "You need " + (25 - energy.getEnergy()) + " more energy to use this.");
                }
            }
            case FEATHER -> {
                Cooldown archerFeather = kSMP.cooldownHandler.getCooldown("Rogue Feather");
                if (archerFeather.onCooldown(player)) {
                    player.sendMessage(ChatColor.RED + "You cannot use this for another " + archerFeather.getRemaining(player) + '.');
                    return;
                }
                if (energy.getEnergy() >= 25) {
                    archerFeather.applyCooldown(player, 25L, TimeUnit.SECONDS, kSMP.instance);
                    removeItemOrSetEmpty(player, mainHand);
                    ClassManager.rogueEnergy.get(player.getUniqueId()).setEnergy(energy.getEnergy() - 25);
                    player.removePotionEffect(PotionEffectType.JUMP_BOOST);
                    player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, 160, 4));
                    Bukkit.getScheduler().runTaskLater(kSMP.instance, () -> {
                        player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, -1, 1));
                    }, 165L);
                    player.sendMessage(ChatColor.YELLOW + "You have " + ChatColor.LIGHT_PURPLE + "Jump Boost V" + ChatColor.YELLOW + " for 8 seconds!");
                } else {
                    player.sendMessage(ChatColor.RED + "You need " + (25 - energy.getEnergy()) + " more energy to use this.");
                }
            }
            default -> {
                // Do nothing
            }
        }
    }

    private void handleBardAbility(Player player) {
        if(Arrays.stream(bardEffects).noneMatch(m -> m == player.getInventory().getItemInMainHand().getType())) {
            return;
        }
        var bardCooldown = kSMP.cooldownHandler.getCooldown("Bard Effect");
        if (bardCooldown.onCooldown(player)) {
            player.sendMessage(ChatColor.RED + "You cannot use another bard effect for " + bardCooldown.getRemaining(player) + '.');
            return;
        }
        Team team = TeamManager.getTeamByPlayer(player);
        List<Player> nearbyMembers = new ArrayList<>();
        if (team != null) {
            for (Player onlinePlayer : team.getOnlineMembers()) {
                if (onlinePlayer != null && onlinePlayer.getLocation().distance(player.getLocation()) <= 20) {
                    nearbyMembers.add(onlinePlayer);
                }
            }
        } else {
            nearbyMembers.add(player);
        }
        Energy energy = ClassManager.bardEnergy.get(player.getUniqueId());
        if (energy == null) return; // Safety
        boolean effectDone = false;
        int cost = 0;
        ItemStack mainHand = player.getInventory().getItemInMainHand();
        Material type = mainHand.getType();
        switch (type) {
            case SUGAR -> {
                cost = 25;
                if (energy.getEnergy() >= cost) {
                    effectDone = true;
                    removeItemOrSetEmpty(player, mainHand);
                    ClassManager.bardEnergy.get(player.getUniqueId()).setEnergy(energy.getEnergy() - cost);
                    int gaveTo = 0;
                    for (Player nearbyMember : nearbyMembers) {
                        gaveTo += 1;
                        addBardClickablePotionEffect(nearbyMember, new PotionEffect(PotionEffectType.SPEED, 240, 2));
                    }
                    if ((gaveTo == 0 || gaveTo == 1) && (team != null && team.getOnlineMembers().size() > 1)) {
                        player.sendMessage(ChatColor.RED + "Nobody else got your bard effects!");
                        return;
                    }
                    player.sendMessage(ChatColor.WHITE.toString() + (gaveTo - 1) + ChatColor.YELLOW + " nearby teammate" + (gaveTo != 1 ? "s" : "") + " have " + ChatColor.AQUA + "Speed III" + ChatColor.YELLOW + " for 12 seconds!");
                } else {
                    player.sendMessage(ChatColor.RED + "You need " + (cost - energy.getEnergy()) + " more energy to use this.");
                }
            }
            case BLAZE_POWDER -> {
                cost = 60;
                if (energy.getEnergy() >= cost) {
                    effectDone = true;
                    removeItemOrSetEmpty(player, mainHand);
                    ClassManager.bardEnergy.get(player.getUniqueId()).setEnergy(energy.getEnergy() - cost);
                    int gaveTo = 0;
                    for (Player nearbyMember : nearbyMembers) {
                        if (nearbyMember == player) continue;
                        addBardClickablePotionEffect(nearbyMember, new PotionEffect(PotionEffectType.STRENGTH, 240, 1));
                        gaveTo += 1;
                    }
                    if ((gaveTo == 0 || gaveTo == 1) && (team != null && team.getOnlineMembers().size() > 1)) {
                        player.sendMessage(ChatColor.RED + "Nobody else got your bard effects!");
                        return;
                    }
                    player.sendMessage(ChatColor.WHITE.toString() + (gaveTo - 1) + ChatColor.YELLOW + " nearby teammate" + (gaveTo > 1 ? "s" : "") + " have " + ChatColor.RED + "Strength II" + ChatColor.YELLOW + " for 12 seconds!");
                } else {
                    player.sendMessage(ChatColor.RED + "You need " + (cost - energy.getEnergy()) + " more energy to use this.");
                }
            }
            case IRON_INGOT -> {
                cost = 40;
                if (energy.getEnergy() >= cost) {
                    effectDone = true;
                    removeItemOrSetEmpty(player, mainHand);
                    ClassManager.bardEnergy.get(player.getUniqueId()).setEnergy(energy.getEnergy() - cost);
                    int gaveTo = 0;
                    for (Player nearbyMember : nearbyMembers) {
                        addBardClickablePotionEffect(nearbyMember, new PotionEffect(PotionEffectType.RESISTANCE, 240, 2));
                        gaveTo += 1;
                    }
                    if ((gaveTo == 0 || gaveTo == 1) && (team != null && team.getOnlineMembers().size() > 1)) {
                        player.sendMessage(ChatColor.RED + "Nobody else got your bard effects!");
                        return;
                    }
                    player.sendMessage(ChatColor.WHITE.toString() + (gaveTo - 1) + ChatColor.YELLOW + " nearby teammate" + (gaveTo > 1 ? "s" : "") + " have " + ChatColor.GOLD + "Resistance III" + ChatColor.YELLOW + " for 12 seconds!");
                } else {
                    player.sendMessage(ChatColor.RED + "You need " + (cost - energy.getEnergy()) + " more energy to use this.");
                }
            }
            case FEATHER -> {
                cost = 20;
                if (energy.getEnergy() >= cost) {
                    effectDone = true;
                    removeItemOrSetEmpty(player, mainHand);
                    ClassManager.bardEnergy.get(player.getUniqueId()).setEnergy(energy.getEnergy() - cost);
                    int gaveTo = 0;
                    for (Player nearbyMember : nearbyMembers) {
                        addBardClickablePotionEffect(nearbyMember, new PotionEffect(PotionEffectType.JUMP_BOOST, 240, 4));
                        gaveTo += 1;
                    }
                    if ((gaveTo == 0 || gaveTo == 1) && (team != null && team.getOnlineMembers().size() > 1)) {
                        player.sendMessage(ChatColor.RED + "Nobody else got your bard effects!");
                        return;
                    }
                    player.sendMessage(ChatColor.WHITE.toString() + (gaveTo - 1) + ChatColor.YELLOW + " nearby teammate" + (gaveTo > 1 ? "s" : "") + " have " + ChatColor.LIGHT_PURPLE + "Jump Boost V" + ChatColor.YELLOW + " for 12 seconds!");
                } else {
                    player.sendMessage(ChatColor.RED + "You need " + (cost - energy.getEnergy()) + " more energy to use this.");
                }
            }
            case MAGMA_CREAM -> {
                cost = 35;
                if (energy.getEnergy() >= cost) {
                    effectDone = true;
                    removeItemOrSetEmpty(player, mainHand);
                    ClassManager.bardEnergy.get(player.getUniqueId()).setEnergy(energy.getEnergy() - cost);
                    int gaveTo = 0;
                    for (Player nearbyMember : nearbyMembers) {
                        addBardClickablePotionEffect(nearbyMember, new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 20 * 60, 0));
                        gaveTo += 1;
                    }
                    if ((gaveTo == 0 || gaveTo == 1) && (team != null && team.getOnlineMembers().size() > 1)) {
                        player.sendMessage(ChatColor.RED + "Nobody else got your bard effects!");
                        return;
                    }
                    player.sendMessage(ChatColor.WHITE.toString() + (gaveTo - 1) + ChatColor.YELLOW + " nearby teammate" + (gaveTo > 1 ? "s" : "") + " have " + ChatColor.RED + "Fire Resistance" + ChatColor.YELLOW + " for 60 seconds!");
                } else {
                    player.sendMessage(ChatColor.RED + "You need " + (cost - energy.getEnergy()) + " more energy to use this.");
                }
            }
            case GHAST_TEAR -> {
                cost = 45;
                if (energy.getEnergy() >= cost) {
                    effectDone = true;
                    removeItemOrSetEmpty(player, mainHand);
                    ClassManager.bardEnergy.get(player.getUniqueId()).setEnergy(energy.getEnergy() - cost);
                    int gaveTo = 0;
                    for (Player nearbyMember : nearbyMembers) {
                        addBardClickablePotionEffect(nearbyMember, new PotionEffect(PotionEffectType.REGENERATION, 240, 1));
                        gaveTo += 1;
                    }
                    if ((gaveTo == 0 || gaveTo == 1) && (team != null && team.getOnlineMembers().size() > 1)) {
                        player.sendMessage(ChatColor.RED + "Nobody else got your bard effects!");
                        return;
                    }
                    player.sendMessage(ChatColor.WHITE.toString() + (gaveTo - 1) + ChatColor.YELLOW + " nearby teammate" + (gaveTo > 1 ? "s" : "") + " have " + ChatColor.LIGHT_PURPLE + "Regeneration II" + ChatColor.YELLOW + " for 12 seconds!");
                } else {
                    player.sendMessage(ChatColor.RED + "You need " + (cost - energy.getEnergy()) + " more energy to use this.");
                }
            }
            case SPIDER_EYE -> {
                cost = 50;
                if (energy.getEnergy() >= cost) {
                    effectDone = true;
                    removeItemOrSetEmpty(player, mainHand);
                    ClassManager.bardEnergy.get(player.getUniqueId()).setEnergy(energy.getEnergy() - cost);
                    int gaveTo = 0;
                    for (Entity entity : player.getNearbyEntities(20, 20, 20)) {
                        if (!(entity instanceof Player targetPlayer)) continue;
                        // Don't hit spawn-protected or teammates
                        if (team != null && team.getOnlineMembers().contains(targetPlayer))
                            continue;
                        targetPlayer.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 240, 1));
                        gaveTo += 1;
                    }
                    if (gaveTo == 0) {
                        player.sendMessage(ChatColor.RED + "No enemies got withered.");
                        return;
                    }
                    player.sendMessage(ChatColor.WHITE.toString() + gaveTo + ChatColor.YELLOW + " nearby " + (gaveTo > 1 ? "enemies" : "enemy") + " have been " + ChatColor.WHITE + "withered" + ChatColor.YELLOW + " for 12 seconds!");
                } else {
                    player.sendMessage(ChatColor.RED + "You need " + (cost - energy.getEnergy()) + " more energy to use this.");
                }
            }
            default -> {
                // Do nothing
            }
        }
        if (effectDone) {
            kSMP.cooldownHandler.getCooldown("Bard Effect").applyCooldown(player, 10, TimeUnit.SECONDS, kSMP.instance);
        }
    }

    // Remove 1 item or set hand to empty
    private void removeItemOrSetEmpty(Player player, ItemStack item) {
        if (item.getAmount() > 1) {
            item.setAmount(item.getAmount() - 1);
            player.getInventory().setItemInMainHand(item);
        } else {
            player.getInventory().setItemInMainHand(null);
        }
    }

    // Apply or override a potion effect for bard clickable abilities
    private void addBardClickablePotionEffect(Player player, PotionEffect toGive) {
        if (!player.hasPotionEffect(toGive.getType())) {
            player.addPotionEffect(toGive);
            Bukkit.getScheduler().runTaskLater(kSMP.instance, () -> ClassManager.checkEffects(player, true), toGive.getDuration()+5);
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
                player.removePotionEffect(toGive.getType());
                player.addPotionEffect(toGive);
                Bukkit.getScheduler().runTaskLater(kSMP.instance, () -> ClassManager.checkEffects(player, true), toGive.getDuration()+5);
                return;
            }
        }
        // If the effect type was not found, add it
        player.addPotionEffect(toGive);
    }

    public static Map<UUID, Long> backstabCooldown = new HashMap<>();

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBackstab(EntityDamageByEntityEvent event) {
        if (event.isCancelled()) return;

        if (event.getDamager() instanceof Player && event.getEntity() instanceof Player) {
            Player damager = (Player) event.getDamager();
            Player victim = (Player) event.getEntity();

            if (ClassManager.activeClass.get(damager.getUniqueId()) != null) {
                if (damager.getItemInHand() != null
                        && damager.getItemInHand().getType() == Material.GOLDEN_SWORD
                        && ClassManager.activeClass.get(damager.getUniqueId()) == ClassType.ROGUE) {

                    UUID uuid = damager.getUniqueId();
                    if (backstabCooldown.containsKey(uuid) && backstabCooldown.get(uuid) > System.currentTimeMillis()) {
                        long diff = backstabCooldown.get(damager.getUniqueId()) - System.currentTimeMillis();
                        if (diff < 0) diff = 0;
                        String formatted = String.format("%.1fs", diff / 1000.0);

                        damager.sendMessage(ChatColor.RED + "You can't backstab for " + ChatColor.BOLD + formatted + ChatColor.RED + "!");
                        return;
                    }

                    backstabCooldown.put(uuid, System.currentTimeMillis() + 15000L);

                    Vector playerVector = damager.getLocation().getDirection();
                    Vector entityVector = victim.getLocation().getDirection();

                    playerVector.setY(0F);
                    entityVector.setY(0F);

                    double degrees = playerVector.angle(entityVector);

                    if (Math.abs(degrees) < 1.4) {
                        damager.setItemInHand(new ItemStack(Material.AIR));

                        damager.playSound(damager.getLocation(), Sound.ENTITY_ITEM_BREAK, 1F, 1F);
                        damager.getWorld().playEffect(victim.getEyeLocation(), Effect.STEP_SOUND, Material.REDSTONE_BLOCK);

                        if (victim.getHealth() - 7D <= 0) {
                            event.setCancelled(true);
                        } else {
                            event.setDamage(0D);
                        }

                        victim.setHealth(Math.max(0D, victim.getHealth() - 7D));

                        damager.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 2 * 20, 2));
                    } else {
                        damager.sendMessage(ChatColor.RED + "Backstab failed!");
                    }
                }
            }
        }
    }
}