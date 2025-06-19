package net.minebo.smp.shop.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import co.aikar.commands.bukkit.contexts.OnlinePlayer;
import net.minebo.smp.shop.ShopManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

@CommandAlias("buy")
public class BuyCommand extends BaseCommand {

    @Default
    @Syntax("<item>")
    @CommandCompletion("@materials")
    public void onBuyCommand(Player player, String id, @Optional Integer amount) {
        ShopManager.buyItemByName(player, id, (amount != null) ? amount : 1);
    }
}
