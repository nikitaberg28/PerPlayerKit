package dev.noah.perplayerkit.gui;

import dev.noah.perplayerkit.ItemFilter;
import dev.noah.perplayerkit.KitManager;
import dev.noah.perplayerkit.KitRoomDataManager;
import dev.noah.perplayerkit.PublicKit;
import dev.noah.perplayerkit.util.*;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.ipvp.canvas.Menu;
import org.ipvp.canvas.slot.ClickOptions;
import org.ipvp.canvas.slot.Slot;
import org.ipvp.canvas.type.ChestMenu;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static dev.noah.perplayerkit.gui.ItemUtil.addHideFlags;
import static dev.noah.perplayerkit.gui.ItemUtil.createItem;
import static dev.noah.perplayerkit.gui.ItemUtil.createGlassPane;

public class GUI {
    private final Plugin plugin;
    private final boolean filterItemsOnImport;
    private static final Set<UUID> kitDeletionFlag = new HashSet<>(); // Флаг удаления кита

    public GUI(Plugin plugin) {
        this.plugin = plugin;
        this.filterItemsOnImport = plugin.getConfig().getBoolean("anti-exploit.import-filter", false);
    }

    public static void addLoadPublicKit(Slot slot, String id) {
        slot.setClickHandler((player, info) -> {
            SoundManager.playClick(player);
            KitManager.get().loadPublicKit(player, id); // Загрузить публичный кит
            info.getClickedMenu().close();
        });
    }

    public static Menu createPublicKitMenu() {
        return ChestMenu.builder(6).title(StyleManager.get().getPrimaryColor() + "Публичная комната китов").redraw(true).build(); // Комната китов
    }

    public static boolean removeKitDeletionFlag(Player player) {
        return kitDeletionFlag.remove(player.getUniqueId());
    }

    public void OpenKitMenu(Player p, int slot) {
        Menu menu = createKitMenu(slot);

        if (KitManager.get().getItemStackArrayById(p.getUniqueId().toString() + slot) != null) {
            ItemStack[] kit = KitManager.get().getItemStackArrayById(p.getUniqueId().toString() + slot); // Кит игрока
            for (int i = 0; i < 41; i++) {
                menu.getSlot(i).setItem(kit[i]);
            }
        }
        for (int i = 0; i < 41; i++) {
            allowModification(menu.getSlot(i));
        }
        for (int i = 41; i < 54; i++) {
            menu.getSlot(i).setItem(ItemUtil.createGlassPane());
        }
        menu.getSlot(45).setItem(createItem(Material.CHAINMAIL_BOOTS, 1, "<gray>БОТИНКИ</gray>"));
        menu.getSlot(46).setItem(createItem(Material.CHAINMAIL_LEGGINGS, 1, "<gray>ПОНОЖИ</gray>"));
        menu.getSlot(47).setItem(createItem(Material.CHAINMAIL_CHESTPLATE, 1, "<gray>НАГРУДНИК</gray>"));
        menu.getSlot(48).setItem(createItem(Material.CHAINMAIL_HELMET, 1, "<gray>ШЛЕМ</gray>"));
        menu.getSlot(49).setItem(createItem(Material.SHIELD, 1, "<gray>ЛЕВАЯ РУКА</gray>"));

        menu.getSlot(51).setItem(createItem(Material.CHEST, 1, "<green>ИМПОРТ</green>", "<gray>● ЛКМ для импорта из инвентаря</gray>"));
        menu.getSlot(52).setItem(createItem(Material.BARRIER, 1, "<red>ОЧИСТИТЬ КИТ</red>", "<gray>● ЛКМ для очистки</gray>")); // Очистить кит
        menu.getSlot(53).setItem(createItem(Material.OAK_DOOR, 1, "<red>НАЗАД</red>"));
        addMainButton(menu.getSlot(53));
        addClear(menu.getSlot(52)); // Очистить кит
        addImport(menu.getSlot(51));
        menu.setCursorDropHandler(Menu.ALLOW_CURSOR_DROPPING);

        menu.open(p);
    }

