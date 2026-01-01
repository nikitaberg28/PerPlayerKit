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
package dev.noah.perplayerkit.util;

import dev.noah.perplayerkit.PerPlayerKit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

public class PlayerUtil {

    public static void repairItem(ItemStack i) { // Починить предмет
        if (i != null) { // Если предмет существует
            ItemMeta meta = i.getItemMeta(); // Получить метаданные предмета
            Damageable damageable = (Damageable) meta; // Привести к Damageable
            if (damageable != null && damageable.hasDamage()) { // Если есть данные о прочности и есть повреждения
                damageable.setDamage(0); // Установить повреждения на 0
            }
            i.setItemMeta(damageable); // Установить изменённые метаданные обратно
        }

    }

    public static void repairAll(Player p) { // Починить всё

        for (ItemStack i : p.getInventory().getContents()) { // Цикл по всем предметам в инвентаре
            repairItem(i); // Починить предмет
        }
        p.sendMessage(ChatColor.GREEN + "Все предметы починены!"); // Отправить сообщение игроку
    }

    public static void healPlayer(Player p) { // Вылечить игрока
        p.setHealth(20); // Установить здоровье
        p.setFoodLevel(20); // Установить уровень еды
        p.setSaturation(20); // Установить насыщение

        // Удалить эффекты зелий, если настроено
        if (PerPlayerKit.getPlugin().getConfig().getBoolean("feature.heal-remove-effects", false)) { // Получить настройку из config
            p.getActivePotionEffects().forEach(potionEffect -> p.removePotionEffect(potionEffect.getType())); // Удалить каждый активный эффект
        }

        p.sendMessage(ChatColor.GREEN + "Вы были вылечены!"); // Отправить сообщение
    }

    public static void healPlayerSilent(Player p) { // Тихо вылечить игрока
        p.setHealth(20); // Установить здоровье
        p.setFoodLevel(20); // Установить еду
        p.setSaturation(20); // Установить насыщение
    }

}