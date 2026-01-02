/*
 * Copyright 2022-2025 Noah Ross
 *
 * This file is part of PerPlayerKit.
 *
 * PerPlayerKit is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * PerPlayerKit is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for
 * more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with PerPlayerKit. If not, see <https://www.gnu.org/licenses/>.
 */
package dev.noah.perplayerkit.commands;

import com.google.common.primitives.Ints;
import dev.noah.perplayerkit.KitShareManager;
import dev.noah.perplayerkit.util.CooldownManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import dev.noah.perplayerkit.util.SoundManager;
import org.jetbrains.annotations.NotNull;

public class ShareECKitCommand implements CommandExecutor {

    private final CooldownManager shareECCommandCooldown;

    public ShareECKitCommand() {
        this.shareECCommandCooldown = new CooldownManager(5);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage("Только игроки могут использовать эту команду");
            return true;
        }

        if (args.length < 1) {
            player.sendMessage(ChatColor.RED + "Ошибка, вы должны выбрать слот эндер-сундука для отправки");
            SoundManager.playFailure(player);
            return true;
        }

        if (shareECCommandCooldown.isOnCooldown(player)) {
            player.sendMessage(ChatColor.RED + "Пожалуйста, не спамьте командой (кулдаун 5 секунд)");
            SoundManager.playFailure(player);
            return true;
        }

        Integer slot = Ints.tryParse(args[0]);

        if (slot == null || slot < 1 || slot > 9) {
            player.sendMessage(ChatColor.RED + "Выберите корректный слот кита");
            SoundManager.playFailure(player);
            return true;
        }

        KitShareManager.get().shareEC(player, slot);
        shareECCommandCooldown.setCooldown(player);

        return true;
    }
}