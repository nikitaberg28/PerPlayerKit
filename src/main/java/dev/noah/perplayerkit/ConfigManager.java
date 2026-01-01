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

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ConfigManager { // Менеджер конфигурации
    private final File configFile; // Файл конфигурации
    private final FileConfiguration config; // Объект конфигурации
    private final Plugin plugin;

    public ConfigManager(Plugin plugin) {
        this.plugin = plugin;
        this.configFile = new File(plugin.getDataFolder(), "config.yml"); // Путь к config.yml
        this.config = YamlConfiguration.loadConfiguration(configFile); // Загрузка конфигурации из файла
    }

    public void loadConfig() { // Загрузить конфигурацию
        if (configFile.exists()) { // Если файл существует
            mergeMissingKeys(); // Объединить отсутствующие ключи
        }else{ // Если файл не существует
            plugin.saveDefaultConfig(); // Сохранить стандартную конфигурацию из ресурсов плагина
        }
        plugin.saveConfig(); // Сохранить текущую конфигурацию (в том числе внесенные изменения)
    }

    private void mergeMissingKeys() { // Объединить отсутствующие ключи
        InputStream defaultConfigStream = plugin.getResource("config.yml"); // Поток данных из ресурса config.yml
        if (defaultConfigStream == null) { // Если ресурс не найден
            return; // Выйти из метода
        }

        FileConfiguration defaultConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defaultConfigStream)); // Загрузка стандартной конфигурации

        boolean updated = false; // Флаг, указывающий, были ли внесены изменения

        //цикл по ключам и добавление отсутствующих
        for (String key : defaultConfig.getKeys(true)) { // Цикл по всем ключам в стандартной конфигурации
            //специальная обработка для публичных китов
            if (key.equals("publickits")) { // Если ключ - "publickits"
                // добавить publickits, если его нет
                if (!config.contains(key)) { // Проверить, есть ли этот ключ в текущей конфигурации
                    config.set(key, defaultConfig.getConfigurationSection(key).getValues(true)); // Установить значение секции
                    plugin.getLogger().info("Добавлена отсутствующая секция: publickits"); // Логировать добавление
                    updated = true; // Установить флаг обновления
                }
                continue; // Продолжить цикл
            }else if(key.startsWith("publickits")){ // Если ключ начинается с "publickits"
                continue; // Пропустить (не добавлять отдельные ключи внутри секции, только саму секцию)
            }

            // Добавить отсутствующие ключи для всего остального
            if (!config.contains(key)) { // Если ключ отсутствует в текущей конфигурации
                config.set(key, defaultConfig.get(key)); // Установить значение по умолчанию
                plugin.getLogger().info("Добавлен отсутствующий ключ конфигурации: " + key); // Логировать добавление
                updated = true; // Установить флаг обновления
            }
        }

        //сохранить обновленную конфигурацию
        if (updated) { // Если были внесены изменения
            try {
                config.save(configFile); // Сохранить конфигурацию в файл
                plugin.getLogger().info("Конфигурация обновлена отсутствующими ключами."); // Логировать успех
            } catch (Exception e) { // Обработка ошибок при сохранении
                plugin.getLogger().severe("Не удалось сохранить обновленную конфигурацию: " + e.getMessage()); // Логировать ошибку
            }
        }
    }


}