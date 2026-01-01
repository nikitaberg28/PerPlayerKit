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

import dev.noah.perplayerkit.gui.ItemUtil;
import dev.noah.perplayerkit.util.IDUtil;
import dev.noah.perplayerkit.util.Serializer;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.IOException;
import java.util.ArrayList;

public class KitRoomDataManager { // Менеджер данных комнаты китов


    private final ArrayList<ItemStack[]> kitroomData; // Данные комнаты китов (список страниц)
    private final Plugin plugin;
    private static KitRoomDataManager instance;

    public KitRoomDataManager(Plugin plugin) {
        this.plugin = plugin;
        kitroomData = new ArrayList<>(); // Инициализация списка


        ItemStack[] defaultPage = new ItemStack[45]; // Стандартная страница (45 слотов)
        defaultPage[0] = ItemUtil.createItem(Material.BLUE_STAINED_GLASS_PANE, "<aqua>Стандартный предмет комнаты китов</aqua>"); // Создание стандартного предмета
        kitroomData.add(defaultPage); // Добавление стандартной страницы 5 раз
        kitroomData.add(defaultPage);
        kitroomData.add(defaultPage);
        kitroomData.add(defaultPage);
        kitroomData.add(defaultPage);

        ItemFilter.get().addToWhitelist(kitroomData); // Добавление предметов в белый список фильтра

        instance = this;
    }

    public static KitRoomDataManager get(){ // Получить экземпляр менеджера
        if(instance == null){ // Проверка инициализации
            throw new IllegalStateException("KitRoomDataManager еще не инициализирован!");
        }
        return instance;
    }

    public void setKitRoom(int page, ItemStack[] data) { // Установить страницу комнаты китов
        kitroomData.set(page, data); // Установка данных страницы

        ItemFilter.get().clearWhitelist(); // Очистка белого списка фильтра

        ItemFilter.get().addToWhitelist(kitroomData); // Добавление обновленных предметов в белый список

    }

    public ItemStack[] getKitRoomPage(int page) { // Получить страницу комнаты китов
        return kitroomData.get(page); // Возврат данных страницы
    }

    public void saveToDBAsync() { // Асинхронное сохранение в БД
        new BukkitRunnable() { // Запуск асинхронной задачи

            @Override
            public void run() { // Выполнение задачи

                for (int i = 0; i < 5; i++) { // Цикл по 5 страницам
                    ItemStack[] pagedata = kitroomData.get(i); // Получение данных страницы
                    String output = Serializer.itemStackArrayToBase64(pagedata); // Сериализация в строку
                    PerPlayerKit.storageManager.saveKitDataByID(IDUtil.getKitRoomId(i), output); // Сохранение в БД
                }
            }

        }.runTaskAsynchronously(plugin); // Запуск задачи асинхронно


    }

    public void loadFromDB() { // Загрузка из БД
        ItemFilter.get().clearWhitelist(); // Очистка белого списка фильтра
        for (int i = 0; i < 5; i++) { // Цикл по 5 страницам
            String input = PerPlayerKit.storageManager.getKitDataByID(IDUtil.getKitRoomId(i)); // Получение данных из БД
            if (!input.equalsIgnoreCase("error")) { // Проверка на ошибку
                try {
                    ItemStack[] pagedata = Serializer.itemStackArrayFromBase64(input); // Десериализация
                    kitroomData.set(i, pagedata); // Установка данных страницы

                } catch (IOException e) { // Обработка ошибки десериализации
                    e.printStackTrace(); // Вывод стека ошибки
                }
            }
        }
        ItemFilter.get().addToWhitelist(kitroomData); // Добавление загруженных предметов в белый список
    }

}