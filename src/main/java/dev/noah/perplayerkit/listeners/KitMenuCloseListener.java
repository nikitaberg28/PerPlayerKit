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

import dev.noah.perplayerkit.KitManager;
import dev.noah.perplayerkit.gui.GUI;
import dev.noah.perplayerkit.util.StyleManager;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class KitMenuCloseListener implements Listener {

    @EventHandler
    public void onKitEditorClose(InventoryCloseEvent e) { // При закрытии редактора кита
        Inventory inv = e.getInventory();
        if (inv.getSize() == 54) { // Проверка размера инвентаря
            if (inv.getLocation() == null) { // Проверка, что инвентарь не является частью мира (например, сундук в мире)
                InventoryView view = e.getView();
                if (view.getTitle().contains(StyleManager.get().getPrimaryColor() + "Кит: ")) { // Проверка заголовка
                    Player p = (Player) e.getPlayer(); // Игрок, закрывающий инвентарь
                    UUID uuid = p.getUniqueId(); // UUID игрока
                    int slot = Integer.parseInt(view.getTitle().replace(StyleManager.get().getPrimaryColor() + "Кит: ", "")); // Номер слота кита
                    ItemStack[] kit = new ItemStack[41]; // Массив предметов кита
                    ItemStack[] chestitems = e.getInventory().getContents(); // Получить содержимое инвентаря

                    for (int i = 0; i < 41; i++) { // Цикл для копирования предметов
                        if (chestitems[i] == null) {
                            kit[i] = null;
                        } else {
                            kit[i] = chestitems[i].clone(); // Клонировать предмет
                        }
                    }
                    KitManager.get().savekit(uuid, slot, kit); // Сохранить кит
                }
            }
        }
    }

    @EventHandler
    public void onPublicKitEditorClose(InventoryCloseEvent e) { // При закрытии редактора публичного кита
        Inventory inv = e.getInventory();
        if (inv.getSize() == 54) { // Проверка размера инвентаря
            if (inv.getLocation() == null) { // Проверка, что инвентарь не является частью мира
                InventoryView view = e.getView();
                if (view.getTitle().contains(StyleManager.get().getPrimaryColor() + "Публичный кит: ")) { // Проверка заголовка
                    Player player = (Player) e.getPlayer(); // Игрок
                    String publickit = view.getTitle().replace(StyleManager.get().getPrimaryColor() + "Публичный кит: ", ""); // ID публичного кита
                    ItemStack[] kit = new ItemStack[41]; // Массив предметов
                    ItemStack[] chestitems = e.getInventory().getContents(); // Содержимое

                    for (int i = 0; i < 41; i++) { // Цикл копирования
                        if (chestitems[i] == null) {
                            kit[i] = null;
                        } else {
                            kit[i] = chestitems[i].clone();
                        }
                    }
                    KitManager.get().savePublicKit(player, publickit, kit); // Сохранить публичный кит
                }
            }
        }
    }

    @EventHandler
    public void onEnderchestEditorClose(InventoryCloseEvent e) { // При закрытии редактора эндер-сундука
        Inventory inv = e.getInventory();
        if (inv.getSize() == 54) { // Проверка размера
            if (inv.getLocation() == null) { // Проверка локации
                InventoryView view = e.getView();
                if (view.getTitle().contains(StyleManager.get().getPrimaryColor() + "Эндер-сундук: ")) { // Проверка заголовка
                    Player p = (Player) e.getPlayer(); // Игрок
                    UUID uuid = p.getUniqueId(); // UUID
                    int slot = Integer.parseInt(view.getTitle().replace(StyleManager.get().getPrimaryColor() + "Эндер-сундук: ", "")); // Слот
                    ItemStack[] kit = new ItemStack[27]; // Массив для 27 слотов
                    ItemStack[] chestitems = e.getInventory().getContents(); // Содержимое

                    for (int i = 0; i < 27; i++) { // Цикл, начиная с 9-го слота (0-8 - стеклянные панели)
                        if (chestitems[i + 9] == null) {
                            kit[i] = null;
                        } else {
                            kit[i] = chestitems[i + 9].clone(); // Клонировать предмет из слота i+9
                        }
                    }
                    KitManager.get().saveEC(uuid, slot, kit); // Сохранить эндер-сундук
                }
            }
        }
    }

    @EventHandler
    public void onInspectKitEditorClose(InventoryCloseEvent e) { // При закрытии просмотра/редактирования чужого кита
        Inventory inv = e.getInventory();
        if (inv.getSize() == 54) { // Проверка размера
            if (inv.getLocation() == null) { // Проверка локации
                InventoryView view = e.getView();
                if (view.getTitle().contains(StyleManager.get().getPrimaryColor() + "Просмотр кита ") && view.getTitle().contains("'s kit ")) { // Проверка заголовка "Inspecting ...'s kit "
                    Player p = (Player) e.getPlayer(); // Администратор
                    if (!p.hasPermission("perplayerkit.admin")) { // Проверка прав
                        return;
                    }
                    String title = view.getTitle();
                    String[] parts = title.replace(StyleManager.get().getPrimaryColor() + "Просмотр кита ", "").split("'s kit "); // Разделить заголовок
                    if (parts.length != 2) { // Должно получиться 2 части
                        return;
                    }
                    String playerName = parts[0]; // Имя игрока
                    int slot; // Слот кита
                    try {
                        slot = Integer.parseInt(parts[1]); // Преобразовать слот в число
                    } catch (NumberFormatException ex) { // Ошибка, если не число
                        return;
                    }

                    UUID targetUuid = null; // UUID цели
                    for (OfflinePlayer offlinePlayer : Bukkit.getOfflinePlayers()) { // Поиск среди оффлайн-игроков
                        if (playerName.equalsIgnoreCase(offlinePlayer.getName())) {
                            targetUuid = offlinePlayer.getUniqueId();
                            break;
                        }
                    }
                    if (targetUuid == null) { // Если не найден, ищем среди онлайн-игроков
                        Player onlinePlayer = Bukkit.getPlayerExact(playerName);
                        if (onlinePlayer != null) {
                            targetUuid = onlinePlayer.getUniqueId();
                        }
                    }
                    if (targetUuid == null) { // Если все еще не найден
                        p.sendMessage(ChatColor.RED + "Не удалось найти игрока " + playerName);
                        return;
                    }

                    if (GUI.removeKitDeletionFlag(p)) { // Проверить флаг удаления
                        return; // Если флаг был, выйти (не сохранять)
                    }

                    ItemStack[] kit = new ItemStack[41]; // Массив предметов
                    ItemStack[] chestitems = e.getInventory().getContents(); // Содержимое

                    for (int i = 0; i < 41; i++) { // Цикл копирования
                        if (chestitems[i] == null) {
                            kit[i] = null;
                        } else {
                            kit[i] = chestitems[i].clone();
                        }
                    }

                    if (KitManager.get().savekit(targetUuid, slot, kit, true)) { // Сохранить кит для цели (с флагом админа)
                        p.sendMessage(ChatColor.GREEN + "Кит " + slot + " обновлен для игрока " + playerName + "!"); // Сообщить об успехе
                    } else {
                        p.sendMessage(ChatColor.RED + "Не удалось обновить кит для игрока " + playerName + "!"); // Сообщить о неудаче
                    }
                }
            }
        }
    }

    @EventHandler
    public void onInspectEnderchestEditorClose(InventoryCloseEvent e) { // При закрытии просмотра/редактирования чужого эндер-сундука
        Inventory inv = e.getInventory();
        if (inv.getSize() == 54) { // Проверка размера
            if (inv.getLocation() == null) { // Проверка локации
                InventoryView view = e.getView();
                if (view.getTitle().contains(StyleManager.get().getPrimaryColor() + "Просмотр эндер-сундука ") && view.getTitle().contains("'s enderchest ")) { // Проверка заголовка "Inspecting ...'s enderchest "
                    Player p = (Player) e.getPlayer(); // Администратор
                    if (!p.hasPermission("perplayerkit.admin")) { // Проверка прав
                        return;
                    }
                    String title = view.getTitle();
                    String[] parts = title.replace(StyleManager.get().getPrimaryColor() + "Просмотр эндер-сундука ", "").split("'s enderchest "); // Разделить заголовок
                    if (parts.length != 2) { // Должно получиться 2 части
                        return;
                    }
                    String playerName = parts[0]; // Имя игрока
                    int slot; // Слот эндер-сундука
                    try {
                        slot = Integer.parseInt(parts[1]); // Преобразовать слот в число
                    } catch (NumberFormatException ex) { // Ошибка, если не число
                        return;
                    }

                    UUID targetUuid = null; // UUID цели
                    for (OfflinePlayer offlinePlayer : Bukkit.getOfflinePlayers()) { // Поиск среди оффлайн-игроков
                        if (playerName.equalsIgnoreCase(offlinePlayer.getName())) {
                            targetUuid = offlinePlayer.getUniqueId();
                            break;
                        }
                    }
                    if (targetUuid == null) { // Если не найден, ищем среди онлайн-игроков
                        Player onlinePlayer = Bukkit.getPlayerExact(playerName);
                        if (onlinePlayer != null) {
                            targetUuid = onlinePlayer.getUniqueId();
                        }
                    }
                    if (targetUuid == null) { // Если все еще не найден
                        p.sendMessage(ChatColor.RED + "Не удалось найти игрока " + playerName);
                        return;
                    }

                    if (GUI.removeKitDeletionFlag(p)) { // Проверить флаг удаления
                        return; // Если флаг был, выйти (не сохранять)
                    }

                    ItemStack[] kit = new ItemStack[27]; // Массив для 27 слотов
                    ItemStack[] chestitems = e.getInventory().getContents(); // Содержимое

                    for (int i = 0; i < 27; i++) { // Цикл, начиная с 9-го слота
                        if (chestitems[i + 9] == null) {
                            kit[i] = null;
                        } else {
                            kit[i] = chestitems[i + 9].clone(); // Клонировать предмет
                        }
                    }

                    if (KitManager.get().saveEC(targetUuid, slot, kit)) { // Сохранить эндер-сундук для цели
                        p.sendMessage(ChatColor.GREEN + "Эндер-сундук " + slot + " обновлен для игрока " + playerName + "!"); // Сообщить об успехе
                    } else {
                        p.sendMessage(ChatColor.RED + "Не удалось обновить эндер-сундук для игрока " + playerName + "!"); // Сообщить о неудаче
                    }
                }
            }
        }
    }
}