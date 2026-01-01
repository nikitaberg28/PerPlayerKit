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

import dev.noah.perplayerkit.commands.*;
import dev.noah.perplayerkit.commands.extracommands.HealCommand;
import dev.noah.perplayerkit.commands.extracommands.RepairCommand;
import dev.noah.perplayerkit.commands.tabcompleters.ECSlotTabCompleter;
import dev.noah.perplayerkit.commands.tabcompleters.KitSlotTabCompleter;
import dev.noah.perplayerkit.listeners.*;
import dev.noah.perplayerkit.listeners.antiexploit.CommandListener;
import dev.noah.perplayerkit.listeners.antiexploit.ShulkerDropItemsListener;
import dev.noah.perplayerkit.listeners.features.OldDeathDropListener;
import dev.noah.perplayerkit.storage.StorageManager;
import dev.noah.perplayerkit.storage.StorageSelector;
import dev.noah.perplayerkit.storage.exceptions.StorageConnectionException;
import dev.noah.perplayerkit.storage.exceptions.StorageOperationException;
import dev.noah.perplayerkit.util.BackupManager;
import dev.noah.perplayerkit.util.BroadcastManager;
import dev.noah.perplayerkit.util.StyleManager;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.ipvp.canvas.MenuFunctionListener;

public final class PerPlayerKit extends JavaPlugin { // Основной класс плагина PerPlayerKit

    public static Plugin plugin; // Статическая ссылка на плагин
    public static StorageManager storageManager; // Менеджер хранения данных
    private BackupManager backupManager; // Менеджер резервных копий

    public static Plugin getPlugin() { // Получить экземпляр плагина
        return plugin;
    }

    @Override
    public void onEnable() { // При включении плагина
        notice(); // Вывести уведомление о лицензии

        int bstatsId = 24380; // ID для bStats
        Metrics metrics = new Metrics(this, bstatsId); // Инициализация bStats

        plugin = this; // Установить статическую ссылку
        ConfigManager configManager = new ConfigManager(this); // Создать менеджер конфигурации
        configManager.loadConfig(); // Загрузить конфигурацию

        new StyleManager(this); // Инициализировать менеджер стилей

        new ItemFilter(this); // Инициализировать фильтр предметов
        new BroadcastManager(this); // Инициализировать менеджер трансляций

        new KitManager(this); // Инициализировать менеджер китов
        new KitShareManager(this); // Инициализировать менеджер обмена китами
        new KitRoomDataManager(this); // Инициализировать менеджер данных комнаты китов

        loadPublicKitsIdsFromConfig(); // Загрузить ID публичных китов из конфигурации
        getLogger().info("Конфигурация публичных китов загружена"); // Логировать

        String dbType = this.getConfig().getString("storage.type"); // Получить тип хранилища из конфигурации

        if (dbType == null) { // Если тип не указан
            this.getLogger().warning("Тип базы данных не найден в конфигурации, исправьте конфигурацию для продолжения!"); // Логировать ошибку
            this.getServer().getPluginManager().disablePlugin(this); // Отключить плагин
            return; // Выйти
        }

        storageManager = new StorageSelector(this, dbType).getDbManager(); // Выбрать и получить менеджер хранилища
        this.getLogger().info("Используется тип хранилища: " + storageManager.getClass().getName()); // Логировать тип

        if (storageManager == null) { // Если менеджер не создан
            this.getLogger().warning("Произошла ошибка базы данных, проверьте конфигурацию!"); // Логировать ошибку
            this.getServer().getPluginManager().disablePlugin(this); // Отключить плагин
            return; // Выйти
        }

        attemptDatabaseConnection(true); // Попытаться подключиться к БД

        try {
            storageManager.init(); // Инициализировать БД
        } catch (StorageOperationException e) { // Обработка ошибки инициализации
            this.getLogger().warning("Не удалось инициализировать базу данных. Отключение плагина."); // Логировать ошибку
            Bukkit.getPluginManager().disablePlugin(this); // Отключить плагин
            return; // Выйти
        }

        // Инициализация системы резервного копирования для файловых методов хранения
        if (isFileBasedStorage(dbType)) { // Если тип хранилища файловый
            backupManager = new BackupManager(this); // Создать менеджер резервных копий
            if (backupManager.isEnabled()) { // Если резервное копирование включено
                getLogger().info("Система резервного копирования инициализирована для файлового хранилища"); // Логировать
            } else { // Если резервное копирование отключено
                getLogger().info("Система резервного копирования отключена в конфигурации"); // Логировать
            }
        } else { // Если тип хранилища не файловый
            getLogger().info("Система резервного копирования не требуется для не файлового хранилища: " + dbType); // Логировать
        }

        Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> { // Запуск асинхронного таймера

            if (storageManager.isConnected()) { // Если подключение к БД активно
                try {
                    storageManager.keepAlive(); // Поддерживать соединение
                } catch (StorageConnectionException e) { // Обработка ошибки поддержки соединения
                    this.getLogger().warning("Поддержка соединения с БД не удалась: " + e.getMessage()); // Логировать ошибку
                }
            } else { // Если подключение к БД неактивно
                this.getLogger().warning("Подключение к базе данных не удалось. Попытка переподключения."); // Логировать ошибку
                attemptDatabaseConnection(false); // Попытаться переподключиться
            }

        }, 30 * 20, 30 * 20); // запускать каждые 30 секунд (в тиках)

