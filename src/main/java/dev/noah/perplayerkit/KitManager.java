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

import dev.noah.perplayerkit.util.BroadcastManager;
import dev.noah.perplayerkit.util.IDUtil;
import dev.noah.perplayerkit.util.Serializer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import dev.noah.perplayerkit.util.SoundManager;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;
import java.util.*;

public class KitManager { // Менеджер китов
    private static KitManager instance; // Экземпляр менеджера
    private final PerPlayerKit plugin;
    private final HashMap<String, ItemStack[]> kitByKitIDMap; // Карта китов по ID
    private final HashMap<UUID, Integer> lastKitUsedByPlayer; // Последний использованный кит игроком
    private final List<PublicKit> publicKitList; // Список публичных китов

    public KitManager(PerPlayerKit plugin) {
        this.plugin = plugin;
        lastKitUsedByPlayer = new HashMap<>();
        publicKitList = new ArrayList<>();
        kitByKitIDMap = new HashMap<>();
        instance = this;
    }

    public static KitManager get() { // Получить экземпляр менеджера
        if (instance == null) {
            throw new IllegalStateException("KitManager не инициализирован");
        }
        return instance;
    }

    public ItemStack[] getItemStackArrayById(String id) { // Получить массив предметов по ID
        return kitByKitIDMap.get(id);
    }

    public List<PublicKit> getPublicKitList() { // Получить список публичных китов
        return publicKitList;
    }

    public int getLastKitLoaded(UUID uuid) { // Получить последний загруженный кит
        if (lastKitUsedByPlayer.containsKey(uuid)) {
            return lastKitUsedByPlayer.get(uuid);
        }
        return -1;
    }

