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

import dev.noah.perplayerkit.util.BroadcastManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class InspectCommandUtil {
    public static final int MIN_SLOT = 1;
    public static final int MAX_SLOT = 9;
    public static final MiniMessage mm = MiniMessage.miniMessage();
    public static final Component ERROR_PREFIX = mm.deserialize("<red>Ошибка:</red> ");

    private InspectCommandUtil() {
        // Утилитный класс
    }

    /**
     * Пытается асинхронно преобразовать идентификатор игрока (имя или UUID) в UUID.
     * Этот метод сначала пробует распарсить UUID, затем проверяет онлайн-игроков синхронно,
     * и, наконец, ищет среди офлайн-игроков асинхронно.
     *
     * @param identifier Имя игрока или строка UUID
     * @return CompletableFuture, содержащий UUID, если найден, иначе null
     */
    public static CompletableFuture<UUID> resolvePlayerIdentifierAsync(String identifier) {
        // Сначала пробуем распарсить как UUID
        try {
            UUID uuid = UUID.fromString(identifier);
            return CompletableFuture.completedFuture(uuid);
        } catch (IllegalArgumentException ignored) {
            // Не UUID, продолжаем
        }

        // Пытаемся найти игрока онлайн (это быстро и безопасно делать синхронно)
        Player onlinePlayer = Bukkit.getPlayerExact(identifier);
        if (onlinePlayer != null) {
            return CompletableFuture.completedFuture(onlinePlayer.getUniqueId());
        }

        // Ищем офлайн-игроков асинхронно (это может быть медленно)
        return CompletableFuture.supplyAsync(() -> {
            for (OfflinePlayer offlinePlayer : Bukkit.getOfflinePlayers()) {
                if (identifier.equalsIgnoreCase(offlinePlayer.getName())) {
                    return offlinePlayer.getUniqueId();
                }
            }
            return null;
        });
    }

    /**
     * Получает имя игрока по его UUID, возвращая строку UUID, если имя недоступно.
     *
     * @param uuid UUID игрока
     * @return Имя игрока или строка UUID
     */
    public static String getPlayerName(@NotNull UUID uuid) {
        Player onlinePlayer = Bukkit.getPlayer(uuid);
        if (onlinePlayer != null) {
            return onlinePlayer.getName();
        }

        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
        String name = offlinePlayer.getName();
        return name != null ? name : uuid.toString();
    }

    /**
     * Показывает сообщение об использовании команды игроку.
     *
     * @param player Игрок, которому нужно отправить сообщение
     * @param commandName Название команды (например, "inspectkit" или "inspectec")
     */
    public static void showUsage(@NotNull Player player, @NotNull String commandName) {
        BroadcastManager.get().sendComponentMessage(player,
                ERROR_PREFIX.append(
                        mm.deserialize("<red>Использование: /" + commandName + " <игрок|uuid> <слот></red>")));
    }
}