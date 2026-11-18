# Ticket Booking System

Учебный проект: бронирование билетов в кино, на концерты и на матчи Казахстанской Премьер-Лиги (КПЛ).

- **backend**: Java 25, Spring Boot 3.3.5, Spring Web, Spring Data JPA, Spring Security + JWT (jjwt 0.12.6), Bean Validation, PostgreSQL
- **frontend**: React 18, Vite, react-router-dom 6.28.0

## Как запустить

Деплой на хостинг (Railway) — см. [DEPLOY.md](DEPLOY.md).

### 1. База данных
1. Установите PostgreSQL и создайте базу: `CREATE DATABASE ticket_booking;`
2. Логин и пароль базы указаны в `backend/src/main/resources/application.properties` (по умолчанию `postgres` / `12345`).
3. Таблицы и тестовые данные создаются из `schema.sql` и `data.sql` **при каждом запуске backend и тестов**: база каждый раз пересоздаётся.
   Все даты в `data.sql` считаются от текущей даты (`CURRENT_DATE`), поэтому события всегда в будущем.

### 2. Backend
```
cd backend
mvn spring-boot:run
```
API: http://localhost:8080/api

Тесты: `mvn test` (79 тестов). Тесты используют ту же базу `ticket_booking` и пересоздают её.

### 3. Frontend
```
cd frontend
npm install
npm run dev
```
Сайт: http://localhost:5173

Если backend запущен на другом порту, укажите адрес API:
```
VITE_API_URL=http://localhost:8099/api npm run dev
```

### Настройки в application.properties
| Ключ | Что это |
|---|---|
| `app.jwt.secret` | секрет для подписи JWT (не меньше 32 символов) |
| `app.jwt.expiration-minutes` | сколько живёт токен (по умолчанию 1440 = сутки) |
| `app.cors.allowed-origin` | адрес фронтенда для CORS (`http://localhost:5173`) |
| `app.admin.*` | данные админа, который создаётся при старте |
| `app.demo-user.*` | пароль для тестового покупателя из `data.sql` |
| `app.booking.reservation-minutes` | сколько минут место держится без оплаты (10) |
| `app.booking.max-seats` | сколько мест можно взять в один заказ (6) |
| `app.verification.code-minutes` | сколько живёт код подтверждения email (15) |
| `app.verification.resend-seconds` | через сколько секунд можно запросить новый код (60) |
| `app.verification.max-attempts` | сколько раз можно ошибиться в коде (5) |

### Отправка кода подтверждения на email
При регистрации пользователь получает на почту 6-значный код и входит на сайт только после его ввода.
**Проще всего — Mailpit** (локальный почтовый ящик, без регистраций и паролей):
```
brew install mailpit
mailpit                                   # оставить запущенным
cp backend/mail-local.properties.example backend/mail-local.properties
```
Письма с кодами открываются в браузере на http://localhost:8025. В настоящий ящик они не уходят — для этого нужен вариант 2 в том же файле (Gmail с паролем приложения).

Для настоящей отправки: в `backend/mail-local.properties` раскомментируйте вариант 2 и впишите адрес и пароль приложения.
Этот файл в `.gitignore` — пароль не попадёт в репозиторий. Backend подхватит его при запуске сам.

Другой способ — **переменные окружения**. Пример для Gmail:

1. Включите двухэтапную проверку в аккаунте Google и создайте «Пароль приложения» (Google Account → Безопасность → Пароли приложений).
2. Запустите backend с переменными:
```
export SPRING_MAIL_HOST=smtp.gmail.com
export SPRING_MAIL_PORT=587
export SPRING_MAIL_USERNAME=your.address@gmail.com
export SPRING_MAIL_PASSWORD="xxxx xxxx xxxx xxxx"   # пароль приложения, не обычный пароль
mvn spring-boot:run
```
В IntelliJ IDEA эти переменные указываются в Run Configuration → Environment variables.

**Если `SPRING_MAIL_HOST` не задан**, письмо не отправляется, а код печатается в лог backend строкой
`Код подтверждения для name@mail.kz: 123456` — так проект можно запустить и проверить без почтового сервера.
В тестах отправка писем заменена заглушкой.

## Тестовые аккаунты
| Роль | Email | Пароль |
|---|---|---|
| Администратор | `admin@ticketbooking.kz` | `Admin12345` |
| Покупатель | `amina@gmail.com` | `Amina12345` |

Админ создаётся при старте backend, если его ещё нет. Новых покупателей можно зарегистрировать на странице `/register`.

