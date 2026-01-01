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

import dev.noah.perplayerkit.KitManager;        // Менеджер китов и эндер-сундуков
import dev.noah.perplayerkit.gui.GUI;           // Класс для работы с графическим интерфейсом
import dev.noah.perplayerkit.util.BroadcastManager; // Утилита для отправки сообщений
import dev.noah.perplayerkit.util.SoundManager;     // Утилита для воспроизведения звуков

import org.bukkit.Bukkit;                        // Основной класс API Bukkit
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;       // Интерфейс для обработки команд
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;          // Интерфейс для автодополнения команд
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;                 // Представление плагина
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;  // Для асинхронного выполнения
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static dev.noah.perplayerkit.commands.InspectCommandUtil.*; // Импорт статических полей и методов из утилиты

// Команда для просмотра чужого кита (только для модераторов)
public class InspectKitCommand implements CommandExecutor, TabCompleter {
    private final Plugin plugin; // Ссылка на экземпляр плагина

    public InspectKitCommand(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        // Проверяем, является ли отправитель команды игроком
        if (!(sender instanceof Player player)) {
            // Если нет, отправляем сообщение об ошибке
            sender.sendMessage(ERROR_PREFIX.append(
                    mm.deserialize("<red>Эту команду могут выполнить только игроки.</red>")).toString());
            return true;
        }

        // Проверяем, есть ли у игрока разрешение на использование команды
        if (!player.hasPermission("perplayerkit.inspect")) {
            BroadcastManager.get().sendComponentMessage(player,
                    ERROR_PREFIX.append(
                            mm.deserialize("<red>У вас нет разрешения на использование этой команды.</red>")));
            SoundManager.playFailure(player);
            return true;
        }

        // Проверяем, передано ли нужное количество аргументов (имя/uuid игрока и слот)
        if (args.length < 2) {
            // Если нет, показываем правильное использование команды
            showUsage(player, "inspectkit"); // "inspectkit" - название команды просмотра кита
            return true;
        }

        // Парсим номер слота
        int slot;
        try {
            slot = Integer.parseInt(args[1]); // args[1] - это номер слота
            // Проверяем, входит ли слот в допустимый диапазон (MIN_SLOT - MAX_SLOT, обычно 1-9)
            if (slot < MIN_SLOT || slot > MAX_SLOT) {
                throw new NumberFormatException(); // Выбрасываем исключение, если слот вне диапазона
            }
        } catch (NumberFormatException e) {
            // Если аргумент не является числом или вне диапазона
            BroadcastManager.get().sendComponentMessage(player,
                    ERROR_PREFIX.append(
                            mm.deserialize("<red>Слот должен быть числом от " +
                                    MIN_SLOT + " до " + MAX_SLOT + ".</red>")));
            SoundManager.playFailure(player);
            return true;
        }

        // Асинхронно разрешаем идентификатор игрока (имя -> UUID)
        CompletableFuture<Void> future = resolvePlayerIdentifierAsync(args[0]) // args[0] - имя или UUID игрока
                .thenCompose(targetUuid -> { // thenCompose используется для цепочки асинхронных операций
                    if (targetUuid == null) {
                        // Если игрок не найден
                        // Планируем отправку сообщения об ошибке в основном потоке Bukkit
                        Bukkit.getScheduler().runTask(plugin, () -> {
                            BroadcastManager.get().sendComponentMessage(player,
                                    ERROR_PREFIX.append(
                                            mm.deserialize("<red>Не удалось найти игрока с таким именем или UUID.</red>")));
                            SoundManager.playFailure(player);
                        });
                        return CompletableFuture.completedFuture(null); // Завершаем цепочку
                    }

                    // Проверяем, онлайн ли целевой игрок
                    Player targetPlayer = Bukkit.getPlayer(targetUuid);

                    // Асинхронно загружаем данные игрока из базы данных, если он оффлайн
                    return CompletableFuture.runAsync(() -> {
                        if (targetPlayer == null) {
                            // Загружаем данные только если игрок оффлайн
                            KitManager.get().loadPlayerDataFromDB(targetUuid);
                        }
                    }).thenRun(() -> { // После загрузки данных
                        // Планируем выполнение в основном потоке Bukkit
                        Bukkit.getScheduler().runTask(plugin, () -> {
                            // Проверяем, есть ли у целевого игрока кит в указанном слоте
                            if (KitManager.get().hasKit(targetUuid, slot)) {
                                // Если есть, открываем GUI для просмотра
                                GUI gui = new GUI(plugin);
                                gui.InspectKit(player, targetUuid, slot); // Метод открытия GUI просмотра
                            } else {
                                // Если нет, отправляем сообщение об ошибке
                                String targetName = getPlayerName(targetUuid); // Получаем имя целевого игрока

                                BroadcastManager.get().sendComponentMessage(player,
                                        ERROR_PREFIX.append(
                                                mm.deserialize("<red>" + targetName +
                                                        " не имеет кита в слоте " + slot + "</red>")));
                                SoundManager.playFailure(player);
                            }
                        });
                    });
                });

        // Обработка исключений, которые могли возникнуть в цепочке асинхронных операций
        future.exceptionally(ex -> {
            // Планируем отправку сообщения об ошибке в основном потоке Bukkit
            Bukkit.getScheduler().runTask(plugin, () -> {
                plugin.getLogger().severe("Ошибка при загрузке данных кита: " + ex.getMessage());
                BroadcastManager.get().sendComponentMessage(player,
                        ERROR_PREFIX.append(
                                mm.deserialize("<red>Произошла ошибка при загрузке данных кита. " +
                                        "См. консоль для подробностей.</red>")));
                SoundManager.playFailure(player);
            });
            return null;
        });

        return true;
    }

    // Метод для автодополнения команды (TabCompleter)
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender,
                                                @NotNull Command command,
                                                @NotNull String label,
                                                @NotNull String[] args) {
        // Проверяем, является ли отправитель игроком и имеет ли он разрешение
        if (!(sender instanceof Player) || !sender.hasPermission("perplayerkit.inspect")) {
            return List.of(); // Возвращаем пустой список, если нет прав
        }

        if (args.length == 1) {
            // Автодополнение для первого аргумента (имя или UUID игрока)
            String input = args[0].toLowerCase();

            // Добавляем имена онлайн-игроков
            List<String> completions = new ArrayList<>(Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName) // Получаем имена онлайн-игроков
                    .filter(name -> name.toLowerCase().startsWith(input)) // Фильтруем по введённому тексту
                    .toList());

            // Добавляем UUID, если введённый текст похож на UUID
            if (input.length() >= 4 && input.contains("-")) {
                completions.addAll(Bukkit.getOnlinePlayers().stream()
                        .map(Player::getUniqueId) // Получаем UUID онлайн-игроков
                        .map(UUID::toString)
                        .filter(uuid -> uuid.startsWith(input)) // Фильтруем по введённому тексту
                        .toList());
            }

            return completions;
        } else if (args.length == 2) {
            // Автодополнение для второго аргумента (номер слота)
            return IntStream.rangeClosed(MIN_SLOT, MAX_SLOT) // Создаём поток чисел от MIN_SLOT до MAX_SLOT
                    .mapToObj(String::valueOf) // Преобразуем числа в строки
                    .filter(slot -> slot.startsWith(args[1])) // Фильтруем по введённому тексту
                    .collect(Collectors.toList()); // Собираем в список
        }

        return new ArrayList<>(); // Возвращаем пустой список в других случаях
    }
}