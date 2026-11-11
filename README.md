# Ticket Booking System

Учебный проект: бронирование билетов в кино, на концерты и на матчи Казахстанской Премьер-Лиги (КПЛ).

- **backend**: Java 25, Spring Boot 3.3.5, Spring Web, Spring Data JPA, Spring Security + JWT (jjwt 0.12.6), Bean Validation, PostgreSQL
- **frontend**: React 18, Vite, react-router-dom 6.28.0

## Как запустить

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

Тесты: `mvn test` (51 тест). Тесты используют ту же базу `ticket_booking` и пересоздают её.

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
1. Пользователь входит и выбирает место. Создаётся бронь `Pending`, место становится `Reserved` на 10 минут.
2. В окне оплаты можно выбрать сохранённую карту или ввести новую и поставить галочку «Сохранить карту».
3. Карта на `0000` → оплата `Failed`, бронь `Cancelled`, место `Available`. Иначе → оплата `Paid`, бронь `Confirmed`, место `Booked`.
4. Задача `@Scheduled` раз в минуту отменяет брони `Pending` старше 10 минут и освобождает места.
5. Оплаченные билеты видны в личном кабинете с номером электронного билета (`CIN-000123`, `CON-000045`, `KPL-000678`). Свою бронь можно отменить там же.

## Эндпоинты

Без токена: ответ 401. С токеном покупателя на админский эндпоинт: 403. Ошибки валидации: 400 с полем `errors` по каждому полю.

Заголовок авторизации: `Authorization: Bearer <token>`.

### Авторизация и профиль
| Метод | URL | Доступ | Что делает |
|---|---|---|---|
| POST | /api/auth/register | все | регистрация: `fullName`, `email`, `phone`, `password`; email занят → 409 |
| POST | /api/auth/login | все | вход, возвращает `token` и `user` (id, fullName, email, phone, role) |
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

### Кино
| Метод | URL | Доступ | Что делает |
|---|---|---|---|
| GET | /api/cinema/movies | все | список фильмов с описанием и постером |
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
| POST | /api/cinema/bookings | вошедшие | забронировать место: `{ "seatId": 5 }` |
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
| POST | /api/concert/bookings | вошедшие | забронировать место |
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
| POST | /api/sport/bookings | вошедшие | забронировать место |
| GET | /api/sport/bookings | ADMIN | все брони матчей |
| PUT | /api/sport/bookings/{id}/cancel | ADMIN | отменить любую бронь |

### Оплата
| Метод | URL | Доступ | Что делает |
|---|---|---|---|
| POST | /api/payments/{type}/{bookingId} | владелец брони | оплатить бронь `Pending` (`type`: cinema, concert, sport) |

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
| `/cinema`, `/concerts`, `/sport` | списки событий |
| `/movies/:id` | фильм: описание, постер, сеансы по дням |
| `/sessions/:id` | схема кинозала, переключатель времени сеансов |
| `/concerts/:id` | концерт: схема зала со сценой и зонами |
| `/matches/:id` | матч: схема Центрального стадиона Алматы с секторами |
| `/login`, `/register` | вход и регистрация |
| `/account` | личный кабинет: Мои билеты, Мои карты, Профиль (только для вошедших) |
| `/admin` | админка (только для ADMIN) |
| `*` | страница 404 |

## Таблицы
Таблицы кино `users`, `movies`, `sessions`, `seats`, `bookings`, `payments` совпадают со схемой учебного задания. Новые данные лежат в отдельных таблицах со связью 1:1:
- `user_credentials` — хеш пароля (BCrypt) и роль `CUSTOMER` или `ADMIN`;
- `movie_details` — описание, постер, жанр, дата выхода, премьера.

Также добавлены `payment_cards`, `concert_payments`, `match_payments`, а у `concerts` и `matches` — колонки `description` и `poster_url`.

Постеры — заглушки с https://picsum.photos. Если картинка не загрузилась, показывается градиентная плашка с названием.
