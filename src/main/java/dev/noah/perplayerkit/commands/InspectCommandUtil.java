/*
 * Copyright 2022-2025 Noah Ross
 *
 * Этот файл является частью PerPlayerKit.
 *
 * PerPlayerKit — свободное программное обеспечение: вы можете распространять
 * и/или изменять его в соответствии с условиями GNU Affero General Public License,
 * опубликованной Free Software Foundation, либо версии 3 Лицензии, либо (по вашему
 * выбору) любой более поздней версии.
 *
 * PerPlayerKit распространяется в надежде, что он будет полезен, но БЕЗ КАКОЙ-ЛИБО
 * ГАРАНТИИ; даже без подразумеваемой гарантии ТОВАРНОГО ВИДА или ПРИГОДНОСТИ ДЛЯ
 * ОПРЕДЕЛЕННОЙ ЦЕЛИ. Для получения дополнительных сведений см. GNU Affero General Public License.
 *
 * Вы должны были получить копию GNU Affero General Public License вместе с PerPlayerKit.
 * Если это не так, см. <https://www.gnu.org/licenses/>.
 */
package dev.noah.perplayerkit.commands;

import dev.noah.perplayerkit.util.BroadcastManager; // Утилита для отправки сообщений
import net.kyori.adventure.text.Component;          // Представление компонента текста (для сообщений)
import net.kyori.adventure.text.minimessage.MiniMessage; // Утилита для парсинга текста в формате MiniMessage
import org.bukkit.Bukkit;                            // Основной класс API Bukkit
import org.bukkit.OfflinePlayer;                     // Представление оффлайн-игрока
import org.bukkit.entity.Player;                     // Представление онлайн-игрока
import org.jetbrains.annotations.NotNull;            // Аннотация, указывающая, что значение не может быть null
import org.jetbrains.annotations.Nullable;           // Аннотация, указывающая, что значение может быть null

import java.util.UUID;                              // Универсальный идентификатор
import java.util.concurrent.CompletableFuture;      // Утилита для асинхронного выполнения

// Вспомогательный класс для команды просмотра (inspect)
public class InspectCommandUtil {
    // Минимальный и максимальный номер слота для китов
    public static final int MIN_SLOT = 1;
    public static final int MAX_SLOT = 9;

    // Утилита для работы с форматом MiniMessage
    public static final MiniMessage mm = MiniMessage.miniMessage();

    // Префикс ошибки в сообщениях
    public static final Component ERROR_PREFIX = mm.deserialize("<red>Ошибка:</red> ");

    // Приватный конструктор, чтобы предотвратить создание экземпляров этого класса
    private InspectCommandUtil() {
        // Это вспомогательный класс
    }

    /**
     * Пытается асинхронно сопоставить идентификатор игрока (имя или UUID) с UUID.
     * Сначала метод пробует распарсить строку как UUID, затем проверяет онлайн-игроков синхронно,
     * и, наконец, ищет оффлайн-игроков асинхронно.
     *
     * @param identifier Имя игрока или строка UUID
     * @return CompletableFuture, содержащий UUID, если найден, иначе null
     */
    public static CompletableFuture<UUID> resolvePlayerIdentifierAsync(String identifier) {
        // Сначала пробуем распарсить как UUID
        try {
            UUID uuid = UUID.fromString(identifier);
            return CompletableFuture.completedFuture(uuid); // Если успешно, возвращаем завершённый результат
        } catch (IllegalArgumentException ignored) {
            // Если строка не является UUID, продолжаем
        }

        // Пробуем найти онлайн-игрока (быстрая и безопасная синхронная операция)
        Player onlinePlayer = Bukkit.getPlayerExact(identifier); // getPlayerExact ищет точное совпадение имени
        if (onlinePlayer != null) {
            return CompletableFuture.completedFuture(onlinePlayer.getUniqueId());
        }

        // Ищем оффлайн-игроков асинхронно (это может быть медленно)
        return CompletableFuture.supplyAsync(() -> {
            for (OfflinePlayer offlinePlayer : Bukkit.getOfflinePlayers()) {
                if (identifier.equalsIgnoreCase(offlinePlayer.getName())) {
                    return offlinePlayer.getUniqueId();
                }
            }
            return null; // Если не найден
        });
    }

    /**
     * Получает имя игрока по его UUID, возвращая строку UUID, если имя недоступно.
     *
     * @param uuid UUID игрока
     * @return Имя игрока или строка UUID
     */
    public static String getPlayerName(@NotNull UUID uuid) {
        // Проверяем, онлайн ли игрок
        Player onlinePlayer = Bukkit.getPlayer(uuid);
        if (onlinePlayer != null) {
            return onlinePlayer.getName();
        }

        // Если оффлайн, получаем имя из кэша
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
        String name = offlinePlayer.getName();
        // Если имя неизвестно, возвращаем строку UUID
        return name != null ? name : uuid.toString();
    }

    /**
     * Показывает сообщение об использовании команды игроку.
     *
     * @param player      Игрок, которому отправить сообщение
     * @param commandName Название команды (например, "inspectkit" или "inspectec")
     */
    public static void showUsage(@NotNull Player player, @NotNull String commandName) {
        // Отправляем игроку сообщение о правильном использовании команды с префиксом ошибки
        BroadcastManager.get().sendComponentMessage(player,
                ERROR_PREFIX.append(
                        mm.deserialize("<red>Использование: /" + commandName + " <игрок|uuid> <слот></red>")));
    }
}