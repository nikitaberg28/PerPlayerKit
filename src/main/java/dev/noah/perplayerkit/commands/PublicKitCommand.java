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
import dev.noah.perplayerkit.KitManager;           // Менеджер китов
import dev.noah.perplayerkit.gui.GUI;              // Класс для работы с графическим интерфейсом
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;          // Интерфейс для обработки команд
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;             // Интерфейс для автодополнения команд
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;                    // Представление плагина
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

// Команда для открытия меню публичных китов или загрузки публичного кита по имени
public class PublicKitCommand implements CommandExecutor, TabCompleter {

    private Plugin plugin; // Ссылка на экземпляр плагина

    public PublicKitCommand(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        // Проверяем, является ли отправитель команды игроком
        if (!(sender instanceof Player player)) {
            // Если нет, отправляем сообщение
            sender.sendMessage("Только игроки могут использовать эту команду");
            return true;
        }

        // Проверяем, заблокирована ли команда в мире, где находится игрок
        if (DisabledCommand.isBlockedInWorld(player)) {
            return true; // Если команда заблокирована, просто завершаем обработку
        }

        // Если не передано аргументов, открываем меню публичных китов
        if (args.length < 1) {
            GUI kitMenu = new GUI(plugin);
            kitMenu.OpenPublicKitMenu(player); // Открываем GUI меню публичных китов
            return true;
        }

        // Если передан один аргумент (имя публичного кита), загружаем этот кит
        String kitName = args[0]; // args[0] - это имя публичного кита
        KitManager.get().loadPublicKit(player, kitName); // Метод загрузки публичного кита

        return true;
    }

    // Метод для автодополнения команды (TabCompleter)
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {

        // Если введён только один аргумент (имя кита), предлагаем доступные публичные киты
        if (args.length == 1) {
            List<String> list = new ArrayList<>();
            // Получаем список публичных китов и добавляем их идентификаторы в список автодополнения
            KitManager.get().getPublicKitList().forEach((kit) -> list.add(kit.id));

            return list;
        }

        // В остальных случаях автодополнение не предоставляется
        return null;
    }
}