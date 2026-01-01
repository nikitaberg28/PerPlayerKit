package dev.noah.perplayerkit.commands;

import dev.noah.perplayerkit.KitManager;           // Менеджер китов
import dev.noah.perplayerkit.gui.ItemUtil;         // Утилита для создания предметов GUI
import dev.noah.perplayerkit.util.BroadcastManager; // Утилита для отправки сообщений
import dev.noah.perplayerkit.util.CooldownManager;  // Утилита для управления кулдаунами
import dev.noah.perplayerkit.util.DisabledCommand;  // Утилита для проверки, разрешена ли команда в мире
import dev.noah.perplayerkit.util.StyleManager;     // Утилита для управления стилями (например, цветами)
import net.kyori.adventure.text.minimessage.MiniMessage; // Утилита для парсинга текста в формате MiniMessage
import org.bukkit.Bukkit;                           // Основной класс API Bukkit
import org.bukkit.Material;                         // Перечисление всех материалов (предметов, блоков)
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;          // Интерфейс для обработки команд
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;               // Аннотация для обработки событий
import org.bukkit.event.Listener;                  // Интерфейс для класса, обрабатывающего события
import org.bukkit.event.block.BlockPlaceEvent;      // Событие размещения блока
import org.bukkit.event.entity.EntityDamageEvent;   // Событие получения урона сущностью
import org.bukkit.event.inventory.InventoryClickEvent; // Событие клика по инвентарю
import org.bukkit.inventory.Inventory;               // Представление инвентаря
import org.bukkit.inventory.InventoryHolder;         // Интерфейс для объекта, "владеющего" инвентарём
import org.bukkit.inventory.ItemStack;               // Представление предмета с количеством, метаданными и т.д.
import org.bukkit.plugin.Plugin;                    // Представление плагина
import org.jetbrains.annotations.NotNull;

// Команда и обработчик событий для функции переснаряжения (regear)
public class RegearCommand implements CommandExecutor, Listener {

    // Предметы, используемые для переснаряжения
    // Шалкер с функцией переснаряжения
    public static final ItemStack REGEAR_SHULKER_ITEM = ItemUtil.createItem(
            Material.WHITE_SHULKER_BOX, 1,
            StyleManager.get().getPrimaryColor() + "Шалкер переснаряжения",
            "<gray>● Пополняет ваш кит</gray>",
            "<gray>● Используйте </gray>" + StyleManager.get().getPrimaryColor() + "<gray>/rg, чтобы получить ещё один шалкер переснаряжения</gray>"
    );
    // Оболочка шалкера с функцией переснаряжения
    public static final ItemStack REGEAR_SHELL_ITEM = ItemUtil.createItem(
            Material.SHULKER_SHELL, 1,
            StyleManager.get().getPrimaryColor() + "Оболочка переснаряжения",
            "<gray>● Пополняет ваш кит</gray>",
            "<gray>● Нажмите, чтобы использовать!</gray>"
    );

    private final Plugin plugin; // Ссылка на экземпляр плагина
    private final CooldownManager commandCooldownManager; // Менеджер кулдауна команды
    private final CooldownManager damageCooldownManager;  // Менеджер кулдауна после получения урона
    private final boolean allowRegearWhileUsingElytra;    // Разрешено ли переснаряжаться в элитрах
    private final boolean preventPuttingItemsInRegearInventory; // Запрещено ли класть предметы в инвентарь переснаряжения

    public RegearCommand(Plugin plugin) {
        this.plugin = plugin;
        // Получаем настройки кулдаунов из конфига
        int commandCooldownInSeconds = plugin.getConfig().getInt("regear.command-cooldown", 5);
        int damageCooldownInSeconds = plugin.getConfig().getInt("regear.damage-timer", 5);
        // Создаём менеджеры кулдаунов
        this.commandCooldownManager = new CooldownManager(commandCooldownInSeconds);
        this.damageCooldownManager = new CooldownManager(damageCooldownInSeconds);
        // Получаем настройки из конфига
        this.allowRegearWhileUsingElytra = plugin.getConfig().getBoolean("regear.allow-while-using-elytra", true);
        this.preventPuttingItemsInRegearInventory = plugin.getConfig().getBoolean("regear.prevent-putting-items-in-regear-inventory", false);
    }