    public boolean savekit(UUID uuid, int slot, ItemStack[] kit) { // Сохранить кит
        if (Bukkit.getPlayer(uuid) != null) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                boolean notEmpty = false; // Флаг, что кит не пустой
                for (ItemStack i : kit) { // Проверка, есть ли в ките предметы
                    if (i != null) {
                        if (!notEmpty) {
                            notEmpty = true;
                        }
                    }
                }

                if (notEmpty) { // Если кит не пустой
                    // Проверка и корректировка слотов брони
                    if (kit[36] != null) { // Слот ботинок
                        if (!kit[36].getType().toString().contains("BOOTS")) {
                            kit[36] = null;
                        }
                    }
                    if (kit[37] != null) { // Слот понож
                        if (!kit[37].getType().toString().contains("LEGGINGS")) {
                            kit[37] = null;
                        }
                    }
                    if (kit[38] != null) { // Слот нагрудника
                        if (!(kit[38].getType().toString().contains("CHESTPLATE") || kit[38].getType().toString().contains("ELYTRA"))) {
                            kit[38] = null;
                        }
                    }
                    if (kit[39] != null) { // Слот шлема
                        if (!kit[39].getType().toString().contains("HELMET")) {
                            kit[39] = null;
                        }
                    }

                    kitByKitIDMap.put(IDUtil.getPlayerKitId(uuid, slot), kit); // Сохранить кит в памяти
                    player.sendMessage(ChatColor.GREEN + "Кит " + slot + " сохранен!"); // Сообщить игроку

                    Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> savePlayerKitToDB(uuid, slot)); // Асинхронное сохранение в БД
                    return true;
                } else { // Если кит пустой
                    player.sendMessage(ChatColor.RED + "Нельзя сохранить пустой кит!");
                }
            }
        }
        return false;
    }

    public boolean savePublicKit(Player player, String publickit, ItemStack[] kit) { // Сохранить публичный кит
        boolean notEmpty = false; // Флаг, что кит не пустой
        for (ItemStack i : kit) { // Проверка, есть ли в ките предметы
            if (i != null) {
                if (!notEmpty) {
                    notEmpty = true;
                }
            }
        }

        if (notEmpty) { // Если кит не пустой
            // Проверка и корректировка слотов брони
            if (kit[36] != null) { // Слот ботинок
                if (!kit[36].getType().toString().contains("BOOTS")) {
                    kit[36] = null;
                }
            }
            if (kit[37] != null) { // Слот понож
                if (!kit[37].getType().toString().contains("LEGGINGS")) {
                    kit[37] = null;
                }
            }
            if (kit[38] != null) { // Слот нагрудника
                if (!(kit[38].getType().toString().contains("CHESTPLATE") || kit[38].getType().toString().contains("ELYTRA"))) {
                    kit[38] = null;
                }
            }
            if (kit[39] != null) { // Слот шлема
                if (!kit[39].getType().toString().contains("HELMET")) {
                    kit[39] = null;
                }
            }

            kitByKitIDMap.put(IDUtil.getPublicKitId(publickit), kit); // Сохранить кит в памяти
            player.sendMessage(ChatColor.GREEN + "Публичный кит " + publickit + " сохранен!"); // Сообщить игроку

            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> savePublicKitToDB(publickit)); // Асинхронное сохранение в БД
            return true;
        } else { // Если кит пустой
            player.sendMessage(ChatColor.RED + "Нельзя сохранить пустой кит!");
        }
        return false;
    }

    public boolean savePublicKit(String id, ItemStack[] kit) { // Сохранить публичный кит (внутреннее использование)
        boolean notEmpty = false; // Флаг, что кит не пустой
        for (ItemStack i : kit) { // Проверка, есть ли в ките предметы
            if (i != null) {
                if (!notEmpty) {
                    notEmpty = true;
                }
            }
        }

        if (notEmpty) { // Если кит не пустой
            // Проверка и корректировка слотов брони
            if (kit[36] != null) { // Слот ботинок
                if (!kit[36].getType().toString().contains("BOOTS")) {
                    kit[36] = null;
                }
            }
            if (kit[37] != null) { // Слот понож
                if (!kit[37].getType().toString().contains("LEGGINGS")) {
                    kit[37] = null;
                }
            }
            if (kit[38] != null) { // Слот нагрудника
                if (!(kit[38].getType().toString().contains("CHESTPLATE") || kit[38].getType().toString().contains("ELYTRA"))) {
                    kit[38] = null;
                }
            }
            if (kit[39] != null) { // Слот шлема
                if (!kit[39].getType().toString().contains("HELMET")) {
                    kit[39] = null;
                }
            }

            kitByKitIDMap.put(IDUtil.getPublicKitId(id), kit); // Сохранить кит в памяти
            return true;
        }
        return false;
    }

    public boolean saveEC(UUID uuid, int slot, ItemStack[] kit) { // Сохранить эндер-сундук
        if (Bukkit.getPlayer(uuid) != null) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                boolean notEmpty = false; // Флаг, что эндер-сундук не пустой
                for (ItemStack i : kit) { // Проверка, есть ли в эндер-сундуке предметы
                    if (i != null) {
                        if (!notEmpty) {
                            notEmpty = true;
                        }
                    }
                }

                if (notEmpty) { // Если эндер-сундук не пустой
                    kitByKitIDMap.put(IDUtil.getECId(uuid, slot), kit); // Сохранить эндер-сундук в памяти
                    player.sendMessage(ChatColor.GREEN + "Эндер-сундук " + slot + " сохранен!"); // Сообщить игроку
                    Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> saveEnderchestToDB(uuid, slot)); // Асинхронное сохранение в БД
                    return true;
                } else { // Если эндер-сундук пустой
                    player.sendMessage(ChatColor.RED + "Нельзя сохранить пустой эндер-сундук!");
                }
            }
        }
        return false;
    }

    public boolean savekit(UUID uuid, int slot, ItemStack[] kit, boolean silent) { // Сохранить кит (тихий режим)
        if (silent) { // Если тихий режим
            if (Bukkit.getPlayer(uuid) != null) {
                Player player = Bukkit.getPlayer(uuid);
                if (player != null) {
                    boolean notEmpty = false; // Флаг, что кит не пустой
                    for (ItemStack i : kit) { // Проверка, есть ли в ките предметы
                        if (i != null) {
                            if (!notEmpty) {
                                notEmpty = true;
                            }
                        }
                    }

                    if (notEmpty) { // Если кит не пустой
                        // Проверка и корректировка слотов брони
                        if (kit[36] != null) { // Слот ботинок
                            if (!kit[36].getType().toString().contains("BOOTS")) {
                                kit[36] = null;
                            }
                        }
                        if (kit[37] != null) { // Слот понож
                            if (!kit[37].getType().toString().contains("LEGGINGS")) {
                                kit[37] = null;
                            }
                        }
                        if (kit[38] != null) { // Слот нагрудника
                            if (!(kit[38].getType().toString().contains("CHESTPLATE") || kit[38].getType().toString().contains("ELYTRA"))) {
                                kit[38] = null;
                            }
                        }
                        if (kit[39] != null) { // Слот шлема
                            if (!kit[39].getType().toString().contains("HELMET")) {
                                kit[39] = null;
                            }
                        }

                        kitByKitIDMap.put(IDUtil.getPlayerKitId(uuid, slot), ItemFilter.get().filterItemStack(kit)); // Сохранить отфильтрованный кит в памяти
                        return true;
                    } else { // Если кит пустой
                        player.sendMessage(ChatColor.RED + "Нельзя сохранить пустой кит!");
                    }
                }
            }
            return false;
        } else { // Если не тихий режим, вызвать обычный savekit
            return savekit(uuid, slot, kit);
        }
    }

    public boolean regearKit(Player player, int slot) { // Пополнить кит (частичная загрузка)
        UUID uuid = player.getUniqueId();
        if (kitByKitIDMap.get(IDUtil.getPlayerKitId(uuid, slot)) == null) { // Проверить, существует ли кит
            return false;
        }

        boolean invertWhitelist = plugin.getConfig().getBoolean("regear.invert-whitelist", false); // Инвертировать белый список?
        Set<String> whitelist = new HashSet<>(plugin.getConfig().getStringList("regear.whitelist")); // Белый список предметов

        ItemStack[] kit = kitByKitIDMap.get(IDUtil.getPlayerKitId(uuid, slot)); // Получить кит
        ItemStack[] playerInventory = player.getInventory().getContents(); // Получить инвентарь игрока
        for (int i = 0; i < Math.min(playerInventory.length, kit.length); i++) { // Цикл по слотам
            if (kit[i] == null) { // Пропустить пустые слоты в ките
                continue;
            }

            if (invertWhitelist) { // Если белый список инвертирован
                if (whitelist.contains(kit[i].getType().toString())) { // Пропустить предметы из списка
                    continue;
                }
            } else { // Если обычный белый список
                if (!whitelist.contains(kit[i].getType().toString())) { // Пропустить предметы не из списка
                    continue;
                }
            }

            if (playerInventory[i] == null || playerInventory[i].getType().isAir() || playerInventory[i].getType() == kit[i].getType()) { // Если слот пустой, воздух или тот же тип
                playerInventory[i] = kit[i]; // Заполнить слот
                continue;
            }
        }
        player.getInventory().setContents(playerInventory); // Установить обновлённый инвентарь
        return true;
    }

    private boolean loadKitInternal(Player player, String kitId, String notFoundMessage, boolean isEnderChest, Runnable afterLoad) { // Внутренний метод загрузки кита
        if (player == null) { // Проверка игрока
            return false;
        }

        ItemStack[] kit = kitByKitIDMap.get(kitId); // Получить кит по ID
        if (kit == null) { // Проверить, существует ли кит
            if (notFoundMessage != null) { // Если нужно отправить сообщение
                player.sendMessage(ChatColor.RED + notFoundMessage); // Отправить сообщение об ошибке
                SoundManager.playFailure(player); // Проиграть звук неудачи
            }
            return false;
        }

        if (isEnderChest) { // Если загружается эндер-сундук
            player.getEnderChest().setContents(kit); // Установить содержимое эндер-сундука
        } else { // Иначе загружается обычный кит
            player.getInventory().setContents(kit); // Установить содержимое инвентаря
        }

        if (afterLoad != null) { // Если есть действия после загрузки
            afterLoad.run(); // Выполнить их
        }
        SoundManager.playSuccess(player); // Проиграть звук успеха
        applyKitLoadEffects(player, isEnderChest); // Применить эффекты загрузки
        return true;
    }

    public boolean loadKit(Player player, int slot) { // Загрузить кит
        return loadKitInternal(player, IDUtil.getPlayerKitId(player.getUniqueId(), slot), "Кит " + slot + " не существует!", false, () -> { // Вызов внутреннего метода
            BroadcastManager.get().broadcastPlayerLoadedPrivateKit(player); // Трансляция загрузки
            player.sendMessage(ChatColor.GREEN + "Кит " + slot + " загружен!"); // Сообщить игроку
            lastKitUsedByPlayer.put(player.getUniqueId(), slot); // Запомнить последний кит
        });
    }

    public boolean loadKitSilent(Player player, int slot) { // Загрузить кит (тихо)
        return loadKitInternal(player, IDUtil.getPlayerKitId(player.getUniqueId(), slot), null, false, null); // Вызов внутреннего метода без сообщений
    }

    public boolean loadPublicKit(Player player, String id) { // Загрузить публичный кит
        return loadKitInternal(player, IDUtil.getPublicKitId(id), "Кит не существует!", false, () -> { // Вызов внутреннего метода
            BroadcastManager.get().broadcastPlayerLoadedPublicKit(player); // Трансляция загрузки
            player.sendMessage(ChatColor.GREEN + "Публичный кит загружен!"); // Сообщить игроку
            player.sendMessage(ChatColor.GRAY + "Вы можете сохранить свою версию этого кита, импортировав его в редактор китов"); // Инструкция
        });
    }

    public boolean loadPublicKitSilent(Player player, String id) { // Загрузить публичный кит (тихо)
        return loadKitInternal(player, IDUtil.getPublicKitId(id), null, false, null); // Вызов внутреннего метода без сообщений
    }

    public boolean loadEnderchest(Player player, int slot) { // Загрузить эндер-сундук
        return loadKitInternal(player, IDUtil.getECId(player.getUniqueId(), slot), "Эндер-сундук " + slot + " не существует!", true, () -> { // Вызов внутреннего метода
            BroadcastManager.get().broadcastPlayerLoadedEnderChest(player); // Трансляция загрузки
            player.sendMessage(ChatColor.GREEN + "Эндер-сундук " + slot + " загружен!"); // Сообщить игроку
        });
    }

    public boolean loadEnderchestSilent(Player player, int slot) { // Загрузить эндер-сундук (тихо)
        return loadKitInternal(player, IDUtil.getECId(player.getUniqueId(), slot), null, true, null); // Вызов внутреннего метода без сообщений
    }

    public boolean loadLastKit(Player player) { // Загрузить последний кит
        if (lastKitUsedByPlayer.containsKey(player.getUniqueId())) { // Проверить, есть ли запись о последнем ките
            return loadKit(player, lastKitUsedByPlayer.get(player.getUniqueId())); // Загрузить его
        }
        return false;
    }

    public boolean hasKit(UUID uuid, int slot) { // Проверить, есть ли кит
        return kitByKitIDMap.get(IDUtil.getPlayerKitId(uuid, slot)) != null; // Проверка по ID
    }

    public boolean hasEC(UUID uuid, int slot) { // Проверить, есть ли эндер-сундук
        return kitByKitIDMap.get(IDUtil.getECId(uuid, slot)) != null; // Проверка по ID
    }

    public ItemStack[] getPlayerEC(UUID uuid, int slot) { // Получить эндер-сундук игрока
        return kitByKitIDMap.get(IDUtil.getECId(uuid, slot)); // Получить по ID
    }

    public ItemStack[] getPlayerKit(UUID uuid, int slot) { // Получить кит игрока
        return kitByKitIDMap.get(IDUtil.getPlayerKitId(uuid, slot)); // Получить по ID
    }

    public boolean hasPublicKit(String id) { // Проверить, есть ли публичный кит
        return kitByKitIDMap.get(IDUtil.getPublicKitId(id)) != null; // Проверка по ID
    }

    public ItemStack[] getPublicKit(String id) { // Получить публичный кит
        return kitByKitIDMap.get(IDUtil.getPublicKitId(id)); // Получить по ID
    }

    public void loadPlayerDataFromDB(UUID uuid) { // Загрузить данные игрока из БД
        for (int slot = 1; slot < 10; slot++) { // Цикл по слотам китов
            String data = PerPlayerKit.storageManager.getKitDataByID(IDUtil.getPlayerKitId(uuid, slot)); // Получить данные из БД
            if (!data.equalsIgnoreCase("error")) { // Если данные получены успешно
                try {
                    ItemStack[] kit = Serializer.itemStackArrayFromBase64(data); // Десериализовать
                    kitByKitIDMap.put(IDUtil.getPlayerKitId(uuid, slot), ItemFilter.get().filterItemStack(Serializer.itemStackArrayFromBase64(data))); // Сохранить в память
                } catch (IOException ignored) { // Игнорировать ошибки
                }
            }
        }
        for (int slot = 1; slot < 10; slot++) { // Цикл по слотам эндер-сундуков
            String data = PerPlayerKit.storageManager.getKitDataByID(IDUtil.getECId(uuid, slot)); // Получить данные из БД
            if (!data.equalsIgnoreCase("error")) { // Если данные получены успешно
                try {
                    ItemStack[] kit = Serializer.itemStackArrayFromBase64(data); // Десериализовать
                    kitByKitIDMap.put(IDUtil.getECId(uuid, slot), ItemFilter.get().filterItemStack(Serializer.itemStackArrayFromBase64(data))); // Сохранить в память
                } catch (IOException ignored) { // Игнорировать ошибки
                }
            }
        }
    }

    public void savePlayerKitsToDB(UUID uuid) { // Сохранить все киты и эндер-сундуки игрока в БД
        for (int i = 1; i < 10; i++) { // Цикл по слотам
            saveKitToDB(IDUtil.getPlayerKitId(uuid, i), true); // Сохранить кит и удалить из памяти
            saveKitToDB(IDUtil.getECId(uuid, i), true); // Сохранить эндер-сундук и удалить из памяти
        }
    }

    public void savePlayerKitToDB(UUID uuid, int slot) { // Сохранить кит игрока в БД
        saveKitToDB(IDUtil.getPlayerKitId(uuid, slot), false); // Сохранить кит, не удаляя из памяти
    }

    public void saveEnderchestToDB(UUID uuid, int slot) { // Сохранить эндер-сундук в БД
        saveKitToDB(IDUtil.getECId(uuid, slot), false); // Сохранить эндер-сундук, не удаляя из памяти
    }

    public void savePublicKitToDB(String id) { // Сохранить публичный кит в БД
        saveKitToDB(IDUtil.getPublicKitId(id), false); // Сохранить публичный кит, не удаляя из памяти
    }

    private void saveKitToDB(String key, boolean removeAfterSave) { // Внутренний метод сохранения в БД
        if (kitByKitIDMap.get(key) != null) { // Проверить, есть ли кит в памяти
            PerPlayerKit.storageManager.saveKitDataByID(key, Serializer.itemStackArrayToBase64(ItemFilter.get().filterItemStack(kitByKitIDMap.get(key)))); // Сериализовать, отфильтровать и сохранить
            if (removeAfterSave) { // Если нужно удалить из памяти
                kitByKitIDMap.remove(key); // Удалить
            }
        }
    }

    public void loadPublicKitFromDB(String id) { // Загрузить публичный кит из БД
        String data = PerPlayerKit.storageManager.getKitDataByID(IDUtil.getPublicKitId(id)); // Получить данные из БД
        if (!data.equalsIgnoreCase("error")) { // Если данные получены успешно
            try {
                ItemStack[] kit = Serializer.itemStackArrayFromBase64(data); // Десериализовать
                kitByKitIDMap.put(IDUtil.getPublicKitId(id), ItemFilter.get().filterItemStack(kit)); // Сохранить в память
            } catch (IOException ignored) { // Игнорировать ошибки
                plugin.getLogger().info("Ошибка загрузки публичного кита " + id); // Логировать ошибку
            }
        }
    }

    public boolean deleteKit(UUID uuid, int slot) { // Удалить кит
        if (hasKit(uuid, slot)) { // Проверить, существует ли кит
            kitByKitIDMap.remove(IDUtil.getPlayerKitId(uuid, slot)); // Удалить из памяти
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> PerPlayerKit.storageManager.deleteKitByID(IDUtil.getPlayerKitId(uuid, slot))); // Асинхронное удаление из БД
            return true;
        }
        return false;
    }

    public boolean deleteEnderchest(UUID uuid, int slot) { // Удалить эндер-сундук
        if (hasEC(uuid, slot)) { // Проверить, существует ли эндер-сундук
            kitByKitIDMap.remove(IDUtil.getECId(uuid, slot)); // Удалить из памяти
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> PerPlayerKit.storageManager.deleteKitByID(IDUtil.getECId(uuid, slot))); // Асинхронное удаление из БД
            return true;
        }
        return false;
    }

    private void applyKitLoadEffects(Player player, boolean isEnderChest) { // Применить эффекты загрузки кита
        if (player.isDead()) { // Не применять к мертвому игроку
            return;
        }

        if (isEnderChest) { // Если загружен эндер-сундук
            if (plugin.getConfig().getBoolean("feature.heal-on-enderchest-load", false)) { // Если включено лечение
                player.setHealth(20);
            }
            if (plugin.getConfig().getBoolean("feature.feed-on-enderchest-load", false)) { // Если включено насыщение
                player.setFoodLevel(20);
            }
            if (plugin.getConfig().getBoolean("feature.set-saturation-on-enderchest-load", false)) { // Если включено насыщение
                player.setSaturation(20);
            }
            if (plugin.getConfig().getBoolean("feature.remove-potion-effects-on-enderchest-load", false)) { // Если включено удаление эффектов
                player.getActivePotionEffects().forEach(potionEffect -> player.removePotionEffect(potionEffect.getType())); // Удалить все эффекты
            }
        } else { // Если загружен обычный кит
            if (plugin.getConfig().getBoolean("feature.set-health-on-kit-load", false)) { // Если включено лечение
                player.setHealth(20);
            }
            if (plugin.getConfig().getBoolean("feature.set-hunger-on-kit-load", false)) { // Если включено насыщение
                player.setFoodLevel(20);
            }
            if (plugin.getConfig().getBoolean("feature.set-saturation-on-kit-load", false)) { // Если включено насыщение
                player.setSaturation(20);
            }
            if (plugin.getConfig().getBoolean("feature.remove-potion-effects-on-kit-load", false)) { // Если включено удаление эффектов
                player.getActivePotionEffects().forEach(potionEffect -> player.removePotionEffect(potionEffect.getType())); // Удалить все эффекты
            }
        }
    }
}