## Тестовые карты
Оплата ненастоящая: деньги не списываются, полный номер карты и CVV нигде не сохраняются (в базе только последние 4 цифры и бренд). Номер проверяется по алгоритму Луна, срок действия не должен быть в прошлом, CVV — 3 или 4 цифры.

| Номер | Результат |
|---|---|
| `4242 4242 4242 4242` | оплата проходит: бронь Confirmed, место Booked |
| `4000 0000 0002 0000` | банк отклоняет: оплата Failed, бронь Cancelled, место снова Available |

Правило отказа: любая карта, номер которой заканчивается на `0000`. Номер `4000 0000 0000 0000` для отказа не подходит: он не проходит проверку Луна, и сервер ответит 400 «Неверный номер карты». Поэтому для отказа используйте `4000 0000 0002 0000`.

## Как работает бронь
1. Пользователь входит и выбирает до 6 мест одного события. На каждое место создаётся бронь `Pending`, места становятся `Reserved` на 10 минут.
2. В окне оплаты можно выбрать сохранённую карту или ввести новую и поставить галочку «Сохранить карту».
3. Все места заказа оплачиваются одной картой. Карта на `0000` → все оплаты `Failed`, брони `Cancelled`, места `Available`. Иначе → оплаты `Paid`, брони `Confirmed`, места `Booked`, на каждое место свой номер билета.
4. Задача `@Scheduled` раз в минуту отменяет брони `Pending` старше 10 минут и освобождает места.
5. Оплаченные билеты видны в личном кабинете с номером электронного билета (`CIN-000123`, `CON-000045`, `KPL-000678`). Свою бронь можно отменить там же.

## Эндпоинты

Без токена: ответ 401. С токеном покупателя на админский эндпоинт: 403. Ошибки валидации: 400 с полем `errors` по каждому полю.

Заголовок авторизации: `Authorization: Bearer <token>`.

### Авторизация и профиль
| Метод | URL | Доступ | Что делает |
|---|---|---|---|
| POST | /api/auth/register | все | регистрация: `fullName`, `email`, `phone`, `password`; отправляет код на email, токен **не** выдаёт; email уже подтверждён → 409. Повторная регистрация неподтверждённого email обновляет данные и шлёт новый код |
| POST | /api/auth/verify | все | `{ "email", "code" }` → подтверждает email и возвращает `token` и `user`. Неверный/просроченный код → 400 |
| POST | /api/auth/resend-code | все | `{ "email" }` → новый код; раньше чем через 60 секунд → 429 |
| POST | /api/auth/login | все | вход, возвращает `token` и `user`; email не подтверждён → 403 `{ "error": "EMAIL_NOT_VERIFIED" }` |
| GET | /api/me | вошедшие | текущий пользователь из токена |
| PUT | /api/me | вошедшие | изменить имя и телефон |
| GET | /api/me/bookings | вошедшие | мои брони кино, концертов и матчей одним списком |
| PUT | /api/me/bookings/{type}/{id}/cancel | вошедшие | отменить свою бронь (`type`: cinema, concert, sport); чужая → 403 |
| GET | /api/me/cards | вошедшие | мои карты |
| POST | /api/me/cards | вошедшие | добавить карту |
| DELETE | /api/me/cards/{id} | вошедшие | удалить свою карту |

### Главная
| Метод | URL | Доступ | Что делает |
|---|---|---|---|
| GET | /api/home | все | секции: Премьеры, Новинки кино, Концерты, Матчи КПЛ, Скоро |

### Поиск
| Метод | URL | Доступ | Что делает |
|---|---|---|---|
| GET | /api/search?q=текст | все | поиск по фильмам, концертам и матчам сразу (минимум 2 символа). Без учёта регистра, «ё» = «е», все слова запроса должны найтись |

### Отзывы и рейтинг
Оценка от 1 до 5 звёзд и необязательный комментарий. Один пользователь — один отзыв на событие (повторная отправка обновляет его).
`eventType`: `movie`, `concert`, `match`. У фильмов, концертов и матчей в ответах API есть поля `rating` (средняя оценка) и `reviewCount`.

| Метод | URL | Доступ | Что делает |
|---|---|---|---|
| GET | /api/reviews/{eventType}/{eventId} | все | средняя оценка, распределение по звёздам и список отзывов (свой — первым, с `mine: true`) |
| POST | /api/reviews/{eventType}/{eventId} | вошедшие | поставить или изменить оценку: `{ "rating": 5, "comment": "..." }` |
| DELETE | /api/reviews/{reviewId} | автор или ADMIN | удалить отзыв; чужой → 403 |

