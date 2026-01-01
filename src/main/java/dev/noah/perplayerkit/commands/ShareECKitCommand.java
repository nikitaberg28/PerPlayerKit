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

import com.google.common.primitives.Ints;          // Утилита для работы с примитивами, в т.ч. парсинг строк в числа
import dev.noah.perplayerkit.KitShareManager;    // Менеджер для работы с обменом и копированием китов/эндер-сундуков
import dev.noah.perplayerkit.util.CooldownManager; // Утилита для управления кулдаунами
import org.bukkit.ChatColor;                      // Для цветного текста в сообщениях
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;        // Интерфейс для обработки команд
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import dev.noah.perplayerkit.util.SoundManager;  // Утилита для воспроизведения звуков
import org.jetbrains.annotations.NotNull;

// Команда для создания кода обмена эндер-сундуком (Share EC Kit)
public class ShareECKitCommand implements CommandExecutor {

    // Менеджер кулдауна для команды обмена эндер-сундуком
    private final CooldownManager shareECCommandCooldown;

    public ShareECKitCommand() {
        // Устанавливаем кулдаун в 5 секунд
        this.shareECCommandCooldown = new CooldownManager(5);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        // Проверяем, является ли отправитель команды игроком
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Только игроки могут использовать эту команду");
            return true;
        }

        // Проверяем, передан ли номер слота (аргумент)
        if (args.length < 1) {
            // Если аргумент не передан, отправляем сообщение об ошибке
            player.sendMessage(ChatColor.RED + "Ошибка, вы должны выбрать слот эндер-сундука для обмена");
            SoundManager.playFailure(player); // Проигрываем звук неудачи
            return true;
        }

        // Проверяем, на кулдауне ли команда
        if (shareECCommandCooldown.isOnCooldown(player)) {
            player.sendMessage(ChatColor.RED + "Пожалуйста, не спамьте командой (кулдаун 5 секунд)");
            SoundManager.playFailure(player); // Проигрываем звук неудачи
            return true;
        }

        // Пробуем распарсить аргумент в число (номер слота)
        Integer slot = Ints.tryParse(args[0]);

        // Проверяем, является ли аргумент числом и входит ли он в допустимый диапазон (1-9)
        if (slot == null || slot < 1 || slot > 9) {
            player.sendMessage(ChatColor.RED + "Выберите допустимый слот эндер-сундука (1-9)");
            SoundManager.playFailure(player); // Проигрываем звук неудачи
            return true;
        }

        // Вызываем менеджер для создания кода обмена эндер-сундуком
        KitShareManager.get().shareEC(player, slot);

        // Устанавливаем кулдаун на команду
        shareECCommandCooldown.setCooldown(player);

        return true;
    }
}