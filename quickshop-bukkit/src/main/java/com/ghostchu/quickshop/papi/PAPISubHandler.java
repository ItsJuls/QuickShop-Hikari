package com.ghostchu.quickshop.papi;

import org.apache.commons.lang3.StringUtils;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public interface PAPISubHandler {
    @Nullable
    default String handle(@NotNull OfflinePlayer player, @NotNull String params) {
        String raw = StringUtils.substringAfter(params, getPrefix() + "_");
        return handle0(player, raw);
    }

    @NotNull
    String getPrefix();

    @Nullable
    String handle0(@NotNull OfflinePlayer player, @NotNull String paramsTrimmed);
}
