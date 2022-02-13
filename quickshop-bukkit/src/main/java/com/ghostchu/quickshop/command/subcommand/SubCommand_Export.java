/*
 * This file is a part of project QuickShop, the name is SubCommand_Export.java
 *  Copyright (C) PotatoCraft Studio and contributors
 *
 *  This program is free software: you can redistribute it and/or modify it
 *  under the terms of the GNU General Public License as published by the
 *  Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT
 *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *  FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *  for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.ghostchu.quickshop.command.subcommand;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.ConsoleCommandSender;
import org.jetbrains.annotations.NotNull;
import com.ghostchu.quickshop.api.command.CommandHandler;
import com.ghostchu.quickshop.util.MsgUtil;
import com.ghostchu.quickshop.util.Util;

@AllArgsConstructor
public class SubCommand_Export implements CommandHandler<ConsoleCommandSender> {

    @Override
    @SneakyThrows
    public synchronized void onCommand(@NotNull ConsoleCommandSender sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {
        Util.makeExportBackup(null);
        MsgUtil.sendDirectMessage(sender, Component.text("Done.").color(NamedTextColor.GREEN));
    }


}