    // Обработчик события получения урона игроком
    @EventHandler
    public void onPlayerTakesDamage(EntityDamageEvent event) {
        // Проверяем, является ли сущность, получившая урон, игроком
        if (!(event.getEntity() instanceof Player player)) {
            return; // Если нет, выходим
        }
        // Устанавливаем кулдаун на переснаряжение после получения урона
        damageCooldownManager.setCooldown(player);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        // Проверяем, является ли отправитель команды игроком
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Только игроки могут использовать эту команду!");
            return true;
        }

        // Проверяем, заблокирована ли команда в мире, где находится игрок
        if (DisabledCommand.isBlockedInWorld(player)) {
            return true; // Если команда заблокирована, просто завершаем обработку
        }

        // Определяем, какой режим использовать, на основе введённой команды (rg или regear)
        String effectiveMode;
        if (label.equalsIgnoreCase("rg")) {
            effectiveMode = plugin.getConfig().getString("regear.rg-mode", "command");
        } else if (label.equalsIgnoreCase("regear")) {
            effectiveMode = plugin.getConfig().getString("regear.regear-mode", "command");
        } else {
            effectiveMode = plugin.getConfig().getString("regear.rg-mode", "command"); // Резервный вариант
        }

        // Если режим "shulker" - даём игроку шалкер-ящик переснаряжения
        if (effectiveMode.equalsIgnoreCase("shulker")) {
            int slot = player.getInventory().firstEmpty(); // Ищем первый пустой слот
            if (slot == -1) { // Если инвентарь полон
                BroadcastManager.get().sendComponentMessage(player, MiniMessage.miniMessage().deserialize("<red>Твой инвентарь полон, не могу дать шалкер переснаряжения!</red>"));
                return true;
            }

            player.getInventory().setItem(slot, REGEAR_SHULKER_ITEM); // Кладём шалкер в инвентарь
            BroadcastManager.get().sendComponentMessage(player, MiniMessage.miniMessage().deserialize("<green>Шалкер переснаряжения выдан!</green>"));

            return true;
        }

        // Если режим "command" - выполняем переснаряжение напрямую
        if (effectiveMode.equalsIgnoreCase("command")) {
            // Получаем слот последнего загруженного кита
            int slot = KitManager.get().getLastKitLoaded(player.getUniqueId());

            if (slot == -1) { // Если кит ещё не загружался
                BroadcastManager.get().sendComponentMessage(player, MiniMessage.miniMessage().deserialize("<red>Вы ещё не загрузили кит!</red>"));
                return true;
            }

            // Проверяем, использует ли игрок элитры, если это запрещено
            if (!allowRegearWhileUsingElytra && player.isGliding() && player.getInventory().getChestplate() != null && player.getInventory().getChestplate().getType() == Material.ELYTRA) {
                BroadcastManager.get().sendComponentMessage(player, MiniMessage.miniMessage().deserialize("<red>Вы не можете переснаряжаться, используя элитры!</red>"));
                return true;
            }

            // Проверяем, на кулдауне ли игрок после получения урона
            if (damageCooldownManager.isOnCooldown(player)) {
                int secondsLeft = damageCooldownManager.getTimeLeft(player);
                BroadcastManager.get().sendComponentMessage(player, MiniMessage.miniMessage().deserialize("<red>Вы должны не быть в бою ещё " + secondsLeft + " секунд перед переснаряжением!</red>"));
                return true;
            }

            // Проверяем, на кулдауне ли команда
            if (commandCooldownManager.isOnCooldown(player)) {
                int secondsLeft = commandCooldownManager.getTimeLeft(player);
                BroadcastManager.get().sendComponentMessage(player, MiniMessage.miniMessage().deserialize("<red>Вы должны подождать " + secondsLeft + " секунд перед повторным использованием команды!</red>"));
                return true;
            }

            // Выполняем переснаряжение
            KitManager.get().regearKit(player, slot);
            BroadcastManager.get().sendComponentMessage(player, MiniMessage.miniMessage().deserialize("<green>Переснаряжение выполнено!</green>"));
            BroadcastManager.get().broadcastPlayerRegeared(player); // Отправляем сообщение в чат

            commandCooldownManager.setCooldown(player); // Устанавливаем кулдаун команды

            return true;
        }

