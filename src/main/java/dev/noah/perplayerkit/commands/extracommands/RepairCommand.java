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
package dev.noah.perplayerkit.commands.extracommands;

import dev.noah.perplayerkit.util.BroadcastManager;
import dev.noah.perplayerkit.util.PlayerUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import dev.noah.perplayerkit.util.SoundManager;
import org.jetbrains.annotations.NotNull;

// Команда для починки всего снаряжения игрока
public class RepairCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        // Проверяем, является ли отправитель команды игроком
        if(!(sender instanceof Player player)){
            sender.sendMessage("Только игроки могут использовать эту команду!");
            return true;
        }

        // Отправляем сообщение о том, что игрок починил своё снаряжение
        BroadcastManager.get().broadcastPlayerRepaired(player);

        // Выполняем починку всего снаряжения игрока
        PlayerUtil.repairAll(player);

        // Проигрываем звук успешного действия
        SoundManager.playSuccess(player);

        return true;
    }
}