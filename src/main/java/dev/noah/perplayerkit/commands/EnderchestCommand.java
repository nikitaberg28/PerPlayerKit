/*
 * Copyright 2022-2025 Noah Ross
 *
 * Этот файл является частью PerPlayerKit.
 *
 * PerPlayerKit — свободное программное обеспечение: вы можете распространять
 * и/или изменять его в соответствии с условиями GNU Affero General Public License,
 * опубликованной Free Software Foundation, либо версии 3 Лицензии, либо (по вашему
 * выбору) любой более поздней версии.
 *
 * PerPlayerKit распространяется в надежде, что он будет полезен, но БЕЗ КАКОЙ-ЛИБО
 * ГАРАНТИИ; даже без подразумеваемой гарантии ТОВАРНОГО ВИДА или ПРИГОДНОСТИ ДЛЯ
 * ОПРЕДЕЛЕННОЙ ЦЕЛИ. Для получения дополнительных сведений см. GNU Affero General Public License.
 *
 * Вы должны были получить копию GNU Affero General Public License вместе с PerPlayerKit.
 * Если это не так, см. <https://www.gnu.org/licenses/>.
 */
package dev.noah.perplayerkit.commands;

import dev.noah.perplayerkit.gui.ItemUtil;           // Утилита для создания предметов GUI
import dev.noah.perplayerkit.util.DisabledCommand;  // Утилита для проверки, разрешена ли команда в мире
import dev.noah.perplayerkit.util.StyleManager;     // Утилита для управления стилями (например, цветами)
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import dev.noah.perplayerkit.util.SoundManager;     // Утилита для воспроизведения звуков
import org.bukkit.inventory.ItemStack;               // Представление предмета с количеством, метаданными и т.д.
import org.ipvp.canvas.Menu;                        // Библиотека для создания GUI (меню)
import org.ipvp.canvas.type.ChestMenu;              // Тип GUI - сундук
import org.jetbrains.annotations.NotNull;

// Команда для просмотра эндер-сундука игрока в режиме "только для чтения"
public class EnderchestCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        // Проверяем, является ли отправитель команды игроком
        if (sender instanceof Player player) {

            // Проверяем, заблокирована ли команда в мире, где находится игрок
            if (DisabledCommand.isBlockedInWorld(player)) {
                return true; // Если команда заблокирована, просто завершаем обработку
            }

            // Открываем GUI с содержимым эндер-сундука игрока
            viewOnlyEC(player);
            return true;
        }

        // Если команда была выполнена не игроком (например, консолью), отправляем сообщение
        sender.sendMessage("Только игроки могут использовать эту команду");
        // Если отправитель - игрок (хотя код сюда не должен попасть), проигрываем звук неудачи
        if (sender instanceof Player s) SoundManager.playFailure(s);
        return true;
    }

    // Метод для открытия GUI с содержимым эндер-сундука
    public void viewOnlyEC(Player p) {
        // Создаём предмет-заполнитель (стеклянная панель)
        ItemStack fill = ItemUtil.createGlassPane();

        // Создаём меню в виде сундука на 5 строк с заголовком
        Menu menu = ChestMenu.builder(5)
                .title(StyleManager.get().getPrimaryColor() + "Просмотр эндер-сундука (только чтение)")
                .build();

        // Заполняем первую и последнюю строки меню стеклянными панелями
        for (int i = 0; i < 9; i++) {
            menu.getSlot(i).setItem(fill); // Первая строка (слоты 0-8)
        }
        for (int i = 36; i < 45; i++) {
            menu.getSlot(i).setItem(fill); // Последняя строка (слоты 36-44)
        }

        // Получаем содержимое эндер-сундука игрока
        ItemStack[] items = p.getEnderChest().getContents();

        // Заполняем средние строки (3-я, 4-я, 5-я) содержимым эндер-сундука
        // Слоты 9-35 в GUI соответствуют 27 слотам эндер-сундука
        for (int i = 0; i < 27; i++) {
            menu.getSlot(i + 9).setItem(items[i]); // Предметы эндер-сундука в слоты GUI с 9 по 35
        }

        // Открываем созданное меню игроку
        menu.open(p);

        // Проигрываем звук открытия GUI
        SoundManager.playOpenGui(p);
    }
}