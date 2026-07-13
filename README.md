# BloodRP

BloodRP добавляет RP-действия между игроками, систему браков, автоматический поцелуй супругов и топ
самых долгих браков.

## Возможности

- Действия по запросу: `bj`, `doggy`, `kiss`.
- Брак через `/marry <player>` с подтверждением второй стороны.
- Развод через `/unmarry` или `/divorce`.
- Автоматический поцелуй при sneaking рядом с супругом.
- Автоматическая неблокирующая синхронизация PDC брака с базой данных при входе игрока.
- Обновление `last_interaction_at` при поцелуе супругов.
- Автоматический распад брака при долгом отсутствии взаимодействий.
- Топ браков по длительности через `/marry top`.
- Настраиваемые дистанции и MiniMessage header/footer для топа.

## Зависимости

Плагин рассчитан на Paper/Folia и объявляет зависимости:

- `CommonBloodLib`
- `GSit`
- `BetterModel`

Для `/marry top` также нужен `BloodOfflinePlayersAPIClient` и `BloodOfflinePlayersAPICommon` на runtime classpath.
Команда резолвит ники через Offline Players API асинхронно и не блокирует main thread.

## Команды

| Команда                        | Право            | Что делает                               |
|--------------------------------|------------------|------------------------------------------|
| `/bj <player>`                 | `bloodrp.sex`    | Отправляет игроку запрос на `bj`.        |
| `/doggy <player>`              | `bloodrp.sex`    | Отправляет игроку запрос на `doggy`.     |
| `/sex <player>`                | `bloodrp.sex`    | Алиас для `/doggy <player>`.             |
| `/kiss <player>`               | `bloodrp.sex`    | Отправляет игроку запрос на поцелуй.     |
| `/marry <player>`              | `bloodrp.sex`    | Отправляет игроку предложение брака.     |
| `/marry top`                   | `bloodrp.sex`    | Показывает топ 10 самых долгих браков.   |
| `/unmarry`                     | `bloodrp.sex`    | Разводит игрока с текущим супругом.      |
| `/divorce`                     | `bloodrp.sex`    | Алиас для `/unmarry`.                    |
| `/bloodrp accept <request-id>` | `bloodrp.sex`    | Принимает конкретный запрос по ID.       |
| `/bloodrp reload`              | `bloodrp.reload` | Перезагружает `config.yml` без рестарта. |
| `/rape bj <player>`            | `bloodrp.rape`   | Запускает `bj` без подтверждения.        |
| `/rape doggy <player>`         | `bloodrp.rape`   | Запускает `doggy` без подтверждения.     |

## Права

| Право                 | Default | Назначение                                             |
|-----------------------|---------|--------------------------------------------------------|
| `bloodrp.sex`         | `op`    | Доступ к обычным действиям, браку и принятию запросов. |
| `bloodrp.rape`        | `op`    | Доступ к direct-действиям без подтверждения.           |
| `bloodrp.rape.immune` | `false` | Игрока с этим правом нельзя выбрать для `/rape`.       |
| `bloodrp.reload`      | `op`    | Доступ к `/bloodrp reload`.                            |

Пример для LuckPerms:

```text
/lp group default permission set bloodrp.sex true
/lp group admin permission set bloodrp.reload true
/lp group admin permission set bloodrp.rape true
```

## Как пользоваться

Обычные действия работают через запрос:

```text
/kiss PlayerName
```

Второй игрок получит сообщение с кнопкой принятия. Также можно принять вручную:

```text
/bloodrp accept <request-id из кнопки>
```

ID привязывает принятие к конкретному действию и действует одну минуту.

## Топ браков

Команда:

```text
/marry top
```

Вывод строится так:

```text
<header из config.yml>
1. Ник1 и Ник2 женаты уже 192 д. 12 ч.
2. Ник3 и Ник4 женаты уже 81 д. 5 ч.
<footer из config.yml>
```

## Конфиг

Файл: `plugins/BloodRP/config.yml`

```yaml
maxActionDistance: 16.0
maxRapeDistance: 5.0
maxDaysWithoutInteraction: 14
warningDaysWithoutInteraction: 13
sneakKissCooldownSeconds: 5

topMarriages:
  header: "<gold><bold>Топ браков</bold></gold>"
  footer: "<gray>Новые браки попадают сюда автоматически.</gray>"
```

Поля:

| Поле                            | Что делает                                                |
|---------------------------------|-----------------------------------------------------------|
| `maxActionDistance`             | Максимальная дистанция для принятия обычного запроса.     |
| `maxRapeDistance`               | Максимальная дистанция для direct-действий через `/rape`. |
| `maxDaysWithoutInteraction`     | Через сколько дней без поцелуя брак распадется.           |
| `warningDaysWithoutInteraction` | Через сколько дней без поцелуя предупреждать при входе.   |
| `sneakKissCooldownSeconds`      | Кулдаун автопоцелуя супругов при sneaking, в секундах.    |
| `topMarriages.header`           | MiniMessage-строка перед списком браков.                  |
| `topMarriages.footer`           | MiniMessage-строка после списка браков.                   |

После изменения конфига:

```text
/bloodrp reload
```