    public void OpenPublicKitEditor(Player p, String kitId) { // Редактор публичного кита
        Menu menu = createPublicKitMenu(kitId);

        if (KitManager.get().getItemStackArrayById(IDUtil.getPublicKitId(kitId)) != null) { // Получить публичный кит
            ItemStack[] kit = KitManager.get().getItemStackArrayById(IDUtil.getPublicKitId(kitId)); // Кит
            for (int i = 0; i < 41; i++) {
                menu.getSlot(i).setItem(kit[i]);
            }
        }
        for (int i = 0; i < 41; i++) {
            allowModification(menu.getSlot(i));
        }
        for (int i = 41; i < 54; i++) {
            menu.getSlot(i).setItem(ItemUtil.createGlassPane());
        }
        menu.getSlot(45).setItem(createItem(Material.CHAINMAIL_BOOTS, 1, "<gray>БОТИНКИ</gray>"));
        menu.getSlot(46).setItem(createItem(Material.CHAINMAIL_LEGGINGS, 1, "<gray>ПОНОЖИ</gray>"));
        menu.getSlot(47).setItem(createItem(Material.CHAINMAIL_CHESTPLATE, 1, "<gray>НАГРУДНИК</gray>"));
        menu.getSlot(48).setItem(createItem(Material.CHAINMAIL_HELMET, 1, "<gray>ШЛЕМ</gray>"));
        menu.getSlot(49).setItem(createItem(Material.SHIELD, 1, "<gray>ЛЕВАЯ РУКА</gray>"));

        menu.getSlot(51).setItem(createItem(Material.CHEST, 1, "<green>ИМПОРТ</green>", "<gray>● ЛКМ для импорта из инвентаря</gray>"));
        menu.getSlot(52).setItem(createItem(Material.BARRIER, 1, "<red>ОЧИСТИТЬ КИТ</red>", "<gray>● ЛКМ для очистки</gray>")); // Очистить кит
        menu.getSlot(53).setItem(createItem(Material.OAK_DOOR, 1, "<red>НАЗАД</red>"));
        addMainButton(menu.getSlot(53));
        addClear(menu.getSlot(52)); // Очистить кит
        addImport(menu.getSlot(51));
        menu.setCursorDropHandler(Menu.ALLOW_CURSOR_DROPPING);

        menu.open(p);
    }

    public void OpenECKitKenu(Player p, int slot) { // Открыть меню кита Эндер-сундука
        Menu menu = createECMenu(slot);

        for (int i = 0; i < 9; i++) {
            menu.getSlot(i).setItem(ItemUtil.createGlassPane());

        }
        for (int i = 36; i < 54; i++) {
            menu.getSlot(i).setItem(ItemUtil.createGlassPane());

        }
        if (KitManager.get().getItemStackArrayById(p.getUniqueId() + "ec" + slot) != null) { // Получить кит эндер-сундука

            ItemStack[] kit = KitManager.get().getItemStackArrayById(p.getUniqueId() + "ec" + slot); // Кит эндер-сундука
            for (int i = 9; i < 36; i++) {
                menu.getSlot(i).setItem(kit[i - 9]);
            }
        }
        for (int i = 9; i < 36; i++) {
            allowModification(menu.getSlot(i));
        }
        menu.getSlot(51).setItem(createItem(Material.ENDER_CHEST, 1, "<green>ИМПОРТ</green>", "<gray>● ЛКМ для импорта из эндер-сундука</gray>")); // Импорт из эндер-сундука
        menu.getSlot(52).setItem(createItem(Material.BARRIER, 1, "<red>ОЧИСТИТЬ КИТ</red>", "<gray>● ЛКМ для очистки</gray>")); // Очистить кит
        menu.getSlot(53).setItem(createItem(Material.OAK_DOOR, 1, "<red>НАЗАД</red>"));
        addMainButton(menu.getSlot(53));
        addClear(menu.getSlot(52), 9, 36); // Очистить кит
        addImportEC(menu.getSlot(51)); // Импорт из эндер-сундука
        menu.setCursorDropHandler(Menu.ALLOW_CURSOR_DROPPING);
        menu.open(p);
    }

