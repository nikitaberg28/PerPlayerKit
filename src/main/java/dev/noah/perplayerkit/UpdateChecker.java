/*
 * Copyright 2022-2025 Noah Ross
 *
 * This file is part of PerPlayerKit.
 *
 * PerPlayerKit is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * PerPlayerKit is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for
 * more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with PerPlayerKit. If not, see <https://www.gnu.org/licenses/>.
 */
package dev.noah.perplayerkit;

import com.squareup.okhttp.Call;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.io.IOException;

public class UpdateChecker {

    private Plugin plugin;
    private String url = "https://hangar.papermc.io/api/v1/projects/PerPlayerKit/latestrelease";
    private String spigotDownloadUrl = "https://www.spigotmc.org/resources/perplayerkit.121437/";
    private String modrinthDownloadUrl = "https://modrinth.com/plugin/perplayerkit";
    private String hangarDownloadUrl = "https://hangar.papermc.io/noah32/PerPlayerKit";

    private Boolean updateAvailableCache = null;
    private String latestVersionCache = null;

    public UpdateChecker(Plugin plugin) {
        this.plugin = plugin;
    }

    private String getCurrentVersion() {
        return plugin.getDescription().getVersion();
    }

    private String getLatestVersion() {
        if (latestVersionCache != null) {
            return latestVersionCache; // Возврат кэшированной версии
        }

        try {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder().url(url).build();
            Call call = client.newCall(request);
            Response response = call.execute();

            if (response.isSuccessful() && response.body() != null) {
                String responseBody = response.body().string();
                if (!responseBody.isEmpty() && responseBody.matches("\\d+(\\.\\d+)*")) {
                    latestVersionCache = responseBody; // Кэширование полученной версии
                    return latestVersionCache;
                } else {
                    plugin.getLogger().warning("Получен неверный формат версии от сервера обновлений: " + responseBody);
                }
            } else {
                plugin.getLogger().warning("Не удалось получить последнюю версию. HTTP статус: " + response.code());
            }
        } catch (IOException e) {
            plugin.getLogger().warning("Ошибка ввода-вывода при получении последней версии: " + e.getMessage());
        }

        plugin.getLogger().warning("Используется запасная версия: 1.0.0");
        latestVersionCache = "1.0.0";
        return latestVersionCache;
    }

    public boolean checkForUpdate() {
        if (updateAvailableCache != null) {
            return updateAvailableCache;
        }

        String currentVersion = getCurrentVersion();
        String latestVersion = getLatestVersion();

        updateAvailableCache = isSemanticallyNewer(currentVersion, latestVersion);
        return updateAvailableCache;
    }

    public void printStartupStatus() {
        if (checkForUpdate()) {
            String currentVersion = getCurrentVersion();
            String latestVersion = getLatestVersion();

            plugin.getLogger().info("Доступна новая версия PerPlayerKit! У вас установлена " + currentVersion + ", а актуальная версия — " + latestVersion);
            plugin.getLogger().info("Скачать новую версию можно здесь:");
            plugin.getLogger().info("Spigot: " + spigotDownloadUrl);
            plugin.getLogger().info("Modrinth: " + modrinthDownloadUrl);
            plugin.getLogger().info("PaperMC: " + hangarDownloadUrl);
        } else {
            plugin.getLogger().info("Вы используете последнюю версию PerPlayerKit");
        }
    }

    public void sendUpdateMessage(Player player) {
        if (checkForUpdate()) {
            String currentVersion = getCurrentVersion();
            String latestVersion = getLatestVersion();

            player.sendMessage("§aДоступна новая версия PerPlayerKit! §7У вас версия §e" + currentVersion + "§7, актуальная — §e" + latestVersion);
        }
    }

    private boolean isSemanticallyNewer(String currentVersion, String newVersion) {
        String[] currentVersionSplit = currentVersion.split("\\.");
        String[] newVersionSplit = newVersion.split("\\.");

        for (int i = 0; i < Math.min(currentVersionSplit.length, newVersionSplit.length); i++) {
            int currentNumber = Integer.parseInt(currentVersionSplit[i]);
            int newNumber = Integer.parseInt(newVersionSplit[i]);

            if (currentNumber < newNumber) {
                return true;
            } else if (currentNumber > newNumber) {
                return false;
            }
        }

        return newVersionSplit.length > currentVersionSplit.length;
    }
}