# API PerPlayerKit

API PerPlayerKit - это простое Java API, которое позволяет разработчикам взаимодействовать с плагином. API **НЕ**стабильно и может измениться в будущем.

### Пример использования:

Добавьте jar-файл плагина в папку `./lib` вашего проекта.

Добавьте в pom.xml:

```
<dependency>
    <groupId>com.local</groupId>
    <artifactId>PerPlayerKit</artifactId>
    <version>local</version>
    <scope>system</scope>
    <systemPath>${project.basedir}/lib/PerPlayerKit-1.1.jar</systemPath>
</dependency>
```

```java
import dev.noah.perplayerkit.API;
import dev.noah.perplayerkit.PublicKit;
// другие импорты и т.д.

public class ExamplePlugin extends JavaPlugin {

    public void onEnable() {
        // Код при включении...
    }


// Выдать игроку публичный кит при входе на сервер
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        // Получить экземпляр API
        API api = API.getInstance();

        // Получить список всех публичных китов
        List<PublicKit> publicKits = api.getPublicKits();

        // Загрузить публичный кит
        api.loadPublicKit(e.getPlayer(), publicKits.get(0));
    }

}
```