### Фильтры списков
Все параметры необязательные, их можно комбинировать. Неверное значение → 400.

| URL | Параметры |
|---|---|
| GET /api/cinema/movies | `q` (название, жанр, описание), `genre`, `ageRating`, `minRating`, `minDuration`, `maxDuration` (минуты), `date` (есть сеанс в этот день, `2026-11-20`), `sort`: `rating` (по умолчанию), `title`, `release`, `duration`, `durationDesc` |
| GET /api/concert/concerts | `q`, `city`, `genre`, `minRating`, `minDuration`, `maxDuration`, `dateFrom`, `dateTo`, `sort`: `date` (по умолчанию), `rating`, `duration`, `durationDesc` |
| GET /api/sport/matches | `q` (команда, город, стадион, по-русски тоже), `teamId`, `minRating`, `dateFrom`, `dateTo`, `sort`: `date` (по умолчанию), `rating` |

Пример: `/api/cinema/movies?genre=Мультфильм&maxDuration=110&sort=rating`

### Кино
| Метод | URL | Доступ | Что делает |
|---|---|---|---|
| GET | /api/cinema/movies | все | список фильмов с описанием, постером и рейтингом (фильтры — см. выше) |
| GET | /api/cinema/movies/{id} | все | один фильм |
| GET | /api/cinema/movies/{id}/sessions | все | сеансы фильма |
| POST | /api/cinema/movies | ADMIN | добавить фильм |
| PUT | /api/cinema/movies/{id} | ADMIN | изменить фильм |
| DELETE | /api/cinema/movies/{id} | ADMIN | удалить фильм |
| GET | /api/cinema/sessions | все | все сеансы |
| GET | /api/cinema/sessions/{id} | все | один сеанс |
| POST | /api/cinema/sessions | ADMIN | добавить сеанс |
| PUT | /api/cinema/sessions/{id} | ADMIN | изменить сеанс |
| DELETE | /api/cinema/sessions/{id} | ADMIN | удалить сеанс |
| GET | /api/cinema/sessions/{id}/seats | все | места на сеанс (номер в формате `ряд-место`, например `3-7`) |
| POST | /api/cinema/sessions/{id}/seats | ADMIN | добавить ряд мест: `rowNumber`, `seatCount`, `price` |
| POST | /api/cinema/bookings | вошедшие | забронировать места: `{ "seatIds": [5, 6, 7] }` (до 6 мест одного сеанса) или `{ "seatId": 5 }`; возвращает список броней. Если хоть одно место занято — 409 и не бронируется ни одно |
| GET | /api/cinema/bookings | ADMIN | все брони кино |
| PUT | /api/cinema/bookings/{id}/cancel | ADMIN | отменить любую бронь |

### Концерты
| Метод | URL | Доступ | Что делает |
|---|---|---|---|
| GET | /api/concert/concerts | все | список концертов |
| GET | /api/concert/concerts/{id} | все | один концерт |
| POST / PUT / DELETE | /api/concert/concerts, /api/concert/concerts/{id} | ADMIN | добавить, изменить, удалить |
| GET | /api/concert/concerts/{id}/seats | все | места по зонам (VIP, Fan zone, Tribune) |
| POST | /api/concert/concerts/{id}/seats | ADMIN | добавить ряд: `zone`, `rowNumber`, `seatCount`, `price` |
| POST | /api/concert/bookings | вошедшие | забронировать места `{ "seatIds": [...] }` (до 6 мест одного концерта) |
| GET | /api/concert/bookings | ADMIN | все брони концертов |
| PUT | /api/concert/bookings/{id}/cancel | ADMIN | отменить любую бронь |

### Матчи КПЛ
| Метод | URL | Доступ | Что делает |
|---|---|---|---|
| GET | /api/sport/teams | все | клубы КПЛ |
| GET | /api/sport/matches | все | список матчей |
| GET | /api/sport/matches/{id} | все | один матч |
| POST / PUT / DELETE | /api/sport/matches, /api/sport/matches/{id} | ADMIN | добавить, изменить, удалить |
| GET | /api/sport/matches/{id}/seats | все | места по секторам (Западная, Восточная, Северная, Южная, VIP) |
| POST | /api/sport/matches/{id}/seats | ADMIN | добавить ряд: `zone` (сектор), `rowNumber`, `seatCount`, `price` |
| POST | /api/sport/bookings | вошедшие | забронировать места `{ "seatIds": [...] }` (до 6 мест одного матча) |
| GET | /api/sport/bookings | ADMIN | все брони матчей |
| PUT | /api/sport/bookings/{id}/cancel | ADMIN | отменить любую бронь |

