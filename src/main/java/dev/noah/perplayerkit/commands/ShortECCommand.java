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

import dev.noah.perplayerkit.util.DisabledCommand; // Утилита для проверки, разрешена ли команда в мире
import dev.noah.perplayerkit.KitManager;           // Менеджер китов и эндер-сундуков
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;          // Интерфейс для обработки команд
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID; // Универсальный идентификатор игрока

// Команда для быстрой загрузки эндер-сундука по номеру, введённому в виде алиаса команды (например, /ec3 или /enderchest5)
public class ShortECCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        // Проверяем, является ли отправитель команды игроком
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Только игроки могут использовать эту команду.");
            return true;
        }

        // Проверяем, заблокирована ли команда в мире, где находится игрок
        if (DisabledCommand.isBlockedInWorld(player)) {
            return true; // Если команда заблокирована, просто завершаем обработку
        }

        UUID uuid = player.getUniqueId(); // Получаем уникальный идентификатор игрока

        // Проверяем, соответствует ли метка команды шаблону ec[1-9] (например, ec1, ec2, ..., ec9)
        if (label.matches("ec[1-9]")) {
            // Извлекаем номер эндер-сундука из метки команды (например, из "ec3" получаем "3")
            int ecNumber = Integer.parseInt(label.substring(2));
            // Загружаем эндер-сундук с указанным номером для игрока
            KitManager.get().loadEnderchest(player, ecNumber);
        }
        // Проверяем, соответствует ли метка команды шаблону enderchest[1-9] (например, enderchest1, ..., enderchest9)
        else if (label.matches("enderchest[1-9]")) {
            // Извлекаем номер эндер-сундука из метки команды (например, из "enderchest5" получаем "5")
            int ecNumber = Integer.parseInt(label.substring(10));
            // Загружаем эндер-сундук с указанным номером для игрока
            KitManager.get().loadEnderchest(player, ecNumber);
        }
        // Если метка команды не соответствует ни одному из допустимых шаблонов
        else {
            player.sendMessage("Неверная метка команды.");
        }

        return true;
    }
}