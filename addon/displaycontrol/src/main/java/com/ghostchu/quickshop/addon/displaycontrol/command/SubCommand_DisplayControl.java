package com.ghostchu.quickshop.addon.displaycontrol.command;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.addon.displaycontrol.Main;
import com.ghostchu.quickshop.api.command.CommandHandler;
import com.ghostchu.quickshop.api.command.CommandParser;
import com.ghostchu.quickshop.util.Util;
import com.ghostchu.quickshop.util.logger.Log;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

public class SubCommand_DisplayControl implements CommandHandler<Player> {
    private final QuickShop qs;
    private final Main plugin;

    public SubCommand_DisplayControl(QuickShop qs, Main plugin) {
        this.qs = qs;
        this.plugin = plugin;
    }

    @Override
    public void onCommand(Player sender, @NotNull String commandLabel, @NotNull CommandParser parser) {
        if (parser.getArgs().size() < 1) {
            qs.text().of(sender, "command-incorrect", "/qs displaycontrol <enable/disable>").send();
            return;
        }
        boolean ops = parser.getArgs().get(0).equalsIgnoreCase("enable");
        Util.asyncThreadRun(() -> {
            try {
                Integer i = plugin.getDatabaseHelper().setDisplayStatusForPlayer(sender.getUniqueId(), ops);
                Log.debug("Execute DisplayToggle with id " + i + " affected");
                qs.text().of(sender, "addon.displaycontrol.toggle", !ops).send();
            } catch (SQLException e) {
                qs.text().of(sender, "addon.displaycontrol.toggle-exception").send();
                plugin.getLogger().log(Level.WARNING, "Cannot save the player display status", e);
            }
        });
    }


    @Override
    public @Nullable List<String> onTabComplete(@NotNull Player sender, @NotNull String commandLabel, @NotNull CommandParser parser) {
        if (parser.getArgs().size() == 1) {
            return List.of("enable", "disable");
        }
        return Collections.emptyList();
    }
}