        // Если режим настроен неправильно
        BroadcastManager.get().sendComponentMessage(player, MiniMessage.miniMessage().deserialize("<red>Команда настроена неправильно, пожалуйста, сообщите администратору.</red>"));
        return true;
    }

    // Обработчик события размещения блока (шалкера переснаряжения)
    @EventHandler
    public void onShulkerPlace(BlockPlaceEvent event) {
        // Проверяем, является ли размещаемый блок шалкером переснаряжения
        if (!event.getItemInHand().equals(REGEAR_SHULKER_ITEM)) {
            return; // Если нет, выходим
        }
        event.setCancelled(true); // Отменяем размещение
        Player player = event.getPlayer();

        // Получаем слот последнего загруженного кита
        int slot = KitManager.get().getLastKitLoaded(player.getUniqueId());

        if (slot == -1) { // Если кит ещё не загружался
            BroadcastManager.get().sendComponentMessage(player, MiniMessage.miniMessage().deserialize("<red>Вы ещё не загрузили кит!</red>"));
            return;
        }

        // Проверяем, на кулдауне ли игрок после получения урона
        if (damageCooldownManager.isOnCooldown(player)) {
            int secondsLeft = damageCooldownManager.getTimeLeft(player);
            BroadcastManager.get().sendComponentMessage(player, MiniMessage.miniMessage().deserialize("<red>Вы должны не быть в бою ещё " + secondsLeft + " секунд перед переснаряжением!</red>"));
            return;
        }

        // Убираем шалкер из руки
        player.getInventory().setItem(event.getHand(), null);

        // Создаём и открываем специальный инвентарь для переснаряжения
        RegearInventoryHolder holder = new RegearInventoryHolder(player);
        Inventory inventory = holder.getInventory();
        player.openInventory(inventory);
    }

    // Обработчик клика по инвентарю переснаряжения
    @EventHandler
    public void onShulkerShellClick(InventoryClickEvent event) {
        // Проверяем, является ли владелец инвентаря специальным инвентарём переснаряжения
        if (!(event.getInventory().getHolder() instanceof RegearInventoryHolder holder)) {
            return; // Если нет, выходим
        }
        ItemStack currentItem = event.getCurrentItem(); // Получаем предмет, по которому кликнули

        if (currentItem == null) {
            return; // Если кликнули в пустой слот, выходим
        }

        // Проверяем, является ли кликнутый предмет оболочкой переснаряжения
        if (!currentItem.equals(REGEAR_SHELL_ITEM)) {
            // Если запрещено класть предметы в инвентарь переснаряжения
            if (preventPuttingItemsInRegearInventory) {
                event.setCancelled(true); // Отменяем действие
            }
            return;
        }

        Player player = holder.player(); // Получаем игрока из владельца инвентаря

        // Получаем слот последнего загруженного кита
        int slot = KitManager.get().getLastKitLoaded(player.getUniqueId());

        if (slot == -1) { // Если кит ещё не загружался
            BroadcastManager.get().sendComponentMessage(player, MiniMessage.miniMessage().deserialize("<red>Вы ещё не загрузили кит!</red>"));
            return;
        }

        // Проверяем, на кулдауне ли игрок после получения урона
        if (damageCooldownManager.isOnCooldown(player)) {
            int secondsLeft = damageCooldownManager.getTimeLeft(player);
            BroadcastManager.get().sendComponentMessage(player, MiniMessage.miniMessage().deserialize("<red>Вы должны не быть в бою ещё " + secondsLeft + " секунд перед переснаряжением!</red>"));
            return;
        }

        player.closeInventory(); // Закрываем инвентарь

        // Выполняем переснаряжение
        KitManager.get().regearKit(player, slot);
        player.updateInventory(); // Обновляем инвентарь игрока

        BroadcastManager.get().sendComponentMessage(player, MiniMessage.miniMessage().deserialize("<green>Переснаряжение выполнено!</green>"));
        BroadcastManager.get().broadcastPlayerRegeared(player); // Отправляем сообщение в чат
    }


    // Запись (record) для владельца инвентаря переснаряжения
    public record RegearInventoryHolder(
            Player player) implements InventoryHolder { // Реализует интерфейс владельца инвентаря

        @Override
        public @NotNull Inventory getInventory() {
            // Создаём инвентарь на 27 слотов с заголовком
            Inventory inventory = Bukkit.createInventory(this, 27, StyleManager.get().getPrimaryColor() + "Шалкер переснаряжения");
            // Кладём оболочку переснаряжения в центральный слот (13)
            inventory.setItem(13, REGEAR_SHELL_ITEM);
            return inventory;
        }
    }
}