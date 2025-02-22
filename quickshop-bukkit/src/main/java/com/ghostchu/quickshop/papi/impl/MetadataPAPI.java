package com.ghostchu.quickshop.papi.impl;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.papi.PAPISubHandler;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class MetadataPAPI implements PAPISubHandler {
    private final QuickShop plugin;

    public MetadataPAPI(@NotNull QuickShop plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getPrefix() {
        return "metadata";
    }

    @Override
    @Nullable
    public String handle0(@NotNull OfflinePlayer player, @NotNull String paramsTrimmed) {
        String[] args = paramsTrimmed.split("_");
        if (args.length < 1) {
            return null;
        }
        return switch (args[0]) {
            case "fork" -> plugin.getFork();
            case "version" -> plugin.getVersion();
            default -> null;
        };
    }

}
