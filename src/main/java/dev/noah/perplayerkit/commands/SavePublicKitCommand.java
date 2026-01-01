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
import dev.noah.perplayerkit.ItemFilter;           // Утилита для фильтрации предметов
import dev.noah.perplayerkit.KitManager;           // Менеджер китов
import org.bukkit.ChatColor;                        // Для цветного текста в сообщениях
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;          // Интерфейс для обработки команд
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;             // Интерфейс для автодополнения команд
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;               // Представление инвентаря
import org.bukkit.inventory.ItemStack;               // Представление предмета с количеством, метаданными и т.д.
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import dev.noah.perplayerkit.util.SoundManager;    // Утилита для воспроизведения звуков

import java.util.List;

// Команда для сохранения содержимого инвентаря игрока как публичного кита
public class SavePublicKitCommand implements CommandExecutor, TabCompleter {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        // Проверяем, является ли отправитель команды игроком
        if (!(sender instanceof Player p)) {
            sender.sendMessage("Только игроки могут использовать эту команду");
            return true;
        }

        // Проверяем, заблокирована ли команда в мире, где находится игрок
        if (DisabledCommand.isBlockedInWorld(p)) {
            return true; // Если команда заблокирована, просто завершаем обработку
        }

        // Проверяем, передано ли имя кита (аргумент)
        if (args.length < 1) {
            p.sendMessage(ChatColor.RED + "Вам нужно указать ID кита");
            p.sendMessage(ChatColor.RED + "Использование: /" + label + " <id_кита>");
            return true;
        }

        String kidId = args[0]; // args[0] - это ID публичного кита

        // Проверяем, существует ли публичный кит с таким ID (указанный в конфиге)
        if (KitManager.get().getPublicKitList().stream().noneMatch(kit -> kit.id.equals(kidId))) {
            p.sendMessage(ChatColor.RED + "Публичный кит " + kidId + " не существует");
            p.sendMessage(ChatColor.RED + "Возможно, вам нужно добавить публичный кит в конфигурации");
            return true;
        }

        // Получаем инвентарь игрока
        Inventory inv = p.getInventory();

        // Массив для хранения предметов (41 слот: 36 слотов инвентаря + 4 слота брони + 1 слот вне инвентаря, например, слот для щита)
        ItemStack[] data = new ItemStack[41];
        // Копируем предметы из инвентаря в массив
        for (int i = 0; i < 41; i++) {
            ItemStack item = inv.getItem(i); // Получаем предмет из слота i
            if (item != null) {
                data[i] = item.clone(); // Клонируем предмет, чтобы избежать ссылок на оригинальные объекты
            }
        }

        // Фильтруем предметы через ItemFilter (например, ограничения на NBT, зачарования и т.д.)
        data = ItemFilter.get().filterItemStack(data);

        KitManager kitManager = KitManager.get();
        // Сохраняем публичный кит
        boolean success = kitManager.savePublicKit(kidId, data);
        if (success) {
            // Если успешно, сохраняем кит в базу данных
            kitManager.savePublicKitToDB(kidId);
            p.sendMessage("Кит " + kidId + " сохранён");
            SoundManager.playSuccess(p); // Проигрываем звук успеха
        } else {
            // Если произошла ошибка при сохранении
            p.sendMessage("Ошибка при сохранении кита " + kidId);
            SoundManager.playFailure(p); // Проигрываем звук неудачи
        }

        return true;
    }

    // Метод для автодополнения команды (TabCompleter)
    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        // Предлагаем автодополнение из списка доступных публичных китов (их ID)
        return KitManager.get().getPublicKitList().stream().map(kit -> kit.id).toList();
    }
}