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

import com.google.common.primitives.Ints; // Утилита для работы с примитивами, в т.ч. парсинг строк в числа
import dev.noah.perplayerkit.KitManager; // Менеджер китов
import org.bukkit.ChatColor;              // Для цветного текста в сообщениях
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import dev.noah.perplayerkit.util.SoundManager; // Утилита для воспроизведения звуков
import org.jetbrains.annotations.NotNull;

import java.util.UUID; // Универсальный идентификатор игрока

// Команда для удаления кита из слота
public class DeleteKitCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        // Проверяем, является ли отправитель команды игроком
        if (sender instanceof Player player) {
            // Получаем уникальный идентификатор игрока
            UUID uuid = player.getUniqueId();

            // Команда ожидает один аргумент - номер слота (например, /deletekit 3)
            if (args.length == 1) {
                // Пробуем распарсить аргумент в число (номер слота)
                Integer slot = Ints.tryParse(args[0]);
                KitManager kitManager = KitManager.get(); // Получаем экземпляр менеджера китов

                // Если аргумент не является числом
                if (slot == null) {
                    // Отправляем игроку сообщение об ошибке
                    player.sendMessage(ChatColor.RED + "Использование: /deletekit <слот>");
                    player.sendMessage(ChatColor.RED + "Выберите реальное число");
                    // Проигрываем звук неудачи
                    SoundManager.playFailure(player);
                    return true;
                }

                // Проверяем, существует ли кит в указанном слоте у этого игрока
                if (kitManager.hasKit(uuid, slot)) {
                    // Пробуем удалить кит
                    if (kitManager.deleteKit(uuid, slot)) {
                        // Если успешно, сообщаем об этом
                        player.sendMessage(ChatColor.GREEN + "Кит " + slot + " удалён!");
                        // Проигрываем звук успеха
                        SoundManager.playSuccess(player);
                    } else {
                        // Если удаление не удалось, сообщаем об этом
                        player.sendMessage(ChatColor.RED + "Ошибка удаления кита!");
                        // Проигрываем звук неудачи
                        SoundManager.playFailure(player);
                    }
                } else {
                    // Если кита в слоте не существует
                    player.sendMessage(ChatColor.RED + "Кита в слоте " + slot + " не существует!");
                    // Проигрываем звук неудачи
                    SoundManager.playFailure(player);
                }
            } else {
                // Если аргумент не передан или их больше одного
                player.sendMessage(ChatColor.RED + "Использование: /deletekit <слот>");
                // Проигрываем звук неудачи
                SoundManager.playFailure(player);
            }
        } else {
            // Если команда была выполнена не игроком (например, консолью)
            sender.sendMessage(ChatColor.RED + "Только игроки могут использовать эту команду!");
            // Если отправитель - игрок (хотя код сюда не должен попасть), проигрываем звук неудачи
            if (sender instanceof Player s) SoundManager.playFailure(s);
        }

        return true;
    }
}