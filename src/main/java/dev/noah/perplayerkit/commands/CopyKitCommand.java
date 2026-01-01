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
import dev.noah.perplayerkit.KitShareManager;      // Менеджер для работы с обменом и копированием китов
import org.bukkit.ChatColor;                        // Для цветного текста в сообщениях
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import dev.noah.perplayerkit.util.SoundManager;    // Утилита для воспроизведения звуков
import org.jetbrains.annotations.NotNull;

// Команда для копирования кита по коду
public class CopyKitCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        // Проверяем, является ли отправитель команды игроком
        if (sender instanceof Player player) {

            // Проверяем, заблокирована ли команда в мире, где находится игрок
            if (DisabledCommand.isBlockedInWorld(player)) {
                return true; // Если команда заблокирована, просто завершаем обработку
            }

            // Проверяем, были ли переданы аргументы команды (ожидаем код кита)
            if (args.length > 0) {
                // Используем менеджер для копирования кита по указанному коду
                KitShareManager.get().copyKit(player, args[0]);
            } else {
                // Если аргумент не передан, отправляем игроку сообщение об ошибке
                player.sendMessage(ChatColor.RED + "Ошибка, вы должны ввести код кита для копирования");
                // Проигрываем звук неудачи
                SoundManager.playFailure(player);
            }
        } else {
            // Если команда была выполнена не игроком (например, консолью), отправляем сообщение
            sender.sendMessage("Только игроки могут использовать эту команду");
        }

        return true;
    }
}