    public void InspectKit(Player p, UUID target, int slot) { // Просмотр кита
        String playerName = getPlayerName(target);
        Menu menu = createInspectMenu(slot, playerName);

        if (KitManager.get().hasKit(target, slot)) {
            ItemStack[] kit = KitManager.get().getItemStackArrayById(target.toString() + slot); // Получить кит игрока
            for (int i = 0; i < 41; i++) {
                menu.getSlot(i).setItem(kit[i]);
            }
        }
        for (int i = 41; i < 54; i++) {
            menu.getSlot(i).setItem(ItemUtil.createGlassPane());
        }
        menu.getSlot(45).setItem(createItem(Material.CHAINMAIL_BOOTS, 1, "<gray>БОТИНКИ</gray>"));
        menu.getSlot(46).setItem(createItem(Material.CHAINMAIL_LEGGINGS, 1, "<gray>ПОНОЖИ</gray>"));
        menu.getSlot(47).setItem(createItem(Material.CHAINMAIL_CHESTPLATE, 1, "<gray>НАГРУДНИК</gray>"));
        menu.getSlot(48).setItem(createItem(Material.CHAINMAIL_HELMET, 1, "<gray>ШЛЕМ</gray>"));
        menu.getSlot(49).setItem(createItem(Material.SHIELD, 1, "<gray>ЛЕВАЯ РУКА</gray>"));

        menu.getSlot(53).setItem(createItem(Material.OAK_DOOR, 1, "<red>ЗАКРЫТЬ</red>"));
        menu.getSlot(53).setClickHandler((player, info) -> {
            SoundManager.playClick(player);
            info.getClickedMenu().close();
            SoundManager.playCloseGui(player);
        });

        if (p.hasPermission("perplayerkit.admin")) {
            for (int i = 0; i < 41; i++) {
                allowModification(menu.getSlot(i));
            }
            menu.getSlot(52).setItem(createItem(Material.BARRIER, 1, "<red>ОЧИСТИТЬ КИТ</red>", "<gray>● ПКМ/Shift-ЛКМ для удаления кита</gray>")); // Очистить кит
            addClearKit(menu.getSlot(52), target, slot); // Удалить кит
        }

        menu.setCursorDropHandler(Menu.ALLOW_CURSOR_DROPPING);
        menu.open(p);
        SoundManager.playOpenGui(p);
    }

    public void InspectEc(Player p, UUID target, int slot) { // Просмотр эндер-сундука
        String playerName = getPlayerName(target);
        Menu menu = createInspectEcMenu(slot, playerName);

        for (int i = 0; i < 9; i++) {
            menu.getSlot(i).setItem(ItemUtil.createGlassPane());

        }
        for (int i = 36; i < 54; i++) {
            menu.getSlot(i).setItem(ItemUtil.createGlassPane());

        }
        if (KitManager.get().getItemStackArrayById(p.getUniqueId() + "ec" + slot) != null) { // Получить кит эндер-сундука

            ItemStack[] kit = KitManager.get().getItemStackArrayById(p.getUniqueId() + "ec" + slot); // Кит эндер-сундука
            for (int i = 9; i < 36; i++) {
                menu.getSlot(i).setItem(kit[i - 9]);
            }
        }

        menu.getSlot(53).setItem(createItem(Material.OAK_DOOR, 1, "<red>ЗАКРЫТЬ</red>"));
        menu.getSlot(53).setClickHandler((player, info) -> {
            SoundManager.playClick(player);
            info.getClickedMenu().close();
            SoundManager.playCloseGui(player);
        });

        if (p.hasPermission("perplayerkit.admin")) {
            for (int i = 9; i < 36; i++) {
                allowModification(menu.getSlot(i));
            }
            menu.getSlot(52).setItem(createItem(Material.BARRIER, 1, "<red>ОЧИСТИТЬ ЭНДЕР-СУНДУК</red>", "<gray>● ПКМ/Shift-ЛКМ для удаления эндер-сундука</gray>")); // Очистить эндер-сундук
            addClearEnderchest(menu.getSlot(52), target, slot); // Удалить эндер-сундук
        }

        menu.setCursorDropHandler(Menu.ALLOW_CURSOR_DROPPING);
        menu.open(p);
        SoundManager.playOpenGui(p);
    }

