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

// Команда для быстрой загрузки кита по номеру, введённому в виде алиаса команды (например, /k3 или /kit5)
public class ShortKitCommand implements CommandExecutor {

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

        // Проверяем, соответствует ли метка команды шаблону k[1-9] (например, k1, k2, ..., k9)
        if (label.matches("k[1-9]")) {
            // Извлекаем номер кита из метки команды (например, из "k3" получаем "3")
            int kitNumber = Integer.parseInt(label.substring(1));
            // Загружаем кит с указанным номером для игрока
            KitManager.get().loadKit(player, kitNumber);
        }
        // Проверяем, соответствует ли метка команды шаблону kit[1-9] (например, kit1, ..., kit9)
        else if (label.matches("kit[1-9]")) {
            // Извлекаем номер кита из метки команды (например, из "kit5" получаем "5")
            int kitNumber = Integer.parseInt(label.substring(3));
            // Загружаем кит с указанным номером для игрока
            KitManager.get().loadKit(player, kitNumber);
        }
        // Если метка команды не соответствует ни одному из допустимых шаблонов
        else {
            player.sendMessage("Неверная метка команды.");
        }

        return true;
    }
}