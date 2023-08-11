package com.ghostchu.quickshop.command.subcommand;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.command.CommandHandler;
import com.ghostchu.quickshop.api.command.CommandParser;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.common.util.CommonUtil;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

public class SubCommand_TaxAccount implements CommandHandler<Player> {

    private final QuickShop plugin;

    public SubCommand_TaxAccount(QuickShop plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onCommand(@NotNull Player sender, @NotNull String commandLabel, @NotNull CommandParser parser) {
        final Shop shop = getLookingShop(sender);
        if (shop != null) {
            if (parser.getArgs().isEmpty()) {
                shop.setTaxAccount(null);
                plugin.text().of(sender, "taxaccount-unset").send();
                return;
            }
            if (CommonUtil.isUUID(parser.getArgs().get(0))) {
                shop.setTaxAccount(UUID.fromString(parser.getArgs().get(0)));
            } else {
                plugin.getPlayerFinder().name2UuidFuture(parser.getArgs().get(0)).whenComplete((uuid, throwable) -> {
                    if (throwable != null) {
                        plugin.text().of(sender, "internal-error", throwable.getMessage()).send();
                        plugin.logger().warn("Failed to get uuid of player " + parser.getArgs().get(0), throwable);
                        return;
                    }
                    if (uuid == null) {
                        plugin.text().of(sender, "unknown-player").send();
                        return;
                    }
                    shop.setTaxAccount(uuid);
                    plugin.text().of(sender, "taxaccount-set", parser.getArgs().get(0)).send();
                });
            }

        } else {
            plugin.text().of(sender, "not-looking-at-shop").send();
        }
    }

    @NotNull
    @Override
    public List<String> onTabComplete(
            @NotNull Player sender, @NotNull String commandLabel, @NotNull CommandParser parser) {
        return null;
    }

}