        loadDatabaseData(); // Загрузить данные из БД
        getLogger().info("Данные базы данных загружены"); // Логировать

        UpdateChecker updateChecker = new UpdateChecker(this); // Создать проверку обновлений

        // РЕГИСТРАЦИЯ НАЧАЛО
        KitSlotTabCompleter kitSlotTabCompleter = new KitSlotTabCompleter(); // Комплитер слотов китов
        ECSlotTabCompleter ecSlotTabCompleter = new ECSlotTabCompleter(); // Комплитер слотов эндер-сундуков

        this.getCommand("kit").setExecutor(new MainMenuCommand(plugin)); // Регистрация команды /kit

        this.getCommand("sharekit").setExecutor(new ShareKitCommand()); // Регистрация команды /sharekit
        this.getCommand("sharekit").setTabCompleter(kitSlotTabCompleter); // Установка комплитера

        this.getCommand("shareec").setExecutor(new ShareECKitCommand()); // Регистрация команды /shareec
        this.getCommand("shareec").setTabCompleter(ecSlotTabCompleter); // Установка комплитера

        this.getCommand("copykit").setExecutor(new CopyKitCommand()); // Регистрация команды /copykit

        KitRoomCommand kitRoomCommand = new KitRoomCommand(); // Команда комнаты китов
        this.getCommand("kitroom").setExecutor(kitRoomCommand); // Регистрация команды /kitroom
        this.getCommand("kitroom").setTabCompleter(kitRoomCommand); // Установка комплитера

        this.getCommand("swapkit").setExecutor(new SwapKitCommand()); // Регистрация команды /swapkit
        this.getCommand("swapkit").setTabCompleter(kitSlotTabCompleter); // Установка комплитера

        this.getCommand("deletekit").setExecutor(new DeleteKitCommand()); // Регистрация команды /deletekit
        this.getCommand("deletekit").setTabCompleter(kitSlotTabCompleter); // Установка комплитера

        this.getCommand("inspectkit").setExecutor(new InspectKitCommand(plugin)); // Регистрация команды /inspectkit
        this.getCommand("inspectkit").setTabCompleter(new InspectKitCommand(plugin)); // Установка комплитера

        this.getCommand("inspectec").setExecutor(new InspectEcCommand(plugin)); // Регистрация команды /inspectec
        this.getCommand("inspectec").setTabCompleter(new InspectEcCommand(plugin)); // Установка комплитера

        this.getCommand("enderchest").setExecutor(new EnderchestCommand()); // Регистрация команды /enderchest

