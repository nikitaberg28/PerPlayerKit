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

import dev.noah.perplayerkit.KitRoomDataManager; // Менеджер данных "комнаты китов"
import org.bukkit.ChatColor;                      // Для цветного текста в сообщениях
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;        // Интерфейс для обработки команд
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;           // Интерфейс для автодополнения команд
import dev.noah.perplayerkit.util.SoundManager; // Утилита для воспроизведения звуков
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

// Команда для загрузки/сохранения "комнаты китов" в/из базы данных
public class KitRoomCommand implements CommandExecutor, TabCompleter {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        // Проверяем, передан ли один аргумент (load или save)
        if (args.length == 1) {
            // Если аргумент "load"
            if (args[0].equalsIgnoreCase("load")) {
                // Загружаем данные "комнаты китов" из базы данных
                KitRoomDataManager.get().loadFromDB();
                // Отправляем сообщение об успешной загрузке
                sender.sendMessage(ChatColor.GREEN + "Комната китов загружена из SQL");
                // Если отправитель - игрок, проигрываем звук успеха
                if (sender instanceof Player p) SoundManager.playSuccess(p);
            }
            // Если аргумент "save"
            else if (args[0].equalsIgnoreCase("save")) {
                // Асинхронно сохраняем данные "комнаты китов" в базу данных
                KitRoomDataManager.get().saveToDBAsync();
                // Отправляем сообщение об успешном сохранении
                sender.sendMessage(ChatColor.GREEN + "Комната китов сохранена в SQL");
                // Если отправитель - игрок, проигрываем звук успеха
                if (sender instanceof Player p) SoundManager.playSuccess(p);
            }
            // Если аргумент не "load" и не "save"
            else {
                // Отправляем сообщение об ошибке
                sender.sendMessage(ChatColor.RED + "Неправильное использование!");
                sender.sendMessage("/kitroom <load/save>");
                // Если отправитель - игрок, проигрываем звук неудачи
                if (sender instanceof Player p) SoundManager.playFailure(p);
            }
        }
        // Если количество аргументов не равно 1
        else {
            // Отправляем сообщение об ошибке
            sender.sendMessage(ChatColor.RED + "Неправильное использование!");
            sender.sendMessage("/kitroom <load/save>");
            // Если отправитель - игрок, проигрываем звук неудачи
            if (sender instanceof Player p) SoundManager.playFailure(p);
        }

        return true;
    }

    // Метод для автодополнения команды (TabCompleter)
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        // Если введён только один аргумент, предлагаем варианты "save" и "load"
        if (args.length == 1) {
            List<String> list = new ArrayList<>();
            list.add("save");  // Сохранить
            list.add("load");  // Загрузить
            return list;
        }
        // В остальных случаях возвращаем null (автодополнение не предоставляется)
        return null;
    }
}