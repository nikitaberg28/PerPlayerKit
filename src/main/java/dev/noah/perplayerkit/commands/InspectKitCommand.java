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
package dev.noah.perplayerkit.commands;

import dev.noah.perplayerkit.KitManager;
import dev.noah.perplayerkit.gui.GUI;
import dev.noah.perplayerkit.util.BroadcastManager;
import dev.noah.perplayerkit.util.SoundManager;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static dev.noah.perplayerkit.commands.InspectCommandUtil.*;

public class InspectKitCommand implements CommandExecutor, TabCompleter {
    private final Plugin plugin;

    public InspectKitCommand(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ERROR_PREFIX.append(
                    mm.deserialize("<red>Эту команду могут выполнять только игроки.</red>")).toString());
            return true;
        }

        if (!player.hasPermission("perplayerkit.inspect")) {
            BroadcastManager.get().sendComponentMessage(player,
                    ERROR_PREFIX.append(
                            mm.deserialize("<red>У вас нет прав на использование этой команды.</red>")));
            SoundManager.playFailure(player);
            return true;
        }

        if (args.length < 2) {
            showUsage(player, "inspectkit");
            return true;
        }

        // Парсинг номера слота
        int slot;
        try {
            slot = Integer.parseInt(args[1]);
            if (slot < MIN_SLOT || slot > MAX_SLOT) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            BroadcastManager.get().sendComponentMessage(player,
                    ERROR_PREFIX.append(
                            mm.deserialize("<red>Слот должен быть числом от " +
                                    MIN_SLOT + " и " + MAX_SLOT + ".</red>")));
            SoundManager.playFailure(player);
            return true;
        }

        // Асинхронное получение идентификатора игрока
        CompletableFuture<Void> future = resolvePlayerIdentifierAsync(args[0])
                .thenCompose(targetUuid -> {
                    if (targetUuid == null) {
                        // Игрок не найден - планируем сообщение об ошибке в основном потоке
                        Bukkit.getScheduler().runTask(plugin, () -> {
                            BroadcastManager.get().sendComponentMessage(player,
                                    ERROR_PREFIX.append(
                                            mm.deserialize("<red>Не удалось найти игрока с таким именем или UUID.</red>")));
                            SoundManager.playFailure(player);
                        });
                        return CompletableFuture.completedFuture(null);
                    }

                    // Сначала проверяем, онлайн ли игрок
                    Player targetPlayer = Bukkit.getPlayer(targetUuid);

                    // Асинхронная загрузка данных игрока
                    return CompletableFuture.runAsync(() -> {
                        if (targetPlayer == null) {
                            // Загружаем из БД только если игрок оффлайн
                            KitManager.get().loadPlayerDataFromDB(targetUuid);
                        }
                    }).thenRun(() -> {
                        // Запуск в основном потоке после загрузки данных
                        Bukkit.getScheduler().runTask(plugin, () -> {
                            if (KitManager.get().hasKit(targetUuid, slot)) {
                                GUI gui = new GUI(plugin);
                                gui.InspectKit(player, targetUuid, slot);
                            } else {
                                String targetName = getPlayerName(targetUuid);

                                BroadcastManager.get().sendComponentMessage(player,
                                        ERROR_PREFIX.append(
                                                mm.deserialize("<red>У игрока " + targetName +
                                                        " нет кита в слоте " + slot + "</red>")));
                                SoundManager.playFailure(player);
                            }
                        });
                    });
                });

        // Обработка исключений
        future.exceptionally(ex -> {
            Bukkit.getScheduler().runTask(plugin, () -> {
                plugin.getLogger().severe("Ошибка при загрузке данных кита: " + ex.getMessage());
                BroadcastManager.get().sendComponentMessage(player,
                        ERROR_PREFIX.append(
                                mm.deserialize("<red>Произошла ошибка при загрузке данных кита. " +
                                        "Подробности в консоли.</red>")));
                SoundManager.playFailure(player);
            });
            return null;
        });

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender,
                                                @NotNull Command command,
                                                @NotNull String label,
                                                @NotNull String[] args) {
        if (!(sender instanceof Player) || !sender.hasPermission("perplayerkit.inspect")) {
            return List.of();
        }

        if (args.length == 1) {
            String input = args[0].toLowerCase();

            // Добавление имен онлайн-игроков
            List<String> completions = new ArrayList<>(Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(input))
                    .toList());

            // Добавление UUID, если ввод похож на UUID
            if (input.length() >= 4 && input.contains("-")) {
                completions.addAll(Bukkit.getOnlinePlayers().stream()
                        .map(Player::getUniqueId)
                        .map(UUID::toString)
                        .filter(uuid -> uuid.startsWith(input))
                        .toList());
            }

            return completions;
        } else if (args.length == 2) {
            // Возвращаем номера слотов для второго аргумента
            return IntStream.rangeClosed(MIN_SLOT, MAX_SLOT)
                    .mapToObj(String::valueOf)
                    .filter(slot -> slot.startsWith(args[1]))
                    .collect(Collectors.toList());
        }

        return new ArrayList<>();
    }
}