    public void OpenMainMenu(Player p) { // Открыть главное меню
        Menu menu = createMainMenu(p);
        for (int i = 0; i < 54; i++) {
            menu.getSlot(i).setItem(createGlassPane());
        }
        for (int i = 9; i < 18; i++) {
            menu.getSlot(i).setItem(createItem(Material.CHEST, 1, "<dark_aqua>Кит " + (i - 8) + "</dark_aqua>", "<gray>● ЛКМ для загрузки кита</gray>")); // Кит
            addEditLoad(menu.getSlot(i), i - 8); // Редактировать/Загрузить
        }
        for (int i = 18; i < 27; i++) {
            if (KitManager.get().getItemStackArrayById(p.getUniqueId() + "ec" + (i - 17)) != null) { // Получить кит эндер-сундука
                menu.getSlot(i).setItem(createItem(Material.ENDER_CHEST, 1, "<dark_aqua>Эндер-сундук " + (i - 17) + "</dark_aqua>", "<gray>● ЛКМ для загрузки кита</gray>")); // Эндер-сундук
                addEditLoadEC(menu.getSlot(i), i - 17); // Редактировать/Загрузить эндер-сундука
            } else {
                menu.getSlot(i).setItem(createItem(Material.ENDER_EYE, 1, "<dark_aqua>Эндер-сундук " + (i - 17) + "</dark_aqua>", "<gray>● ЛКМ для создания</gray>")); // Создать эндер-сундук
                addEditEC(menu.getSlot(i), i - 17); // Редактировать эндер-сундука
            }
        }
        for (int i = 27; i < 36; i++) {
            if (KitManager.get().getItemStackArrayById(p.getUniqueId().toString() + (i - 26)) != null) { // Получить кит
                menu.getSlot(i).setItem(createItem(Material.KNOWLEDGE_BOOK, 1, "<green>КИТ СУЩЕСТВУЕТ</green>", "<gray>● ЛКМ для редактирования</gray>")); // Кит существует
            } else {
                menu.getSlot(i).setItem(createItem(Material.BOOK, 1, "<red>КИТ НЕ НАЙДЕН</red>", "<gray>● ЛКМ для создания</gray>")); // Кит не найден
            }
            addEdit(menu.getSlot(i), i - 26); // Редактировать кит
        }

        for (int i = 37; i < 44; i++) {
            menu.getSlot(i).setItem(createGlassPane());
        }

        menu.getSlot(37).setItem(createItem(Material.NETHER_STAR, 1, "<green>КОМНАТА КИТОВ</green>")); // Комната китов
        menu.getSlot(38).setItem(createItem(Material.BOOKSHELF, 1, "<yellow>ГОТОВЫЕ КИТЫ</yellow>")); // Готовые киты
        menu.getSlot(39).setItem(createItem(Material.OAK_SIGN, 1, "<green>ИНФА</green>", "<gray>● ЛКМ по слоту кита для загрузки</gray>", "<gray>● ЛКМ по книге для редактирования</gray>", "<gray>● /sharekit <slot></gray>")); // Инфо
        menu.getSlot(41).setItem(createItem(Material.REDSTONE_BLOCK, 1, "<red>ОЧИСТИТЬ ИНВЕНТАРЬ</red>", "<gray>● ЛКМ для очистки</gray>"));
        menu.getSlot(42).setItem(createItem(Material.COMPASS, 1, "<green>ПОДЕЛИТЬСЯ КИТАМИ</green>", "<gray>● /sharekit <slot></gray>")); // Делиться китами
        menu.getSlot(43).setItem(createItem(Material.EXPERIENCE_BOTTLE, 1, "<green>ПОЧИНИТЬ ПРЕДМЕТЫ</green>")); // Починить предметы
        addRepairButton(menu.getSlot(43)); // Кнопка починки
        addKitRoom(menu.getSlot(37)); // Комната китов
        addPublicKitMenu(menu.getSlot(38)); // Меню публичных китов
        addClearButton(menu.getSlot(41)); // Кнопка очистки

        menu.setCursorDropHandler(Menu.ALLOW_CURSOR_DROPPING);
        menu.open(p);
    }

    public void OpenKitRoom(Player p) { // Открыть комнату китов
        OpenKitRoom(p, 0);
    }

