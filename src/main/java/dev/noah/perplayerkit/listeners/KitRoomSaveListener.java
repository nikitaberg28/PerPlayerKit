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
package dev.noah.perplayerkit.listeners;

import dev.noah.perplayerkit.KitRoomDataManager;
import dev.noah.perplayerkit.util.StyleManager;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

public class KitRoomSaveListener implements Listener { // Слушатель сохранения комнаты китов

    @EventHandler
    public void onSaveButtonClick(InventoryClickEvent e) { // При нажатии на кнопку сохранения
        if (e.getClick().isShiftClick() && e.getClick().isRightClick()) { // Проверка: Shift + Правый клик
            Inventory inv = e.getInventory();
            if (inv.getSize() == 54) { // Проверка размера инвентаря
                if (inv.getLocation() == null) { // Проверка, что инвентарь не в мире
                    InventoryView view = e.getView();
                    Player p = (Player) e.getWhoClicked(); // Игрок, кликнувший по инвентарю

                    if (view.getTitle().contains(StyleManager.get().getPrimaryColor() + p.getName() + "'s Kits")) { // Проверка заголовка меню (должно быть меню китов игрока)
                        ItemStack saveButton = e.getInventory().getItem(53); // Предмет в слоте 53 (кнопка сохранения)
                        if (saveButton != null && saveButton.getType() == Material.BARRIER) { // Проверка, что это предмет-барьер (кнопка сохранения)
                            if (e.getSlot() == 53) { // Проверка, что клик был по слоту 53
                                if (p.hasPermission("perplayerkit.editkitroom") || p.isOp()) { // Проверка прав администратора

                                    // Безопасное получение номера страницы из количества предметов в кнопке
                                    int page = (saveButton.getAmount() > 0) ? saveButton.getAmount() - 1 : 0;

                                    // Инициализация массива комнаты китов
                                    ItemStack[] kitroom = new ItemStack[45]; // Массив для 45 предметов

                                    for (int i = 0; i < 45; i++) { // Цикл по первым 45 слотам
                                        ItemStack item = e.getInventory().getItem(i); // Получить предмет из слота i
                                        kitroom[i] = (item != null) ? item.clone() : null; // Клонировать предмет или присвоить null
                                    }

                                    // Сохранение данных комнаты китов
                                    KitRoomDataManager.get().setKitRoom(page, kitroom); // Установить содержимое страницы
                                    KitRoomDataManager.get().saveToDBAsync(); // Асинхронное сохранение в БД
                                    p.sendMessage(ChatColor.GREEN + "Сохранена страница комнаты китов: " + (page + 1)); // Отправить сообщение об успехе
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}