        SavePublicKitCommand savePublicKitCommand = new SavePublicKitCommand(); // Команда сохранения публичного кита
        this.getCommand("savepublickit").setExecutor(savePublicKitCommand); // Регистрация команды /savepublickit
        this.getCommand("savepublickit").setTabCompleter(savePublicKitCommand); // Установка комплитера

        PublicKitCommand publicKitCommand = new PublicKitCommand(plugin); // Команда публичного кита
        this.getCommand("publickit").setExecutor(publicKitCommand); // Регистрация команды /publickit
        this.getCommand("publickit").setTabCompleter(publicKitCommand); // Установка комплитера

        for (int i = 1; i <= 9; i++) { // Цикл для команд /k1 - /k9
            this.getCommand("k" + i).setExecutor(new ShortKitCommand()); // Регистрация короткой команды кита
        }

        for (int i = 1; i <= 9; i++) { // Цикл для команд /ec1 - /ec9
            this.getCommand("ec" + i).setExecutor(new ShortECCommand()); // Регистрация короткой команды эндер-сундука
        }

        RegearCommand regearCommand = new RegearCommand(this); // Команда пополнения
        this.getCommand("regear").setExecutor(regearCommand); // Регистрация команды /regear

        this.getCommand("heal").setExecutor(new HealCommand()); // Регистрация команды /heal
        this.getCommand("repair").setExecutor(new RepairCommand()); // Регистрация команды /repair
        this.getCommand("perplayerkit").setExecutor(new PerPlayerKitCommand(this)); // Регистрация команды /perplayerkit

        Bukkit.getPluginManager().registerEvents(regearCommand, this); // Регистрация слушателя пополнения
        Bukkit.getPluginManager().registerEvents(new JoinListener(this, updateChecker), this); // Регистрация слушателя входа
        Bukkit.getPluginManager().registerEvents(new QuitListener(this), this); // Регистрация слушателя выхода
        Bukkit.getPluginManager().registerEvents(new MenuFunctionListener(), this); // Регистрация слушателя меню
        Bukkit.getPluginManager().registerEvents(new KitMenuCloseListener(), this); // Регистрация слушателя закрытия меню китов
        Bukkit.getPluginManager().registerEvents(new KitRoomSaveListener(), this); // Регистрация слушателя сохранения комнаты китов
        Bukkit.getPluginManager().registerEvents(new AutoRekitListener(this), this); // Регистрация слушателя автосмены кита
        Bukkit.getPluginManager().registerEvents(new AboutCommandListener(), this); // Регистрация слушателя команды "о плагине"

        // функции
        if (getConfig().getBoolean("feature.old-death-drops", false)) { // Если включена старая система выпадения вещей при смерти
            Bukkit.getPluginManager().registerEvents(new OldDeathDropListener(), this); // Регистрация слушателя
        }

        if (getConfig().getBoolean("anti-exploit.block-spaces-in-commands", false)) { // Если включена блокировка пробелов в командах
            Bukkit.getPluginManager().registerEvents(new CommandListener(), this); // Регистрация слушателя
        }

        if (getConfig().getBoolean("anti-exploit.prevent-shulkers-dropping-items", false)) { // Если включена защита от выпадения предметов из шалкеров
            Bukkit.getPluginManager().registerEvents(new ShulkerDropItemsListener(), this); // Регистрация слушателя
        }

        // РЕГИСТРАЦИЯ КОНЕЦ