    public void OpenKitRoom(Player p, int page) { // Открыть комнату китов (страница)
        Menu menu = createKitRoom();
        for (int i = 0; i < 45; i++) {
            allowModification(menu.getSlot(i));
        }
        for (int i = 45; i < 54; i++) {
            menu.getSlot(i).setItem(ItemUtil.createGlassPane());
        }
        if (KitRoomDataManager.get().getKitRoomPage(page) != null) { // Получить страницу комнаты китов
            for (int i = 0; i < 45; i++) {
                menu.getSlot(i).setItem(KitRoomDataManager.get().getKitRoomPage(page)[i]); // Получить страницу комнаты китов
            }
        }

        menu.getSlot(45).setItem(createItem(Material.BEACON, 1, "<dark_aqua>ПОПОЛНИТЬ</dark_aqua>")); // Пополнить
        addKitRoom(menu.getSlot(45), page); // Комната китов (страница)

        if (!p.hasPermission("perplayerkit.editkitroom")) {
            menu.getSlot(53).setItem(createItem(Material.OAK_DOOR, 1, "<red>НАЗАД</red>"));
            addMainButton(menu.getSlot(53));
        } else {
            menu.getSlot(53).setItem(createItem(Material.BARRIER, page + 1, "<red>МЕНЮ РЕДАКТИРОВАНИЯ</red>", "<red>ПКМ/Shift-ЛКМ ДЛЯ СОХРАНЕНИЯ</red>")); // Меню редактирования
            // Привязываем обработчик сохранения страницы (ПКМ или Shift+ЛКМ)
            addKitRoomSaveButton(menu.getSlot(53), page);
        }
        addKitRoom(menu.getSlot(47), 0); // Комната китов (страница)
        addKitRoom(menu.getSlot(48), 1); // Комната китов (страница)
        addKitRoom(menu.getSlot(49), 2); // Комната китов (страница)
        addKitRoom(menu.getSlot(50), 3); // Комната китов (страница)
        addKitRoom(menu.getSlot(51), 4); // Комната китов (страница)

        for (int i = 1; i < 6; i++) {
            menu.getSlot(46 + i).setItem(addHideFlags(createItem(Material.valueOf(plugin.getConfig().getString("kitroom.items." + i + ".material")), "<reset>" + plugin.getConfig().getString("kitroom.items." + i + ".name")))); // Получить материал и имя из конфига
        }

        menu.getSlot(page + 47).setItem(ItemUtil.addEnchantLook(menu.getSlot(page + 47).getItem(p))); // Выделить выбранную страницу

        menu.setCursorDropHandler(Menu.ALLOW_CURSOR_DROPPING);
        menu.open(p);
    }

    public Menu ViewPublicKitMenu(Player p, String id) { // Просмотр публичного кита
        ItemStack[] kit = KitManager.get().getPublicKit(id); // Получить публичный кит

        if (kit == null) {
            p.sendMessage(ChatColor.RED + "Кит не найден"); // Кит не найден
            if (p.hasPermission("perplayerkit.admin")) {
                p.sendMessage(ChatColor.RED + "Чтобы назначить кит этому публичному киту, используйте /savepublickit <id>"); // Назначить кит
            }
            return null;
        }
        Menu menu = ChestMenu.builder(6).title(StyleManager.get().getPrimaryColor() + "Просмотр публичного кита: " + id).redraw(true).build(); // Просмотр публичного кита

        for (int i = 0; i < 54; i++) {
            menu.getSlot(i).setItem(ItemUtil.createGlassPane());
        }

        for (int i = 9; i < 36; i++) {
            menu.getSlot(i).setItem(kit[i]);
        }
        for (int i = 0; i < 9; i++) {
            menu.getSlot(i + 36).setItem(kit[i]);
        }
        for (int i = 36; i < 41; i++) {
            menu.getSlot(i + 9).setItem(kit[i]);
        }

        menu.getSlot(45).setItem(createItem(Material.CHAINMAIL_BOOTS, 1, "<gray>БОТИНКИ</gray>"));
        menu.getSlot(46).setItem(createItem(Material.CHAINMAIL_LEGGINGS, 1, "<gray>ПОНОЖИ</gray>"));
        menu.getSlot(47).setItem(createItem(Material.CHAINMAIL_CHESTPLATE, 1, "<gray>НАГРУДНИК</gray>"));
        menu.getSlot(48).setItem(createItem(Material.CHAINMAIL_HELMET, 1, "<gray>ШЛЕМ</gray>"));
        menu.getSlot(49).setItem(createItem(Material.SHIELD, 1, "<gray>ЛЕВАЯ РУКА</gray>"));

        menu.getSlot(52).setItem(createItem(Material.APPLE, 1, "<green>ЗАГРУЗИТЬ КИТ</green>")); // Загрузить кит
        menu.getSlot(53).setItem(createItem(Material.OAK_DOOR, 1, "<red>НАЗАД</red>"));
        addPublicKitMenu(menu.getSlot(53)); // Меню публичных китов
        addLoadPublicKit(menu.getSlot(52), id); // Загрузить публичный кит

        menu.open(p);

        return menu;
    }

