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

import dev.noah.perplayerkit.util.importutil.KitsXImporter; // Утилита для импорта данных из плагина KitsX
import org.bukkit.ChatColor;                                // Для цветного текста в сообщениях
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;                   // Интерфейс для обработки команд
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;                     // Интерфейс для автодополнения команд
import org.bukkit.plugin.Plugin;                            // Представление плагина
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

// Команда для выполнения вспомогательных действий плагина, таких как импорт или информация
public class PerPlayerKitCommand implements CommandExecutor, TabCompleter {

    private Plugin plugin; // Ссылка на экземпляр плагина

    public PerPlayerKitCommand(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        // Если не передано подкоманд (аргументов)
        if (args.length == 0) {
            // Отправляем сообщение об ошибке
            sender.sendMessage(ChatColor.RED + "Отсутствуют аргументы!");
            return true;
        }

        // Обработка подкоманд с помощью switch
        switch (args[0].toLowerCase()) {
            case "about": // Подкоманда "about" - показывает краткую информацию о плагине
                sender.sendMessage(ChatColor.GREEN + "PerPlayerKit — это плагин, позволяющий игрокам создавать свои собственные киты.");
                return true;
            case "import": // Подкоманда "import" - для импорта данных из других плагинов
                // Проверяем, передан ли тип импорта (например, "kitsx")
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Отсутствует тип импорта!");
                    return true;
                }

                // Обработка конкретного типа импорта
                switch (args[1].toLowerCase()) {
                    case "kitsx": // Импорт из плагина KitsX
                        sender.sendMessage(ChatColor.GREEN + "Начинается импорт...");
                        // Создаём экземпляр импортера
                        KitsXImporter importer = new KitsXImporter(plugin, sender);
                        // Проверяем, существуют ли файлы для импорта
                        if (!importer.checkForFiles()) {
                            // Если файлы отсутствуют, отправляем сообщение об ошибке
                            sender.sendMessage(ChatColor.RED + "Отсутствуют файлы для импорта");
                            sender.sendMessage(ChatColor.RED + "Скопируйте папку data из KitsX в папку PerPlayerKit");
                        }
                        // Запускаем процесс импорта
                        importer.importFiles();
                        // Сообщаем об окончании попытки импорта
                        sender.sendMessage(ChatColor.GREEN + "Попытка импорта данных из KitsX выполнена!");
                        break;
                    default: // Если указан неверный тип импорта
                        sender.sendMessage(ChatColor.RED + "Неверный тип импорта!");
                        break;
                }
                return true;
            default: // Если указана неизвестная подкоманда
                sender.sendMessage(ChatColor.RED + "Неверная подкоманда!");
                return true;
        }
    }

    // Метод для автодополнения команды (TabCompleter)
    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        // Если введена только первая подкоманда
        if (args.length == 1) {
            // Предлагаем варианты "about" и "import"
            return List.of("about", "import");
        }

        // Если введена подкоманда "import" и начинаем ввод второго аргумента
        if (args.length == 2 && args[0].equalsIgnoreCase("import")) {
            // Предлагаем вариант "kitsx"
            return List.of("kitsx");
        }

        // В остальных случаях автодополнение не предоставляется
        return null;
    }
}