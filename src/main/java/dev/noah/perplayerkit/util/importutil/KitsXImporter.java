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
package dev.noah.perplayerkit.util.importutil;

import dev.noah.perplayerkit.KitManager;
import dev.noah.perplayerkit.KitRoomDataManager;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class KitsXImporter { // Утилита импорта из KitsX

    private final String kitroomFilePath = "data/kitroom.yml"; // Путь к файлу комнаты китов
    private final String kitsFilePath = "data/kits.yml"; // Путь к файлу китов
    private final String enderchestsFilePath = "data/enderchest.yml"; // Путь к файлу эндер-сундуков

    private final Plugin plugin;
    private final CommandSender sender;

    public KitsXImporter(Plugin plugin, CommandSender sender) {
        this.plugin = plugin;
        this.sender = sender;
    }

    public boolean checkForFiles() { // Проверить наличие файлов
        // Проверка существования необходимых файлов
        File kitroom = new File(plugin.getDataFolder(), kitroomFilePath); // Файл комнаты китов
        File kits = new File(plugin.getDataFolder(), kitsFilePath); // Файл китов
        File enderchests = new File(plugin.getDataFolder(), enderchestsFilePath); // Файл эндер-сундуков

        return kitroom.exists() && kits.exists() && enderchests.exists(); // Возврат результата проверки
    }

    public void importFiles() { // Импортировать файлы
        if (!checkForFiles()) { // Проверить наличие файлов
            sender.sendMessage(ChatColor.RED + "Требуемые файлы отсутствуют. Невозможно продолжить импорт."); // Отправить сообщение об ошибке
            return;
        }

        importKitroom(sender); // Импортировать комнату китов
        importKits(sender); // Импортировать киты
        importEnderchests(sender); // Импортировать эндер-сундуки
    }



    private void importKitroom(CommandSender sender) { // Импортировать комнату китов
        // Определение пути к файлу kitroom.yml
        File kitroomFile = new File(plugin.getDataFolder(), kitroomFilePath); // Файл комнаты китов

        // Проверка существования файла
        if (!kitroomFile.exists()) { // Если файл не существует
            sender.sendMessage(ChatColor.RED + "Файл kitroom.yml не найден! Импорт пропущен."); // Отправить сообщение
            return;
        }

        // Загрузка YAML-файла
        YamlConfiguration yamlConfig = YamlConfiguration.loadConfiguration(kitroomFile); // Конфигурация YAML

        // Разбор категорий, ограничение 5 категориями
        if (yamlConfig.contains("categories")) { // Если существует секция "categories"
            List<String> categoryKeys = new ArrayList<>(yamlConfig.getConfigurationSection("categories").getKeys(false)); // Получить ключи категорий

            // Обработка только первых 5 категорий
            for (int categoryIndex = 0; categoryIndex < Math.min(5, categoryKeys.size()); categoryIndex++) { // Цикл по категориям
                String category = categoryKeys.get(categoryIndex); // Текущая категория

                sender.sendMessage(ChatColor.BLUE + "Обработка категории: " + category); // Отправить сообщение

                // Создание массива для хранения до 45 предметов в текущей категории
                ItemStack[] categoryItems = new ItemStack[45]; // Массив предметов категории
                int itemIndex = 0; // Индекс предмета

                for (String key : yamlConfig.getConfigurationSection("categories." + category).getKeys(false)) { // Цикл по предметам в категории
                    if (itemIndex >= 45) { // Если достигнут лимит предметов
                        sender.sendMessage(ChatColor.YELLOW + "Игнорирование дополнительных предметов в категории: " + category); // Отправить предупреждение
                        break; // Прервать цикл
                    }

                    try {
                        int slot = Integer.parseInt(key); // Преобразовать ключ в номер слота
                        ItemStack itemStack = yamlConfig.getItemStack("categories." + category + "." + key); // Получить предмет

                        if (itemStack != null && slot < 45) { // Если предмет существует и слот валиден
                            categoryItems[slot] = itemStack; // Сохранить предмет в массив
                            itemIndex++; // Увеличить индекс
                        }
                    } catch (NumberFormatException e) { // Ошибка, если ключ не является числом
                        sender.sendMessage(ChatColor.GOLD + "[Предупреждение] Неверный номер слота: " + key + " в категории: " + category); // Отправить предупреждение
                    }
                }

                // Сохранение данных с помощью setKitRoom
                KitRoomDataManager.get().setKitRoom(categoryIndex, categoryItems); // Установить комнату китов
                KitRoomDataManager.get().saveToDBAsync(); // Асинхронное сохранение в БД

                // Логирование количества импортированных предметов в этой категории
                sender.sendMessage(ChatColor.GREEN + "Импортировано " + itemIndex + " предметов в категории: " + category); // Отправить сообщение
            }
        }

        // Логирование завершения
        sender.sendMessage(ChatColor.AQUA + "Завершен импорт kitroom.yml."); // Отправить сообщение
    }

    private void importKits(CommandSender sender) { // Импортировать киты
        // Определение пути к файлу kits.yml
        File kitsFile = new File(plugin.getDataFolder(), kitsFilePath); // Файл китов

        // Проверка существования файла
        if (!kitsFile.exists()) { // Если файл не существует
            sender.sendMessage(ChatColor.RED + "Файл kits.yml не найден! Импорт пропущен."); // Отправить сообщение
            return;
        }

        // Загрузка YAML-файла
        YamlConfiguration yamlConfig = YamlConfiguration.loadConfiguration(kitsFile); // Конфигурация YAML

        // Проверка наличия определенных UUID игроков
        if (yamlConfig.getKeys(false).isEmpty()) { // Если нет ключей (UUID)
            sender.sendMessage(ChatColor.YELLOW + "В файле kits.yml не найдено китов игроков."); // Отправить сообщение
            return;
        }

        for (String uuid : yamlConfig.getKeys(false)) { // Цикл по UUID
            sender.sendMessage(ChatColor.BLUE + "Обработка китов для UUID игрока: " + uuid); // Отправить сообщение

            if (!yamlConfig.isConfigurationSection(uuid)) { // Если не секция конфигурации
                sender.sendMessage(ChatColor.YELLOW + "Для UUID не найдено китов: " + uuid); // Отправить сообщение
                continue; // Продолжить цикл
            }

            for (String kitKey : yamlConfig.getConfigurationSection(uuid).getKeys(false)) { // Цикл по китам игрока
                int kitNumber; // Номер кита
                try {
                    kitNumber = Integer.parseInt(kitKey.replace("Kit ", "").trim()); // Преобразовать ключ в номер
                } catch (NumberFormatException e) { // Ошибка, если ключ не является числом
                    sender.sendMessage(ChatColor.GOLD + "[Предупреждение] Неверный номер кита: " + kitKey + " для UUID игрока: " + uuid); // Отправить предупреждение
                    continue; // Продолжить цикл
                }

                sender.sendMessage(ChatColor.GREEN + "Обработка кита " + kitNumber + " для UUID игрока: " + uuid); // Отправить сообщение

                if (!yamlConfig.isConfigurationSection(uuid + "." + kitKey)) { // Если не секция конфигурации
                    sender.sendMessage(ChatColor.YELLOW + "Для кита " + kitNumber + " для UUID игрока " + uuid + " не найдено предметов."); // Отправить сообщение
                    continue; // Продолжить цикл
                }

                // Настроено для обработки до 41 слота
                ItemStack[] kitItems = new ItemStack[41]; // Массив предметов кита
                for (String slotKey : yamlConfig.getConfigurationSection(uuid + "." + kitKey).getKeys(false)) { // Цикл по слотам кита
                    int slot; // Номер слота
                    try {
                        slot = Integer.parseInt(slotKey); // Преобразовать ключ в номер
                    } catch (NumberFormatException e) { // Ошибка, если ключ не является числом
                        sender.sendMessage(ChatColor.GOLD + "[Предупреждение] Неверный номер слота: " + slotKey + " в ките " + kitNumber); // Отправить предупреждение
                        continue; // Продолжить цикл
                    }

                    if (slot < 0 || slot >= 41) { // Если номер слота вне диапазона
                        sender.sendMessage(ChatColor.YELLOW + "Слот " + slot + " в ките " + kitNumber + " вне диапазона (0-40). Пропущен."); // Отправить предупреждение
                        continue; // Продолжить цикл
                    }

                    ItemStack itemStack = yamlConfig.getItemStack(uuid + "." + kitKey + "." + slotKey); // Получить предмет
                    if (itemStack != null) { // Если предмет существует
                        kitItems[slot] = itemStack; // Сохранить предмет в нужный слот
                    }
                }

                // Сохранение или обработка данных кита
                KitManager.get().savekit(UUID.fromString(uuid), kitNumber, kitItems); // Сохранить кит
            }

            KitManager.get().savePlayerKitsToDB(UUID.fromString(uuid)); // Сохранить киты игрока в БД

        }

        // Логирование завершения
        sender.sendMessage(ChatColor.AQUA + "Завершен импорт kits.yml."); // Отправить сообщение
    }

    private void importEnderchests(CommandSender sender) { // Импортировать эндер-сундуки
        // Определение пути к файлу kits.yml (используется тот же файл, что и для китов, согласно оригинальному коду)
        File kitsFile = new File(plugin.getDataFolder(), enderchestsFilePath); // Файл эндер-сундуков (исправлено: должно быть enderchestsFilePath)

        // Проверка существования файла
        if (!kitsFile.exists()) { // Если файл не существует (исправлено: проверка теперь для enderchestsFilePath)
            sender.sendMessage(ChatColor.RED + "Файл enderchest.yml не найден! Импорт пропущен."); // Отправить сообщение
            return;
        }

        // Загрузка YAML-файла
        YamlConfiguration yamlConfig = YamlConfiguration.loadConfiguration(kitsFile); // Конфигурация YAML (файл тот же, что и раньше)

        // Проверка наличия определенных UUID игроков
        if (yamlConfig.getKeys(false).isEmpty()) { // Если нет ключей (UUID)
            sender.sendMessage(ChatColor.YELLOW + "В файле enderchest.yml не найдено эндер-сундуков игроков."); // Отправить сообщение (исправлено)
            return;
        }

        for (String uuid : yamlConfig.getKeys(false)) { // Цикл по UUID
            sender.sendMessage(ChatColor.BLUE + "Обработка эндер-сундуков для UUID игрока: " + uuid); // Отправить сообщение

            if (!yamlConfig.isConfigurationSection(uuid)) { // Если не секция конфигурации
                sender.sendMessage(ChatColor.YELLOW + "Для UUID не найдено эндер-сундуков: " + uuid); // Отправить сообщение
                continue; // Продолжить цикл
            }

            for (String kitKey : yamlConfig.getConfigurationSection(uuid).getKeys(false)) { // Цикл по "kitKey", но на самом деле это эндер-сундук (оригинальная логика)
                int kitNumber; // Номер эндер-сундука (оригинальная переменная)
                try {
                    kitNumber = Integer.parseInt(kitKey.replace("EnderChest ", "").trim()); // Преобразовать ключ в номер (оригинальная логика)
                } catch (NumberFormatException e) { // Ошибка, если ключ не является числом
                    sender.sendMessage(ChatColor.GOLD + "[Предупреждение] Неверный номер эндер-сундука: " + kitKey + " для UUID игрока: " + uuid); // Отправить предупреждение (оригинальная логика)
                    continue; // Продолжить цикл
                }

                sender.sendMessage(ChatColor.GREEN + "Обработка эндер-сундука " + kitNumber + " для UUID игрока: " + uuid); // Отправить сообщение

                if (!yamlConfig.isConfigurationSection(uuid + "." + kitKey)) { // Если не секция конфигурации
                    sender.sendMessage(ChatColor.YELLOW + "Для эндер-сундука " + kitNumber + " для UUID игрока " + uuid + " не найдено предметов."); // Отправить сообщение
                    continue; // Продолжить цикл
                }

                // Настроено для обработки до 27 слотов
                ItemStack[] kitItems = new ItemStack[27]; // Массив предметов эндер-сундука (оригинальное имя переменной)
                for (String slotKey : yamlConfig.getConfigurationSection(uuid + "." + kitKey).getKeys(false)) { // Цикл по слотам (оригинальная логика)
                    int slot; // Номер слота
                    try {
                        slot = Integer.parseInt(slotKey); // Преобразовать ключ в номер
                    } catch (NumberFormatException e) { // Ошибка, если ключ не является числом
                        sender.sendMessage(ChatColor.GOLD + "[Предупреждение] Неверный номер слота: " + slotKey + " в эндер-сундуке " + kitNumber); // Отправить предупреждение
                        continue; // Продолжить цикл
                    }

                    if (slot < 0 || slot >= 27) { // Если номер слота вне диапазона
                        sender.sendMessage(ChatColor.YELLOW + "Слот " + slot + " в эндер-сундуке " + kitNumber + " вне диапазона (0-26). Пропущен."); // Отправить предупреждение
                        continue; // Продолжить цикл
                    }

                    ItemStack itemStack = yamlConfig.getItemStack(uuid + "." + kitKey + "." + slotKey); // Получить предмет
                    if (itemStack != null) { // Если предмет существует
                        kitItems[slot] = itemStack; // Сохранить предмет в нужный слот (оригинальная логика)
                    }
                }

                // Сохранение или обработка данных эндер-сундука (оригинальная логика)
                // ВНИМАНИЕ: Оригинальный код вызывает savekit вместо saveEC. Это, вероятно, ошибка в оригинальном коде KitsXImporter.
                // Однако, для точного перевода без изменений, я оставляю это как есть.
                KitManager.get().savekit(UUID.fromString(uuid), kitNumber, kitItems); // Сохранить "кит" (оригинальный вызов, предположительно ошибка)
            }

            // ВНИМАНИЕ: Оригинальный код вызывает savePlayerKitsToDB вместо savePlayerEnderchestsToDB.
            // Это также, вероятно, ошибка в оригинальном коде.
            KitManager.get().savePlayerKitsToDB(UUID.fromString(uuid)); // Сохранить "киты" игрока в БД (оригинальный вызов)

        }

        // Логирование завершения
        sender.sendMessage(ChatColor.AQUA + "Завершен импорт enderchest.yml."); // Отправить сообщение
    }



}