    public void OpenPublicKitMenu(Player player) { // Открыть меню публичных китов
        Menu menu = createPublicKitMenu();
        for (int i = 0; i < 54; i++) {
            menu.getSlot(i).setItem(ItemUtil.createGlassPane());
        }

        for (int i = 18; i < 36; i++) {
            menu.getSlot(i).setItem(ItemUtil.createItem(Material.BOOK, 1, "<gray>звезда добавит киты потом.</gray>")); // Больше китов скоро
        }

        List<PublicKit> publicKitList = KitManager.get().getPublicKitList(); // Получить список публичных китов

        for (int i = 0; i < publicKitList.size(); i++) {
            if (KitManager.get().hasPublicKit(publicKitList.get(i).id)) { // Проверить наличие публичного кита
                if (player.hasPermission("perplayerkit.admin")) {
                    menu.getSlot(i + 18).setItem(createItem(publicKitList.get(i).icon, 1, ChatColor.RESET + publicKitList.get(i).name, "<gray>● [АДМИН] Shift-клик для редактирования</gray>")); // Иконка, имя
                } else {
                    menu.getSlot(i + 18).setItem(createItem(publicKitList.get(i).icon, 1, ChatColor.RESET + publicKitList.get(i).name)); // Иконка, имя
                }
                addPublicKitButton(menu.getSlot(i + 18), publicKitList.get(i).id); // Кнопка публичного кита
            } else {
                if (player.hasPermission("perplayerkit.admin")) {
                    menu.getSlot(i + 18).setItem(createItem(publicKitList.get(i).icon, 1, ChatColor.RESET + publicKitList.get(i).name + " <red>[НЕ НАЗНАЧЕН]</red>", "<gray>● Администраторы еще не настроили этот кит</gray>", "<gray>● [АДМИН] Shift-клик для редактирования</gray>")); // Не назначен
                } else {
                    menu.getSlot(i + 18).setItem(createItem(publicKitList.get(i).icon, 1, ChatColor.RESET + publicKitList.get(i).name + " <red>[НЕ НАЗНАЧЕН]</red>", "<gray>● Администраторы еще не настроили этот кит</gray>")); // Не назначен
                }
            }

            if (player.hasPermission("perplayerkit.admin")) {
                addAdminPublicKitButton(menu.getSlot(i + 18), publicKitList.get(i).id); // Админская кнопка публичного кита
            }
        }

        addMainButton(menu.getSlot(53)); // Кнопка главного меню

        menu.getSlot(53).setItem(createItem(Material.OAK_DOOR, 1, "<red>НАЗАД</red>"));
        menu.open(player);
    }

    public void addClear(Slot slot) { // Добавить очистку (для обычного кита)
        slot.setClickHandler((player, info) -> {
            SoundManager.playClick(player);
            Menu m = info.getClickedMenu();
            for (int i = 0; i < 41; i++) {
                m.getSlot(i).setItem((org.bukkit.inventory.ItemStack) null);
            }
        });
    }

    public void addClear(Slot slot, int start, int end) { // Добавить очистку (диапазон)
        slot.setClickHandler((player, info) -> {
            SoundManager.playClick(player);
            Menu m = info.getClickedMenu();
            for (int i = start; i < end; i++) {
                m.getSlot(i).setItem((org.bukkit.inventory.ItemStack) null);
            }
        });
    }

    public void addClearKit(Slot slot, UUID target, int slotNum) { // Добавить удаление кита
        slot.setClickHandler((player, info) -> {
            SoundManager.playClick(player);
            if (info.getClickType() == ClickType.RIGHT || info.getClickType().isShiftClick()) {
                KitManager.get().deleteKit(target, slotNum); // Удалить кит
                player.sendMessage(ChatColor.GREEN + "Кит " + slotNum + " удален для игрока!"); // Кит удален
                SoundManager.playSuccess(player);
                kitDeletionFlag.add(player.getUniqueId()); // Добавить флаг удаления
                info.getClickedMenu().close();
                SoundManager.playCloseGui(player);
            }
        });
    }

    public void addClearEnderchest(Slot slot, UUID target, int slotNum) { // Добавить удаление эндер-сундука
        slot.setClickHandler((player, info) -> {
            SoundManager.playClick(player);
            if (info.getClickType() == ClickType.RIGHT || info.getClickType().isShiftClick()) {
                KitManager.get().deleteEnderchest(target, slotNum); // Удалить эндер-сундук
                player.sendMessage(ChatColor.GREEN + "Эндер-сундук " + slotNum + " удален для игрока!"); // Эндер-сундук удален
                SoundManager.playSuccess(player);
                kitDeletionFlag.add(player.getUniqueId()); // Добавить флаг удаления
                info.getClickedMenu().close();
                SoundManager.playCloseGui(player);
            }
        });
    }

    public void addPublicKitButton(Slot slot, String id) { // Добавить кнопку публичного кита
        slot.setClickHandler((player, info) -> {
            SoundManager.playClick(player);
            if (info.getClickType() == ClickType.LEFT) {
                KitManager.get().loadPublicKit(player, id); // Загрузить публичный кит
                info.getClickedMenu().close();
            } else if (info.getClickType() == ClickType.RIGHT) {
                Menu m = ViewPublicKitMenu(player, id); // Просмотр публичного кита
                if (m != null) {
                    m.open(player);
                }
            }
        });
    }

