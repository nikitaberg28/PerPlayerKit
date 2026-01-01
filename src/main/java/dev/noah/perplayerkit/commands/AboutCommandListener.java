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

import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

// Класс, отвечающий за обработку команды /aboutperplayerkit
public class AboutCommandListener implements Listener {

    // Объект для хранения свойств сборки (например, версия, время сборки)
    private final Properties buildProperties = new Properties();

    // Конструктор класса, загружает свойства сборки из файла build.properties
    public AboutCommandListener() {
        try (InputStream input = getClass().getResourceAsStream("/build.properties")) {
            if (input != null) {
                buildProperties.load(input);
            }
        } catch (IOException ex) {
            // Выводим стек вызовов в случае ошибки чтения файла
            ex.printStackTrace();
        }
    }

    // Метод, отправляющий игроку сообщение с информацией о плагине
    private void sendAboutMessage(CommandSender sender) {
        String author = "Noah Ross, NikitaBerg"; // Автор плагина
        String source = "https://github.com/rossnoah/PerPlayerKit"; // Ссылка на исходный код
        String license = "https://nikitaberg.ru"; // Сайт автора перевода

        // Получаем время сборки и версию плагина из свойств. Если не найдены — "Unknown"
        String buildTimestamp = buildProperties.getProperty("build.timestamp", "Unknown");
        String pluginVersion = buildProperties.getProperty("plugin.version", "Unknown");

        // Отправляем игроку сообщение с информацией о плагине
        sender.sendMessage("==========[О плагине]==========");
        sender.sendMessage("PerPlayerKit");
        sender.sendMessage("Автор: " + author);
        sender.sendMessage("Сайт автора перевода: " + license);
        sender.sendMessage("Исходный код: " + source);
        sender.sendMessage("Версия: " + pluginVersion);
        sender.sendMessage("Время сборки: " + buildTimestamp);
        sender.sendMessage("===============================");
    }

    // Обработчик события ввода команды игроком
    @EventHandler
    public void onPreCommand(PlayerCommandPreprocessEvent event) {
        String message = event.getMessage(); // Получаем введённую команду
        if (message.equalsIgnoreCase("/aboutperplayerkit")) { // Проверяем, совпадает ли команда с /aboutperplayerkit
            event.setCancelled(true); // Отменяем выполнение команды, чтобы не было сообщения "неизвестная команда"
            CommandSender sender = event.getPlayer(); // Получаем игрока, введшего команду
            sendAboutMessage(sender); // Отправляем ему сообщение с информацией о плагине
        }
    }
}