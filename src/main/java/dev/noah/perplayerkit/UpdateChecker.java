/*
 * Copyright 2022-2025 Noah Ross
 *
 * Этот файл является частью PerPlayerKit.
 *
 * PerPlayerKit - свободное программное обеспечение: вы можете распространять и/или изменять его
 * в соответствии с условиями лицензии GNU Affero General Public License, опубликованной
 * Free Software Foundation, либо версии 3 Лицензии, либо (по вашему
 * выбору) любой более поздней версии.
 *
 * PerPlayerKit распространяется в надежде, что он будет полезен, но БЕЗ КАКОЙ-ЛИБО
 * ГАРАНТИИ; даже без подразумеваемой гарантии ТОВАРНОГО ВИДА или ПРИГОДНОСТИ
 * ДЛЯ ОПРЕДЕЛЕННОЙ ЦЕЛИ. Подробнее см. в лицензии GNU Affero General Public License.
 *
 * Вы должны были получить копию лицензии GNU Affero General Public License
 * вместе с PerPlayerKit. Если нет, см. <https://www.gnu.org/licenses/>.
 */
package dev.noah.perplayerkit;

import com.squareup.okhttp.Call;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.io.IOException;

public class UpdateChecker { // Проверка обновлений

    private Plugin plugin;
    private String url = "https://hangar.papermc.io/api/v1/projects/PerPlayerKit/latestrelease  "; // URL для получения последней версии
    private String spigotDownloadUrl = "https://www.spigotmc.org/resources/perplayerkit.121437/  "; // URL для скачивания с Spigot
    private String modrinthDownloadUrl = "https://modrinth.com/plugin/perplayerkit  "; // URL для скачивания с Modrinth
    private String hangarDownloadUrl = "https://hangar.papermc.io/noah32/PerPlayerKit  "; // URL для скачивания с Hangar

    private Boolean updateAvailableCache = null; // Кэш для статуса наличия обновления
    private String latestVersionCache = null; // Кэш для последней версии

    public UpdateChecker(Plugin plugin) {
        this.plugin = plugin;
    }

    private String getCurrentVersion() { // Получить текущую версию плагина
        return plugin.getDescription().getVersion();
    }

    private String getLatestVersion() { // Получить последнюю версию
        if (latestVersionCache != null) {
            return latestVersionCache; // Вернуть кэшированную последнюю версию
        }

        try {
            OkHttpClient client = new OkHttpClient(); // Создание HTTP-клиента
            Request request = new Request.Builder().url(url).build(); // Создание запроса
            Call call = client.newCall(request); // Создание вызова
            Response response = call.execute(); // Выполнение запроса

            if (response.isSuccessful() && response.body() != null) { // Если запрос успешен и тело ответа не null
                String responseBody = response.body().string(); // Получение тела ответа как строки
                if (!responseBody.isEmpty() && responseBody.matches("\\d+(\\.\\d+)*")) { // Проверка формата версии
                    latestVersionCache = responseBody; // Кэшировать последнюю версию
                    return latestVersionCache;
                } else { // Если формат версии неверный
                    plugin.getLogger().warning("Получен неверный формат версии с сервера обновлений: " + responseBody); // Логировать ошибку
                }
            } else { // Если запрос неуспешен
                plugin.getLogger().warning("Не удалось получить последнюю версию. HTTP Status: " + response.code()); // Логировать ошибку
            }
        } catch (IOException e) { // Обработка ошибки ввода-вывода
            plugin.getLogger().warning("Произошла ошибка IOException при получении последней версии: " + e.getMessage()); // Логировать ошибку
        }

        plugin.getLogger().warning("Используется резервная версия: 1.0.0"); // Логировать использование резервной версии
        latestVersionCache = "1.0.0"; // Кэшировать резервную версию
        return latestVersionCache;
    }

    public boolean checkForUpdate() { // Проверить наличие обновления
        if (updateAvailableCache != null) {
            return updateAvailableCache; // Вернуть кэшированный результат, если уже проверяли
        }

        String currentVersion = getCurrentVersion(); // Получить текущую версию
        String latestVersion = getLatestVersion(); // Получить последнюю версию

        updateAvailableCache = isSemanticallyNewer(currentVersion, latestVersion); // Проверить, новее ли последняя версия
        return updateAvailableCache;
    }

    public void printStartupStatus() { // Вывести статус обновления при запуске
        if (checkForUpdate()) { // Если доступно обновление
            String currentVersion = getCurrentVersion(); // Получить текущую версию
            String latestVersion = getLatestVersion(); // Получить последнюю версию

            plugin.getLogger().info("Доступна новая версия PerPlayerKit! Вы используете версию " + currentVersion + ", а последняя версия - " + latestVersion); // Логировать информацию об обновлении
            plugin.getLogger().info("Скачайте последнюю версию на:"); // Логировать
            plugin.getLogger().info("Spigot: " + spigotDownloadUrl); // Логировать URL
            plugin.getLogger().info("Modrinth: " + modrinthDownloadUrl); // Логировать URL
            plugin.getLogger().info("PaperMC: " + hangarDownloadUrl); // Логировать URL
        } else { // Если обновление не доступно
            plugin.getLogger().info("Вы используете последнюю версию PerPlayerKit"); // Логировать
        }
    }

    public void sendUpdateMessage(Player player) { // Отправить сообщение об обновлении игроку
        if (checkForUpdate()) { // Если доступно обновление
            String currentVersion = getCurrentVersion(); // Получить текущую версию
            String latestVersion = getLatestVersion(); // Получить последнюю версию

            player.sendMessage("Доступна новая версия PerPlayerKit! Вы используете версию " + currentVersion + ", а последняя версия - " + latestVersion); // Отправить сообщение игроку
        }
    }

    private boolean isSemanticallyNewer(String currentVersion, String newVersion) { // Сравнить версии (семантически)
        String[] currentVersionSplit = currentVersion.split("\\."); // Разделить текущую версию по точке
        String[] newVersionSplit = newVersion.split("\\."); // Разделить новую версию по точке

        for (int i = 0; i < Math.min(currentVersionSplit.length, newVersionSplit.length); i++) { // Цикл по частям версии
            int currentNumber = Integer.parseInt(currentVersionSplit[i]); // Преобразовать часть текущей версии в число
            int newNumber = Integer.parseInt(newVersionSplit[i]); // Преобразовать часть новой версии в число

            if (currentNumber < newNumber) { // Если текущая часть меньше новой
                return true; // Новая версия новее
            } else if (currentNumber > newNumber) { // Если текущая часть больше новой
                return false; // Новая версия не новее
            }
            // Цикл продолжается, если части равны
        }

        return newVersionSplit.length > currentVersionSplit.length; // Новая версия новее, если она длиннее (например, 1.0.0 vs 1.0)
    }
}