    public void addAdminPublicKitButton(Slot slot, String id) { // Добавить админскую кнопку публичного кита
        slot.setClickHandler((player, info) -> {
            SoundManager.playClick(player);
            if (info.getClickType().isShiftClick()) {
                OpenPublicKitEditor(player, id); // Открыть редактор публичного кита
                return;
            }
            if (info.getClickType() == ClickType.LEFT) {
                KitManager.get().loadPublicKit(player, id); // Загрузить публичный кит
            } else if (info.getClickType() == ClickType.RIGHT) {
                Menu m = ViewPublicKitMenu(player, id); // Просмотр публичного кита
                if (m != null) {
                    m.open(player);
                }
            }
        });
    }

    public void addMainButton(Slot slot) { // Добавить кнопку главного меню
        slot.setClickHandler((player, info) -> {
            SoundManager.playClick(player);
            OpenMainMenu(player); // Открыть главное меню
        });
    }

    public void addKitRoom(Slot slot) { // Добавить кнопку комнаты китов
        slot.setClickHandler((player, info) -> {
            SoundManager.playClick(player);
            OpenKitRoom(player); // Открыть комнату китов
            BroadcastManager.get().broadcastPlayerOpenedKitRoom(player); // Трансляция открытия комнаты китов
        });
    }

    public void addKitRoom(Slot slot, int page) { // Добавить кнопку комнаты китов (страница)
        slot.setClickHandler((player, info) -> {
            SoundManager.playClick(player);
            OpenKitRoom(player, page); // Открыть комнату китов (страница)
        });
    }

    public void addPublicKitMenu(Slot slot) { // Добавить кнопку меню публичных китов
        slot.setClickHandler((player, info) -> {
            SoundManager.playClick(player);
            OpenPublicKitMenu(player); // Открыть меню публичных китов
        });
    }

    // Исправленная версия: теперь принимает страницу, читает содержимое меню и реагирует на ПКМ или Shift+ЛКМ
    public void addKitRoomSaveButton(Slot slot, int page) {
        slot.setClickHandler((player, info) -> {
            SoundManager.playClick(player);
            ClickType ct = info.getClickType();
            // Условие: ПКМ (RIGHT) ИЛИ Shift+ЛКМ (SHIFT_LEFT)
            if (ct == ClickType.RIGHT || ct == ClickType.SHIFT_LEFT) {
                Menu m = info.getClickedMenu();
                ItemStack[] data = new ItemStack[45];
                for (int i = 0; i < 45; i++) {
                    data[i] = m.getSlot(i).getItem();
                }
                KitRoomDataManager.get().setKitRoom(page, data);
                player.sendMessage(ChatColor.GREEN + "Меню сохранено в базу данных");
                SoundManager.playSuccess(player);
            }
        });
    }

    public void addRepairButton(Slot slot) { // Добавить кнопку починки
        slot.setClickHandler((player, info) -> {
            SoundManager.playClick(player);
            BroadcastManager.get().broadcastPlayerRepaired(player); // Трансляция починки
            PlayerUtil.repairAll(player); // Починить всё
            player.updateInventory(); // Обновить инвентарь
            SoundManager.playSuccess(player);
        });
    }

    public void addClearButton(Slot slot) { // Добавить кнопку очистки инвентаря
        slot.setClickHandler((player, info) -> {
            SoundManager.playClick(player);
            player.getInventory().clear(); // Очистить инвентарь
            player.sendMessage(ChatColor.GREEN + "Инвентарь очищен"); // Инвентарь очищен
            SoundManager.playSuccess(player);
        });
    }

    public void addImport(Slot slot) { // Добавить импорт (из инвентаря)
        slot.setClickHandler((player, info) -> {
            SoundManager.playClick(player);
            Menu m = info.getClickedMenu();
            ItemStack[] inv;
            if (filterItemsOnImport) {
                inv = ItemFilter.get().filterItemStack(player.getInventory().getContents()); // Фильтровать предметы
            } else {
                inv = player.getInventory().getContents(); // Получить содержимое инвентаря
            }
            for (int i = 0; i < 41; i++) {
                m.getSlot(i).setItem(inv[i]);
            }
        });
    }

