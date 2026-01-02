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

public final class PerPlayerKit extends JavaPlugin {

    public static Plugin plugin;
    public static StorageManager storageManager;
    private BackupManager backupManager;

    public static Plugin getPlugin() {
        return plugin;
    }

    @Override
    public void onEnable() {
        notice();

        int bstatsId = 24380;
        Metrics metrics = new Metrics(this, bstatsId);

        plugin = this;
        ConfigManager configManager = new ConfigManager(this);
        configManager.loadConfig();

        new StyleManager(this);

        new ItemFilter(this);
        new BroadcastManager(this);

        new KitManager(this);
        new KitShareManager(this);
        new KitRoomDataManager(this);

        loadPublicKitsIdsFromConfig();
        getLogger().info("Конфигурация публичных китов загружена");

        String dbType = this.getConfig().getString("storage.type");

        if (dbType == null) {
            this.getLogger().warning("Тип базы данных не найден в конфиге, исправьте конфиг для продолжения!");
            this.getServer().getPluginManager().disablePlugin(this);
            return;
        }

        storageManager = new StorageSelector(this, dbType).getDbManager();
        this.getLogger().info("Используемый тип хранилища: " + storageManager.getClass().getName());

        if (storageManager == null) {
            this.getLogger().warning("Произошла ошибка базы данных, пожалуйста, проверьте конфиг!");
            this.getServer().getPluginManager().disablePlugin(this);
            return;
        }

        attemptDatabaseConnection(true);

        try {
            storageManager.init();
        } catch (StorageOperationException e) {
            this.getLogger().warning("Не удалось инициализировать базу данных. Плагин отключается.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        // Инициализация системы резервного копирования для файловых методов хранения
        if (isFileBasedStorage(dbType)) {
            backupManager = new BackupManager(this);
            if (backupManager.isEnabled()) {
                getLogger().info("Система бэкапов инициализирована для файлового хранилища");
            } else {
                getLogger().info("Система бэкапов отключена в конфигурации");
            }
        } else {
            getLogger().info("Система бэкапов не требуется для не-файлового хранилища: " + dbType);
        }

        Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> {

            if (storageManager.isConnected()) {
                try {
                    storageManager.keepAlive();
                } catch (StorageConnectionException e) {
                    this.getLogger().warning("Keep alive базы данных не удался: " + e.getMessage());
                }
            } else {
                this.getLogger().warning("Соединение с базой данных потеряно. Попытка переподключения.");
                attemptDatabaseConnection(false);
            }

        }, 30 * 20, 30 * 20); // выполняется каждые 30 секунд

        loadDatabaseData();
        getLogger().info("Данные базы данных загружены");

        UpdateChecker updateChecker = new UpdateChecker(this);

        // НАЧАЛО РЕГИСТРАЦИИ
        KitSlotTabCompleter kitSlotTabCompleter = new KitSlotTabCompleter();
        ECSlotTabCompleter ecSlotTabCompleter = new ECSlotTabCompleter();

        this.getCommand("kit").setExecutor(new MainMenuCommand(plugin));

        this.getCommand("sharekit").setExecutor(new ShareKitCommand());
        this.getCommand("sharekit").setTabCompleter(kitSlotTabCompleter);

        this.getCommand("shareec").setExecutor(new ShareECKitCommand());
        this.getCommand("shareec").setTabCompleter(ecSlotTabCompleter);

        this.getCommand("copykit").setExecutor(new CopyKitCommand());

        KitRoomCommand kitRoomCommand = new KitRoomCommand();
        this.getCommand("kitroom").setExecutor(kitRoomCommand);
        this.getCommand("kitroom").setTabCompleter(kitRoomCommand);

        this.getCommand("swapkit").setExecutor(new SwapKitCommand());
        this.getCommand("swapkit").setTabCompleter(kitSlotTabCompleter);

        this.getCommand("deletekit").setExecutor(new DeleteKitCommand());
        this.getCommand("deletekit").setTabCompleter(kitSlotTabCompleter);

        this.getCommand("inspectkit").setExecutor(new InspectKitCommand(plugin));
        this.getCommand("inspectkit").setTabCompleter(new InspectKitCommand(plugin));

        this.getCommand("inspectec").setExecutor(new InspectEcCommand(plugin));
        this.getCommand("inspectec").setTabCompleter(new InspectEcCommand(plugin));

        this.getCommand("enderchest").setExecutor(new EnderchestCommand());

        SavePublicKitCommand savePublicKitCommand = new SavePublicKitCommand();
        this.getCommand("savepublickit").setExecutor(savePublicKitCommand);
        this.getCommand("savepublickit").setTabCompleter(savePublicKitCommand);

        PublicKitCommand publicKitCommand = new PublicKitCommand(plugin);
        this.getCommand("publickit").setExecutor(publicKitCommand);
        this.getCommand("publickit").setTabCompleter(publicKitCommand);

        for (int i = 1; i <= 9; i++) {
            this.getCommand("k" + i).setExecutor(new ShortKitCommand());
        }

        for (int i = 1; i <= 9; i++) {
            this.getCommand("ec" + i).setExecutor(new ShortECCommand());
        }

        RegearCommand regearCommand = new RegearCommand(this);
        this.getCommand("regear").setExecutor(regearCommand);

        this.getCommand("heal").setExecutor(new HealCommand());
        this.getCommand("repair").setExecutor(new RepairCommand());
        this.getCommand("perplayerkit").setExecutor(new PerPlayerKitCommand(this));

        Bukkit.getPluginManager().registerEvents(regearCommand, this);
        Bukkit.getPluginManager().registerEvents(new JoinListener(this, updateChecker), this);
        Bukkit.getPluginManager().registerEvents(new QuitListener(this), this);
        Bukkit.getPluginManager().registerEvents(new MenuFunctionListener(), this);
        Bukkit.getPluginManager().registerEvents(new KitMenuCloseListener(), this);
        Bukkit.getPluginManager().registerEvents(new KitRoomSaveListener(), this);
        Bukkit.getPluginManager().registerEvents(new AutoRekitListener(this), this);
        Bukkit.getPluginManager().registerEvents(new AboutCommandListener(), this);

        // Функции (features)
        if (getConfig().getBoolean("feature.old-death-drops", false)) {
            Bukkit.getPluginManager().registerEvents(new OldDeathDropListener(), this);
        }

        if (getConfig().getBoolean("anti-exploit.block-spaces-in-commands", false)) {
            Bukkit.getPluginManager().registerEvents(new CommandListener(), this);
        }

        if (getConfig().getBoolean("anti-exploit.prevent-shulkers-dropping-items", false)) {
            Bukkit.getPluginManager().registerEvents(new ShulkerDropItemsListener(), this);
        }

        // КОНЕЦ РЕГИСТРАЦИИ

        BroadcastManager.get().startScheduledBroadcast();
        updateChecker.printStartupStatus();

    }

    @Override
    public void onDisable() {
        closeDatabaseConnection();

        // Выключение менеджера бэкапов, если он существует
        if (backupManager != null) {
            backupManager.shutdown();
        }
    }

    /**
     * Проверяет, является ли тип хранилища файловым (требует бэкапов)
     * * @param storageType Тип хранилища из конфигурации
     * @return true, если хранилище файловое, иначе false
     */
    private boolean isFileBasedStorage(String storageType) {
        return storageType.equalsIgnoreCase("sqlite") ||
                storageType.equalsIgnoreCase("yml") ||
                storageType.equalsIgnoreCase("yaml");
    }

    private void loadPublicKitsIdsFromConfig() {
        // Генерация списка публичных китов из конфига
        ConfigurationSection publicKitsSection = getConfig().getConfigurationSection("publickits");

        if (publicKitsSection == null) {
            this.getLogger().warning("Публичные киты не найдены в конфиге!");
        } else {

            publicKitsSection.getKeys(false).forEach(key -> {
                String name = getConfig().getString("publickits." + key + ".name");
                Material icon = Material.valueOf(getConfig().getString("publickits." + key + ".icon"));
                PublicKit kit = new PublicKit(key, name, icon);
                KitManager.get().getPublicKitList().add(kit);
            });
        }
    }

    private void loadDatabaseData() {
        KitRoomDataManager.get().loadFromDB();
        KitManager.get().getPublicKitList().forEach(kit -> KitManager.get().loadPublicKitFromDB(kit.id));
        Bukkit.getOnlinePlayers().forEach(player -> KitManager.get().loadPlayerDataFromDB(player.getUniqueId()));

    }

    private void attemptDatabaseConnection(boolean disableOnFail) {
        try {
            storageManager.connect();
            if (!storageManager.isConnected()) {
                throw new StorageConnectionException("Ожидалось подключение к базе данных, но оно не удалось.");
            }
        } catch (StorageConnectionException e) {
            if (disableOnFail) {
                this.getLogger().warning("Ошибка подключения к базе данных: " + e.getMessage());
                this.getLogger().warning("Плагин отключается.");
                Bukkit.getPluginManager().disablePlugin(this);
            } else {
                this.getLogger().warning("Ошибка подключения к базе данных: " + e.getMessage());
            }
        }
    }

    private void closeDatabaseConnection() {
        try {
            storageManager.close();
        } catch (StorageConnectionException e) {
            // одна попытка повтора
            try {
                storageManager.close();
            } catch (StorageConnectionException ex) {
                this.getLogger().warning("Не удалось закрыть соединение с базой данных: " + e.getMessage());
            }
        }
    }

    private void notice() {
        String notice = """
                * PerPlayerKit — это свободное программное обеспечение: вы можете распространять и/или изменять его
                * в соответствии с условиями лицензии GNU Affero General Public License, опубликованной
                * Free Software Foundation, версии 3 или (по вашему выбору) любой более поздней версии.
                *
                * PerPlayerKit распространяется в надежде, что он будет полезен, но БЕЗ КАКИХ-ЛИБО ГАРАНТИЙ.
                * Для получения дополнительной информации см. лицензию GNU Affero General Public License.
                *
                * Вы должны были получить копию GNU Affero General Public License вместе с PerPlayerKit.
                * Если нет, см. <https://www.gnu.org/licenses/>.""";

        String otherInfo = """
                * Всем пользователям должен быть предоставлен исходный код программного обеспечения в соответствии с лицензией AGPL-3.0.
                * Если вы используете модифицированную версию PerPlayerKit, вы должны предоставить исходный код своей
                * модифицированной версии всем пользователям в соответствии с лицензией AGPL-3.0.
                * Рекомендуется изменить команду /aboutperplayerkit, чтобы включить ссылку на ваш модифицированный исходный код.
                """;

        getLogger().info(notice);
        getLogger().info(otherInfo);
    }
}