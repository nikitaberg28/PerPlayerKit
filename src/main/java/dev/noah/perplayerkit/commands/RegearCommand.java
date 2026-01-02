package dev.noah.perplayerkit.commands;

import dev.noah.perplayerkit.KitManager;
import dev.noah.perplayerkit.gui.ItemUtil;
import dev.noah.perplayerkit.util.BroadcastManager;
import dev.noah.perplayerkit.util.CooldownManager;
import dev.noah.perplayerkit.util.DisabledCommand;
import dev.noah.perplayerkit.util.StyleManager;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public class RegearCommand implements CommandExecutor, Listener {

    public static final ItemStack REGEAR_SHULKER_ITEM = ItemUtil.createItem(Material.WHITE_SHULKER_BOX, 1, StyleManager.get().getPrimaryColor() + "Шалкер пополнения", "<gray>● Пополняет ваш кит</gray>", "<gray>● Используйте </gray>" + StyleManager.get().getPrimaryColor() + "<gray>/rg, чтобы получить еще один</gray>");
    public static final ItemStack REGEAR_SHELL_ITEM = ItemUtil.createItem(Material.SHULKER_SHELL, 1, StyleManager.get().getPrimaryColor() + "Ядро пополнения", "<gray>● Пополняет ваш кит</gray>", "<gray>● Нажмите, чтобы использовать!</gray>");

    private final Plugin plugin;
    private final CooldownManager commandCooldownManager;
    private final CooldownManager damageCooldownManager;
    private final boolean allowRegearWhileUsingElytra;
    private final boolean preventPuttingItemsInRegearInventory;

    public RegearCommand(Plugin plugin) {
        this.plugin = plugin;
        int commandCooldownInSeconds = plugin.getConfig().getInt("regear.command-cooldown", 5);
        int damageCooldownInSeconds = plugin.getConfig().getInt("regear.damage-timer", 5);
        this.commandCooldownManager = new CooldownManager(commandCooldownInSeconds);
        this.damageCooldownManager = new CooldownManager(damageCooldownInSeconds);
        this.allowRegearWhileUsingElytra = plugin.getConfig().getBoolean("regear.allow-while-using-elytra", true);
        this.preventPuttingItemsInRegearInventory = plugin.getConfig().getBoolean("regear.prevent-putting-items-in-regear-inventory", false);
    }

    @EventHandler
    public void onPlayerTakesDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        damageCooldownManager.setCooldown(player);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Только игроки могут использовать эту команду!");
            return true;
        }

        if (DisabledCommand.isBlockedInWorld(player)) {
            return true;
        }

        // Определение режима использования на основе лейбла команды
        String effectiveMode;
        if (label.equalsIgnoreCase("rg")) {
            effectiveMode = plugin.getConfig().getString("regear.rg-mode", "command");
        } else if (label.equalsIgnoreCase("regear")) {
            effectiveMode = plugin.getConfig().getString("regear.regear-mode", "command");
        } else {
            effectiveMode = plugin.getConfig().getString("regear.rg-mode", "command"); // Резервный вариант
        }

        if (effectiveMode.equalsIgnoreCase("shulker")) {
            int slot = player.getInventory().firstEmpty();
            if (slot == -1) {
                BroadcastManager.get().sendComponentMessage(player, MiniMessage.miniMessage().deserialize("<red>Ваш инвентарь полон, невозможно выдать шалкер пополнения!"));
                return true;
            }

            player.getInventory().setItem(slot, REGEAR_SHULKER_ITEM);
            BroadcastManager.get().sendComponentMessage(player, MiniMessage.miniMessage().deserialize("<green>Шалкер пополнения выдан!"));

            return true;
        }

        if (effectiveMode.equalsIgnoreCase("command")) {
            int slot = KitManager.get().getLastKitLoaded(player.getUniqueId());

            if (slot == -1) {
                BroadcastManager.get().sendComponentMessage(player, MiniMessage.miniMessage().deserialize("<red>Вы еще не загрузили ни один кит!"));
                return true;
            }

            if (!allowRegearWhileUsingElytra && player.isGliding() && player.getInventory().getChestplate() != null && player.getInventory().getChestplate().getType() == Material.ELYTRA) {
                BroadcastManager.get().sendComponentMessage(player, MiniMessage.miniMessage().deserialize("<red>Вы не можете пополнять кит во время полета на элитрах!"));
                return true;
            }

            if (damageCooldownManager.isOnCooldown(player)) {
                int secondsLeft = damageCooldownManager.getTimeLeft(player);
                BroadcastManager.get().sendComponentMessage(player, MiniMessage.miniMessage().deserialize("<red>Вы должны находиться вне боя еще " + secondsLeft + " сек. перед пополнением!"));
                return true;
            }

            if (commandCooldownManager.isOnCooldown(player)) {
                int secondsLeft = commandCooldownManager.getTimeLeft(player);
                BroadcastManager.get().sendComponentMessage(player, MiniMessage.miniMessage().deserialize("<red>Подождите " + secondsLeft + " сек. перед повторным использованием команды!"));
                return true;
            }

            KitManager.get().regearKit(player, slot);
            BroadcastManager.get().sendComponentMessage(player, MiniMessage.miniMessage().deserialize("<green>Кит пополнен!"));
            BroadcastManager.get().broadcastPlayerRegeared(player);

            commandCooldownManager.setCooldown(player);

            return true;
        }

        BroadcastManager.get().sendComponentMessage(player, MiniMessage.miniMessage().deserialize("<red>Эта команда настроена неверно, пожалуйста, свяжитесь с администратором."));
        return true;
    }

    @EventHandler
    public void onShulkerPlace(BlockPlaceEvent event) {
        if (!event.getItemInHand().equals(REGEAR_SHULKER_ITEM)) {
            return;
        }
        event.setCancelled(true);
        Player player = event.getPlayer();

        int slot = KitManager.get().getLastKitLoaded(player.getUniqueId());

        if (slot == -1) {
            BroadcastManager.get().sendComponentMessage(player, MiniMessage.miniMessage().deserialize("<red>Вы еще не загрузили ни один кит!"));
            return;
        }

        if (damageCooldownManager.isOnCooldown(player)) {
            int secondsLeft = damageCooldownManager.getTimeLeft(player);
            BroadcastManager.get().sendComponentMessage(player, MiniMessage.miniMessage().deserialize("<red>Вы должны находиться вне боя еще " + secondsLeft + " сек. перед пополнением!"));
            return;
        }

        player.getInventory().setItem(event.getHand(), null);

        // Кастомный инвентарь с холдером
        RegearInventoryHolder holder = new RegearInventoryHolder(player);
        Inventory inventory = holder.getInventory();
        player.openInventory(inventory);
    }

    @EventHandler
    public void onShulkerShellClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof RegearInventoryHolder holder)) {
            return;
        }
        ItemStack currentItem = event.getCurrentItem();

        if (currentItem == null) {
            return;
        }


        if (!currentItem.equals(REGEAR_SHELL_ITEM)) {
            if (preventPuttingItemsInRegearInventory) {
                event.setCancelled(true);
            }
            return;
        }

        Player player = holder.player();

        int slot = KitManager.get().getLastKitLoaded(player.getUniqueId());

        if (slot == -1) {
            BroadcastManager.get().sendComponentMessage(player, MiniMessage.miniMessage().deserialize("<red>Вы еще не загрузили ни один кит!"));
            return;
        }

        if (damageCooldownManager.isOnCooldown(player)) {
            int secondsLeft = damageCooldownManager.getTimeLeft(player);
            BroadcastManager.get().sendComponentMessage(player, MiniMessage.miniMessage().deserialize("<red>Вы должны находиться вне боя еще " + secondsLeft + " сек. перед пополнением!"));
            return;
        }

        player.closeInventory();

        KitManager.get().regearKit(player, slot);
        player.updateInventory();

        BroadcastManager.get().sendComponentMessage(player, MiniMessage.miniMessage().deserialize("<green>Кит пополнен!"));
        BroadcastManager.get().broadcastPlayerRegeared(player);
    }


    public record RegearInventoryHolder(
            Player player) implements InventoryHolder {

        @Override
        public @NotNull Inventory getInventory() {
            Inventory inventory = Bukkit.createInventory(this, 27, StyleManager.get().getPrimaryColor() + "Шалкер пополнения");
            inventory.setItem(13, REGEAR_SHELL_ITEM);
            return inventory;
        }
    }
}