    public void addImportEC(Slot slot) { // Добавить импорт (из эндер-сундука)
        slot.setClickHandler((player, info) -> {
            SoundManager.playClick(player);
            Menu m = info.getClickedMenu();
            ItemStack[] inv;
            if (filterItemsOnImport) {
                inv = ItemFilter.get().filterItemStack(player.getEnderChest().getContents()); // Фильтровать предметы
            } else {
                inv = player.getEnderChest().getContents(); // Получить содержимое эндер-сундука
            }
            for (int i = 0; i < 27; i++) {
                m.getSlot(i + 9).setItem(inv[i]);
            }
        });
    }

    public void addEdit(Slot slot, int i) { // Добавить редактирование (кита)
        slot.setClickHandler((player, info) -> {
            SoundManager.playClick(player);
            OpenKitMenu(player, i); // Открыть меню кита
        });
    }

    public void addEditEC(Slot slot, int i) { // Добавить редактирование (эндер-сундука)
        slot.setClickHandler((player, info) -> {
            SoundManager.playClick(player);
            OpenECKitKenu(player, i); // Открыть меню эндер-сундука
        });
    }

    public void addLoad(Slot slot, int i) { // Добавить загрузку (кита)
        slot.setClickHandler((player, info) -> {
            SoundManager.playClick(player);
            KitManager.get().loadKit(player, i); // Загрузить кит
            info.getClickedMenu().close();
            SoundManager.playCloseGui(player);
        });
    }

    public void addEditLoad(Slot slot, int i) { // Добавить редактирование/загрузку (кита)
        slot.setClickHandler((player, info) -> {
            SoundManager.playClick(player);
            if (info.getClickType() == ClickType.LEFT) {
                KitManager.get().loadKit(player, i); // Загрузить кит
                info.getClickedMenu().close();
            } else {
                OpenKitMenu(player, i); // Открыть меню кита
            }
        });
    }

    public void addEditLoadEC(Slot slot, int i) { // Добавить редактирование/загрузку (эндер-сундука)
        slot.setClickHandler((player, info) -> {
            SoundManager.playClick(player);
            if (info.getClickType() == ClickType.LEFT) {
                KitManager.get().loadEnderchest(player, i); // Загрузить эндер-сундука
                info.getClickedMenu().close();
            } else {
                OpenECKitKenu(player, i); // Открыть меню эндер-сундука
            }
        });
    }

    public Menu createKitMenu(int slot) { // Создать меню кита
        return ChestMenu.builder(6).title(StyleManager.get().getPrimaryColor() + "Кит: " + slot).build(); // Кит
    }

    public Menu createPublicKitMenu(String id) { // Создать меню публичного кита
        return ChestMenu.builder(6).title(StyleManager.get().getPrimaryColor() + "Публичный кит: " + id).build(); // Публичный кит
    }

    public Menu createECMenu(int slot) { // Создать меню эндер-сундука
        return ChestMenu.builder(6).title(StyleManager.get().getPrimaryColor() + "Эндер-сундук: " + slot).build(); // Эндер-сундук
    }

    public Menu createInspectMenu(int slot, String playerName) { // Создать меню просмотра кита
        return ChestMenu.builder(6).title(StyleManager.get().getPrimaryColor() + "Просмотр кита " + playerName + " " + slot).build(); // Просмотр кита
    }

    public Menu createInspectEcMenu(int slot, String playerName) { // Создать меню просмотра эндер-сундука
        return ChestMenu.builder(6).title(StyleManager.get().getPrimaryColor() + "Просмотр эндер-сундука " + playerName + " " + slot).build(); // Просмотр эндер-сундука
    }

    public Menu createMainMenu(Player p) { // Создать главное меню
        return ChestMenu.builder(6).title(StyleManager.get().getPrimaryColor() + "Киты " + p.getName()).build(); // Киты игрока
    }

    public Menu createKitRoom() { // Создать меню комнаты китов
        return ChestMenu.builder(6).title(StyleManager.get().getPrimaryColor() + "Комната китов").redraw(true).build(); // Комната китов
    }

    public void allowModification(Slot slot) { // Разрешить модификацию
        ClickOptions options = ClickOptions.ALLOW_ALL;
        slot.setClickOptions(options);
    }

    private String getPlayerName(UUID uuid) { // Получить имя игрока
        Player onlinePlayer = Bukkit.getPlayer(uuid);
        if (onlinePlayer != null) {
            return onlinePlayer.getName();
        }
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
        String name = offlinePlayer.getName();
        return name != null ? name : uuid.toString();
    }
}
