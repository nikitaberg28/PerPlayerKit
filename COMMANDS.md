# Документация по командам PerPlayerKit

- **Версия:** 1.7.0
- **Автор(ы):** Noah Ross, NikitaBerg(автор перевода)
- **Минимальная версия Spigot/Paper:** 1.19

## Команды

В следующей таблице перечислены каждая команда, её использование, псевдонимы и требуемые разрешения.

| Команда             | Псевдонимы               | Разрешение                     |
|---------------------|--------------------------|--------------------------------|
| `perplayerkit`      | `N/A`                    | `perplayerkit.admin`           |
| `aboutperplayerkit` | `N/A`                    | `N/A`                          |
| `kitroom`           | `N/A`                    | `perplayerkit.admin`           |
| `kit`               | `k`                      | `perplayerkit.menu`            |
| `copykit`           | `copyec, copyenderchest` | `perplayerkit.copykit`         |
| `sharekit`          | `N/A`                    | `perplayerkit.sharekit`        |
| `shareec`           | `shareenderchest`        | `perplayerkit.shareenderchest` |
| `swapkit`           | `N/A`                    | `perplayerkit.swapkit`         |
| `deletekit`         | `N/A`                    | `perplayerkit.deletekit`       |
| `inspectkit`        | `N/A`                    | `perplayerkit.staff`           |
| `inspectec`         | `N/A`                    | `perplayerkit.staff`           |
| `publickit`         | `pk, premadekit`         | `perplayerkit.publickit`       |
| `k1`                | `kit1`                   | `perplayerkit.kit`             |
| `k2`                | `kit2`                   | `perplayerkit.kit`             |
| `k3`                | `kit3`                   | `perplayerkit.kit`             |
| `k4`                | `kit4`                   | `perplayerkit.kit`             |
| `k5`                | `kit5`                   | `perplayerkit.kit`             |
| `k6`                | `kit6`                   | `perplayerkit.kit`             |
| `k7`                | `kit7`                   | `perplayerkit.kit`             |
| `k8`                | `kit8`                   | `perplayerkit.kit`             |
| `k9`                | `kit9`                   | `perplayerkit.kit`             |
| `ec1`               | `enderchest1`            | `perplayerkit.enderchest`      |
| `ec2`               | `enderchest2`            | `perplayerkit.enderchest`      |
| `ec3`               | `enderchest3`            | `perplayerkit.enderchest`      |
| `ec4`               | `enderchest4`            | `perplayerkit.enderchest`      |
| `ec5`               | `enderchest5`            | `perplayerkit.enderchest`      |
| `ec6`               | `enderchest6`            | `perplayerkit.enderchest`      |
| `ec7`               | `enderchest7`            | `perplayerkit.enderchest`      |
| `ec8`               | `enderchest8`            | `perplayerkit.enderchest`      |
| `ec9`               | `enderchest9`            | `perplayerkit.enderchest`      |
| `enderchest`        | `ec`                     | `perplayerkit.viewenderchest`  |
| `savepublickit`     | `N/A`                    | `perplayerkit.admin`           |
| `regear`            | `rg`                     | `perplayerkit.regear`          |
| `heal`              | `N/A`                    | `perplayerkit.heal`            |
| `repair`            | `N/A`                    | `perplayerkit.repair`          |

## Подробности команды пополнения снаряжения (Regear)

Система пополнения снаряжения позволяет игрокам пополнять предметы из загруженного кита. Поведение команд `/rg` и `/regear` можно настраивать независимо:

### Режимы

**Режим команды**: Напрямую пополняет предметы из белого списка из загруженного кита игрока
- Применяется перезарядка между использованиями
- Таймер урона предотвращает пополнение снаряжения во время боя
- Пополняются только предметы из белого списка

**Режим шалкера**: Даёт игроку шалкер пополнения снаряжения
- Игрок ставит шалкер на землю, чтобы открыть специальный интерфейс
- Игрок нажимает на оболочку пополнения внутри, чтобы вызвать пополнение
- Применяется перезарядка между использованиями команды
- Таймер урона предотвращает пополнение снаряжения во время боя

### Конфигурация

Обе команды `/rg` и `/regear` могут использовать разные режимы. См. **CONFIG.md** → **Команда пополнения снаряжения** для параметров конфигурации (`rg-mode` и `regear-mode`).

## Разрешения

В следующей таблице перечислены каждое разрешение верхнего уровня и разрешения, которые оно предоставляет.

| Разрешение         | Предоставляет                                                                                                                                                                                                                                                                                                                                                                                       |
|--------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `kit.admin`        | `perplayerkit.admin`                                                                                                                                                                                                                                                                                                                                                                         |
| `kit.staff`        | `perplayerkit.staff`                                                                                                                                                                                                                                                                                                                                                                         |
| `kit.use`          | `perplayerkit.use`                                                                                                                                                                                                                                                                                                                                                                           |
| `perplayerkit.use` | `perplayerkit.menu`, `perplayerkit.copykit`, `perplayerkit.sharekit`, `perplayerkit.shareenderchest`, `perplayerkit.swapkit`, `perplayerkit.deletekit`, `perplayerkit.publickit`, `perplayerkit.kit`, `perplayerkit.enderchest`, `perplayerkit.viewenderchest`, `perplayerkit.regear`, `perplayerkit.heal`, `perplayerkit.repair`, `perplayerkit.rekitonrespawn`, `perplayerkit.rekitonkill` |

## Уведомления сообщениями

Следующее разрешение контролирует, какие уведомления о действиях, связанных с китами, видят игроки:

| Разрешение              | Назначение                                                                                          |
|-------------------------|--------------------------------------------------------------------------------------------------|
| `perplayerkit.kitnotify` | Позволяет игрокам видеть уведомления о действиях, связанных с китами (например, когда другие игроки загружают киты, чинят снаряжение и т.д.). **По умолчанию `true`** - все игроки могут видеть эти сообщения по умолчанию. Установите в `false`, чтобы скрыть все сообщения о действиях с китами от игрока. |
