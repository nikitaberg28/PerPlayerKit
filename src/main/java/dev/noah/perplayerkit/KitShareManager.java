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
import org.apache.commons.lang3.RandomStringUtils;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import dev.noah.perplayerkit.util.SoundManager;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class KitShareManager { // Менеджер обмена китами


    public static HashMap<String, ItemStack[]> kitShareMap; // Карта обмена китами (ID -> кит)
    private static KitShareManager instance;
    private final Plugin plugin;

    public KitShareManager(Plugin plugin) {
        this.plugin = plugin;
        kitShareMap = new HashMap<>(); // Инициализация карты
        instance = this;
    }

    public static KitShareManager get() { // Получить экземпляр менеджера
        if (instance == null) {
            throw new IllegalStateException("KitShareManager не инициализирован");
        }
        return instance;
    }

    public List<String> getKitSlots(Player p) { // Получить слоты с китами
        ArrayList<String> slots = new ArrayList<>(); // Список слотов
        for (int i = 1; i <= 9; i++) { // Цикл по слотам 1-9
            if (KitManager.get().hasKit(p.getUniqueId(), i)) { // Проверить, есть ли кит в слоте
                slots.add(String.valueOf(i)); // Добавить номер слота в список
            }
        }
        return slots; // Вернуть список
    }

    public List<String> getECSlots(Player p) { // Получить слоты с эндер-сундуками
        ArrayList<String> slots = new ArrayList<>(); // Список слотов
        for (int i = 1; i <= 9; i++) { // Цикл по слотам 1-9
            if (KitManager.get().hasEC(p.getUniqueId(), i)) { // Проверить, есть ли эндер-сундук в слоте
                slots.add(String.valueOf(i)); // Добавить номер слота в список
            }
        }
        return slots; // Вернуть список
    }

    public void shareKit(Player p, int slot) { // Поделиться китом
        UUID uuid = p.getUniqueId(); // UUID игрока
        KitManager kitManager = KitManager.get(); // Менеджер китов
        if (kitManager.hasKit(uuid, slot)) { // Проверить, есть ли кит в слоте
            String id = RandomStringUtils.randomAlphanumeric(6).toUpperCase(); // Сгенерировать ID

            if (kitShareMap.putIfAbsent(id, kitManager.getPlayerKit(uuid, slot).clone()) == null) { // Попытаться добавить кит в карту
                p.sendMessage(ChatColor.GREEN + "Используйте /copykit " + id + " чтобы скопировать этот кит"); // Сообщить игроку
                p.sendMessage(ChatColor.GREEN + "Код действителен 15 минут"); // Сообщить о времени действия
                SoundManager.playSuccess(p); // Проиграть звук успеха


                new BukkitRunnable() { // Запуск отложенной задачи

                    @Override
                    public void run() { // Выполнение задачи
                        kitShareMap.remove(id); // Удалить кит по истечении времени
                    }

                }.runTaskLater(plugin, 15 * 60 * 20); // Запуск задачи через 15 минут (в тиках)


            } else { // Если не удалось добавить (например, ID уже занят)
                p.sendMessage(ChatColor.RED + "Произошла непредвиденная ошибка, повторите попытку."); // Сообщить об ошибке
                SoundManager.playFailure(p); // Проиграть звук неудачи
            }

        } else { // Если кита в слоте не существует
            p.sendMessage(ChatColor.RED + "Ошибка, такого кита не существует"); // Сообщить об ошибке
            SoundManager.playFailure(p); // Проиграть звук неудачи
        }

    }


    public void shareEC(Player p, int slot) { // Поделиться эндер-сундуком
        UUID uuid = p.getUniqueId(); // UUID игрока
        KitManager kitManager = KitManager.get(); // Менеджер китов
        if (kitManager.hasEC(uuid, slot)) { // Проверить, есть ли эндер-сундук в слоте
            String id = RandomStringUtils.randomAlphanumeric(6).toUpperCase(); // Сгенерировать ID

            if (kitShareMap.putIfAbsent(id, kitManager.getPlayerEC(uuid, slot).clone()) == null) { // Попытаться добавить эндер-сундук в карту
                p.sendMessage(ChatColor.GREEN + "Используйте /copyEC " + id + " чтобы скопировать этот эндер-сундук"); // Сообщить игроку
                p.sendMessage(ChatColor.GREEN + "Код действителен 15 минут"); // Сообщить о времени действия
                SoundManager.playSuccess(p); // Проиграть звук успеха


                new BukkitRunnable() { // Запуск отложенной задачи

                    @Override
                    public void run() { // Выполнение задачи
                        kitShareMap.remove(id); // Удалить эндер-сундук по истечении времени
                    }

                }.runTaskLater(plugin, 15 * 60 * 20); // Запуск задачи через 15 минут (в тиках)


            } else { // Если не удалось добавить (например, ID уже занят)
                p.sendMessage(ChatColor.RED + "Произошла непредвиденная ошибка, повторите попытку."); // Сообщить об ошибке
                SoundManager.playFailure(p); // Проиграть звук неудачи
            }

        } else { // Если эндер-сундука в слоте не существует
            p.sendMessage(ChatColor.RED + "Ошибка, такого эндер-сундука не существует"); // Сообщить об ошибке
            SoundManager.playFailure(p); // Проиграть звук неудачи
        }

    }


    public void copyKit(Player p, String str) { // Скопировать кит

        String id = str.toUpperCase(); // Привести ID к верхнему регистру
        if (!kitShareMap.containsKey(id)) { // Проверить, существует ли кит с таким ID
            p.sendMessage(ChatColor.RED + "Ошибка, кит не существует или срок действия кода истек"); // Сообщить об ошибке
            SoundManager.playFailure(p); // Проиграть звук неудачи
            return; // Выйти из метода
        }

        ItemStack[] data = kitShareMap.get(id); // Получить данные кита

        if (data.length == 27) { // Если длина массива 27, это эндер-сундук
            // эндер-сундук
            p.getEnderChest().setContents(kitShareMap.get(id)); // Установить содержимое эндер-сундука
            BroadcastManager.get().broadcastPlayerCopiedEC(p); // Трансляция события
            SoundManager.playSuccess(p); // Проиграть звук успеха

        } else if (data.length == 41) { // Если длина массива 41, это инвентарь
            // инвентарь

            p.getInventory().setContents(kitShareMap.get(id)); // Установить содержимое инвентаря
            BroadcastManager.get().broadcastPlayerCopiedKit(p); // Трансляция события
            SoundManager.playSuccess(p); // Проиграть звук успеха
        } else { // Если длина не 27 и не 41 - ошибка
            p.sendMessage(ChatColor.RED + "Произошла непредвиденная ошибка, повторите попытку."); // Сообщить об ошибке
            SoundManager.playFailure(p); // Проиграть звук неудачи
        }


    }


}