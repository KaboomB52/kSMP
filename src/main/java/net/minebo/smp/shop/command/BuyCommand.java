package net.minebo.smp.shop.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Optional;
import co.aikar.commands.annotation.Syntax;
import co.aikar.commands.bukkit.contexts.OnlinePlayer;
import net.minebo.smp.shop.ShopManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

@CommandAlias("buy")
public class BuyCommand extends BaseCommand {

    @Default
    @Syntax("<item>")
    public void onBuyCommand(Player player, String id, @Optional Integer amount) {
        ShopManager.buyItemByName(player, id, (amount != null) ? amount : 1);
    }
}