### Оплата
| Метод | URL | Доступ | Что делает |
|---|---|---|---|
| POST | /api/payments/{type} | владелец броней | оплатить сразу несколько броней одной картой: в теле те же поля + `"bookingIds": [12, 13]`. Ответ: общая сумма `amount`, `bookingIds`, `ticketNumbers` |
| POST | /api/payments/{type}/{bookingId} | владелец брони | оплатить одну бронь `Pending` (`type`: cinema, concert, sport) |

Оплата сохранённой картой:
```json
{ "cardId": 1 }
```
Оплата новой картой:
```json
{
  "newCard": { "cardNumber": "4242 4242 4242 4242", "holderName": "AMINA SEITKALI", "expMonth": 12, "expYear": 2030, "cvv": "123" },
  "saveCard": true
}
```

### Пользователи
| Метод | URL | Доступ | Что делает |
|---|---|---|---|
| GET | /api/users | ADMIN | все пользователи |
| GET | /api/users/{id} | ADMIN | один пользователь |
| POST | /api/users | ADMIN | добавить пользователя без пароля |

## Страницы фронтенда
| URL | Страница |
|---|---|
| `/` | главная: баннер и карусели рекомендаций |
| `/cinema`, `/concerts`, `/sport` | списки событий с поиском и фильтрами (рейтинг, длительность, дата, жанр, город, команда); фильтры хранятся в адресе страницы |
| `/movies/:id` | фильм: описание, постер, сеансы по дням |
| `/sessions/:id` | схема кинозала, переключатель времени сеансов |
| `/concerts/:id` | концерт: схема зала со сценой и зонами |
| `/matches/:id` | матч: схема Центрального стадиона Алматы с секторами |
| `/login`, `/register` | вход и регистрация |
| `/verify` | ввод кода из письма, повторная отправка кода |
| `/search?q=` | поиск по всему сайту (строка поиска в шапке) |
| `/account` | личный кабинет: Мои билеты, Мои карты, Профиль (только для вошедших) |
| `/admin` | админка (только для ADMIN) |
| `*` | страница 404 |

## Таблицы
Таблицы кино `users`, `movies`, `sessions`, `seats`, `bookings`, `payments` совпадают со схемой учебного задания. Новые данные лежат в отдельных таблицах со связью 1:1:
- `user_credentials` — хеш пароля (BCrypt) и роль `CUSTOMER` или `ADMIN`;
- `movie_details` — описание, постер, жанр, дата выхода, премьера.

Также добавлены `payment_cards`, `concert_payments`, `match_payments`, а у `concerts` и `matches` — колонки `description` и `poster_url`.

Добавлена таблица `reviews` (оценка 1–5, комментарий, одна запись на пользователя и событие), `email_verifications` (хеш кода, срок действия, число попыток),
у `user_credentials` — `email_verified`, у `concerts` — `duration_min` и `genre`, у `teams` — `logo_url`.

## Контент
- **Фильмы** — реальные релизы 2026 года. Названия в российском прокате, длительность и даты выхода взяты из Википедии; фильмы, которые ещё не вышли («Голодные игры: Рассвет жатвы», «Мстители: Доктор Дум», «Дюна: Часть третья»), попадают в раздел «Скоро» и продаются в предпродажу на первые дни проката.
- **Концерты** — реальные артисты (Димаш Кудайберген, Кайрат Нуртас, Роза Рымбаева, А'Студио, Скриптонит, Imanbek, Jah Khalib, MiyaGi & Andy Panda, Макс Корж, Jony) и реальные площадки Алматы и Астаны. Даты, цены и программа концертов — учебные, это не анонсы настоящих концертов.
- **Матчи** — клубы КПЛ с их настоящими эмблемами; постер матча собирается из эмблем обеих команд на фоне фото стадиона хозяев.
- **Картинки** лежат в `frontend/public/images` (постеры фильмов, фото артистов, эмблемы, стадионы) и взяты из Википедии / Wikimedia Commons. Используются в учебных целях; постеры фильмов и эмблемы клубов принадлежат их правообладателям.
- Отзывы в `data.sql` написаны от имени тестовых пользователей, чтобы фильтр по рейтингу было на чём проверить.

Если картинка не загрузилась, показывается градиентная плашка с названием.
