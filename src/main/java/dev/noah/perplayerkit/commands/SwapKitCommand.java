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
import dev.noah.perplayerkit.KitManager;          // Менеджер китов и эндер-сундуков
import org.bukkit.ChatColor;                      // Для цветного текста в сообщениях
import dev.noah.perplayerkit.util.SoundManager;  // Утилита для воспроизведения звуков
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;        // Интерфейс для обработки команд
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;             // Представление предмета с количеством, метаданными и т.д.
import org.jetbrains.annotations.NotNull;

import java.util.UUID; // Универсальный идентификатор игрока

// Команда для обмена содержимого двух слотов китов местами (swap)
public class SwapKitCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        // Проверяем, является ли отправитель команды игроком
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Только игроки могут использовать эту команду!");
            return true;
        }

        // Проверяем, передано ли ровно 2 аргумента (номера слотов)
        if (args.length != 2) {
            player.sendMessage(ChatColor.RED + "Использование: /swapkit <слот1> <слот2>");
            SoundManager.playFailure(player); // Проигрываем звук неудачи
            return true;
        }

        // Пробуем распарсить аргументы в числа (номера слотов)
        Integer slot1 = Ints.tryParse(args[0]);
        Integer slot2 = Ints.tryParse(args[1]);

        // Проверяем, являются ли аргументы числами
        if (slot1 == null || slot2 == null) {
            player.sendMessage(ChatColor.RED + "Использование: /swapkit <слот1> <слот2>");
            player.sendMessage(ChatColor.RED + "Выберите реальные числа");
            SoundManager.playFailure(player); // Проигрываем звук неудачи
            return true;
        }

        KitManager kitManager = KitManager.get(); // Получаем экземпляр менеджера китов
        UUID uuid = player.getUniqueId(); // Получаем уникальный идентификатор игрока

        // Проверяем, существуют ли киты в указанных слотах
        if (!kitManager.hasKit(uuid, slot1)) {
            player.sendMessage(ChatColor.RED + "Кита в слоте " + slot1 + " не существует!");
            SoundManager.playFailure(player); // Проигрываем звук неудачи
            return true;
        }

        if (!kitManager.hasKit(uuid, slot2)) {
            player.sendMessage(ChatColor.RED + "Кита в слоте " + slot2 + " не существует!");
            SoundManager.playFailure(player); // Проигрываем звук неудачи
            return true;
        }

        // Обмениваем содержимое китов местами
        // Получаем содержимое первого кита и сохраняем во временный массив
        ItemStack[] tempkit = kitManager.getPlayerKit(uuid, slot1).clone();
        // Сохраняем содержимое второго кита в первый слот
        kitManager.savekit(uuid, slot1, kitManager.getPlayerKit(uuid, slot2), true);
        // Сохраняем содержимое временного массива (старого первого кита) во второй слот
        kitManager.savekit(uuid, slot2, tempkit.clone(), true);

        // Сохраняем оба обновлённых кита в базу данных
        kitManager.saveEnderchestToDB(uuid, slot1); // Метод, скорее всего, сохраняет кит, несмотря на название
        kitManager.saveEnderchestToDB(uuid, slot2);

        // Отправляем игроку сообщение об успешном обмене
        player.sendMessage(ChatColor.GREEN + "Киты в слотах " + slot1 + " и " + slot2 + " были обменяны местами!");
        SoundManager.playSuccess(player); // Проигрываем звук успеха

        return true;
    }
}