        BroadcastManager.get().startScheduledBroadcast(); // Запуск регулярных трансляций
        updateChecker.printStartupStatus(); // Вывод статуса обновления при запуске

    }

    @Override
    public void onDisable() { // При отключении плагина
        closeDatabaseConnection(); // Закрыть соединение с БД

        // Завершение работы менеджера резервных копий, если он существует
        if (backupManager != null) { // Если менеджер инициализирован
            backupManager.shutdown(); // Завершить его работу
        }
    }

    /**
     * Проверить, является ли тип хранилища файловым (требует резервные копии)
     *
     * @param storageType Тип хранилища из конфигурации
     * @return true, если файловое хранилище, false в противном случае
     */
    private boolean isFileBasedStorage(String storageType) { // Проверка файлового хранилища
        return storageType.equalsIgnoreCase("sqlite") || // SQLite
                storageType.equalsIgnoreCase("yml") || // YAML
                storageType.equalsIgnoreCase("yaml"); // YAML
    }

    private void loadPublicKitsIdsFromConfig() { // Загрузка ID публичных китов из конфигурации
        // генерация списка публичных китов из конфигурации
        ConfigurationSection publicKitsSection = getConfig().getConfigurationSection("publickits"); // Получить секцию publickits

        if (publicKitsSection == null) { // Если секция отсутствует
            this.getLogger().warning("В конфигурации не найдено публичных китов!"); // Логировать предупреждение
        } else { // Если секция существует

            publicKitsSection.getKeys(false).forEach(key -> { // Цикл по ключам (ID китов)
                String name = getConfig().getString("publickits." + key + ".name"); // Получить имя
                Material icon = Material.valueOf(getConfig().getString("publickits." + key + ".icon")); // Получить иконку
                PublicKit kit = new PublicKit(key, name, icon); // Создать объект PublicKit
                KitManager.get().getPublicKitList().add(kit); // Добавить в список публичных китов
            });
        }
    }

    private void loadDatabaseData() { // Загрузка данных из БД
        KitRoomDataManager.get().loadFromDB(); // Загрузить данные комнаты китов
        KitManager.get().getPublicKitList().forEach(kit -> KitManager.get().loadPublicKitFromDB(kit.id)); // Загрузить каждый публичный кит
        Bukkit.getOnlinePlayers().forEach(player -> KitManager.get().loadPlayerDataFromDB(player.getUniqueId())); // Загрузить данные для каждого онлайн-игрока

    }

    private void attemptDatabaseConnection(boolean disableOnFail) { // Попытка подключения к БД
        try {
            storageManager.connect(); // Подключиться
            if (!storageManager.isConnected()) { // Если подключение не установлено
                throw new StorageConnectionException("Ожидалось подключение к базе данных, но оно не удалось."); // Выбросить исключение
            }
        } catch (StorageConnectionException e) { // Обработка ошибки подключения
            if (disableOnFail) { // Если нужно отключить плагин при ошибке
                this.getLogger().warning("Подключение к базе данных не удалось: " + e.getMessage()); // Логировать ошибку
                this.getLogger().warning("Отключение плагина."); // Логировать отключение
                Bukkit.getPluginManager().disablePlugin(this); // Отключить плагин
            } else { // Если не нужно отключать плагин
                this.getLogger().warning("Подключение к базе данных не удалось: " + e.getMessage()); // Логировать ошибку
            }
        }
    }

    private void closeDatabaseConnection() { // Закрытие соединения с БД
        try {
            storageManager.close(); // Закрыть соединение
        } catch (StorageConnectionException e) { // Обработка ошибки закрытия
            // повторить попытку один раз
            try {
                storageManager.close(); // Повторное закрытие
            } catch (StorageConnectionException ex) { // Обработка ошибки при повторной попытке
                this.getLogger().warning("Не удалось закрыть соединение с базой данных: " + e.getMessage()); // Логировать ошибку
            }
        }
    }

    private void notice() { // Вывод уведомления о лицензии
        String notice = """
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
                """; // Основное уведомление о лицензии

        String otherInfo = """
                * Все пользователи должны получить исходный код программного обеспечения, в соответствии с лицензией AGPL-3.0.
                * Если вы используете изменённую версию PerPlayerKit, вы должны сделать исходный код вашей
                * изменённой версии доступным для всех пользователей, в соответствии с лицензией AGPL-3.0.
                * Рассмотрите возможность изменения команды /aboutperplayerkit, чтобы включить в неё ссылку на ваш изменённый исходный код.
                """; // Дополнительная информация о лицензии

        getLogger().info(notice); // Логировать основное уведомление
        getLogger().info(otherInfo); // Логировать дополнительную информацию
    }
}