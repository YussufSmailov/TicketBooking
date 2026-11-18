# Деплой на Railway

Проект на Railway — это 4 сервиса в одном проекте:

```
Railway-проект
├── Postgres   — база данных (готовый сервис Railway)
├── backend    — Spring Boot, папка backend/   → https://backend-xxxx.up.railway.app/api
├── frontend   — сайт, папка frontend/         → https://frontend-xxxx.up.railway.app
└── mailpit    — почтовый ящик для теста       → https://mailpit-xxxx.up.railway.app (с паролем)
```

> **Названия сервисов важны.** Переменные ниже ссылаются на сервисы по имени: `Postgres`, `backend`,
> `frontend`, `mailpit`. Если назовёте иначе, поправьте имена в `${{...}}`.

Цена: при регистрации Railway даёт пробные $5, потом тариф Hobby — $5 в месяц. Этого хватает на все 4 сервиса.

---

## 1. Создать проект

1. Зарегистрируйтесь на https://railway.com (удобнее через GitHub).
2. **New Project → Empty Project.**

## 2. База данных

1. В проекте: **+ Create → Database → PostgreSQL.**
2. Сервис будет называться `Postgres`. Больше ничего делать не нужно.

Таблицы и тестовые данные backend создаст сам при **первом** запуске, на пустой базе.
При следующих запусках и обновлениях база не трогается.

## 3. Почтовый ящик Mailpit

1. **+ Create → Docker Image**, образ: `axllent/mailpit:v1.34.0`.
2. Переименуйте сервис в `mailpit`: клик по названию → Settings → Service Name.
3. **Variables** → добавьте:
   ```
   MP_UI_AUTH=tester:придумайте-пароль
   ```
   Это логин и пароль для входа в ящик. Без него любой по ссылке прочитает все коды.
4. **Settings → Networking → Generate Domain**, порт **8025**.

Почтовый порт 1025 наружу не открывается: backend достучится до Mailpit по внутренней сети Railway.

## 4. Backend

1. **+ Create → GitHub Repo** → выберите `TicketBooking`.
2. Переименуйте сервис в `backend`.
3. **Settings → Source → Root Directory:** `/backend`.
   Railway найдёт там `Dockerfile` и `railway.json` и соберёт по ним.
4. **Settings → Networking → Generate Domain**, порт **8080**.
5. **Variables → Raw Editor**, вставьте и замените значения в угловых скобках:
   ```
   SPRING_PROFILES_ACTIVE=prod

   PGHOST=${{Postgres.PGHOST}}
   PGPORT=${{Postgres.PGPORT}}
   PGDATABASE=${{Postgres.PGDATABASE}}
   PGUSER=${{Postgres.PGUSER}}
   PGPASSWORD=${{Postgres.PGPASSWORD}}

   APP_JWT_SECRET=<длинная случайная строка, минимум 32 символа>
   APP_ADMIN_PASSWORD=<пароль для admin@ticketbooking.kz>
   APP_DEMO_USER_PASSWORD=<пароль для тестового покупателя amina@gmail.com>
   APP_CORS_ALLOWED_ORIGIN=https://${{frontend.RAILWAY_PUBLIC_DOMAIN}}

   SPRING_MAIL_HOST=${{mailpit.RAILWAY_PRIVATE_DOMAIN}}
   SPRING_MAIL_PORT=1025
   MAIL_SMTP_AUTH=false
   MAIL_SMTP_STARTTLS=false
   ```
   Случайную строку для `APP_JWT_SECRET` можно получить командой `openssl rand -base64 48`.

Если какой-то обязательной переменной нет, backend не запустится и напишет в логах,
какой именно: `Could not resolve placeholder 'APP_JWT_SECRET'`.

## 5. Frontend

1. **+ Create → GitHub Repo** → снова `TicketBooking`.
2. Переименуйте сервис в `frontend`.
3. **Settings → Source → Root Directory:** `/frontend`.
4. **Settings → Networking → Generate Domain**, порт **8080**.
5. **Variables:**
   ```
   VITE_API_URL=https://${{backend.RAILWAY_PUBLIC_DOMAIN}}/api
   ```
   Адрес API вшивается в сайт **при сборке**. Если поменяли эту переменную — нужен Redeploy frontend.

## 6. Запустить

Нажмите **Deploy** (или **Apply changes**) вверху. Первая сборка занимает 3–5 минут.
Если backend или frontend собрались раньше, чем вы задали переменные, сделайте у них **Redeploy**
(три точки у последнего деплоя → Redeploy).

## 7. Проверить

1. Откройте домен `frontend` — должна открыться главная с фильмами.
2. Зарегистрируйтесь с любым email.
3. Откройте домен `mailpit`, войдите с логином и паролем из `MP_UI_AUTH` — там письмо с кодом.
4. Введите код на сайте — вы в аккаунте.
5. Админка: `admin@ticketbooking.kz` и пароль из `APP_ADMIN_PASSWORD`.

## Обновления

Каждый `git push` в ветку `main` Railway подхватывает сам и пересобирает backend и frontend.
Данные в базе при этом сохраняются.

---

## Если что-то не работает

| Что видно | Причина и что делать |
|---|---|
| Сайт открывается, но «Ошибка сервера» / ничего не грузится | В консоли браузера (F12) ошибка CORS → `APP_CORS_ALLOWED_ORIGIN` должен быть ровно `https://домен-frontend` без `/` в конце. Запросы идут на `localhost` → у frontend не задан `VITE_API_URL`, задайте и сделайте Redeploy |
| Backend падает при запуске | Deploy Logs: `Could not resolve placeholder 'X'` — не задана переменная X. `Connection refused` к базе — проверьте переменные `PG*` |
| Регистрация: «Не удалось отправить письмо с кодом» | Сервис Mailpit не запущен или называется не `mailpit`; проверьте `SPRING_MAIL_HOST` |
| Обновление страницы, например /movies/4, даёт 404 | Frontend собран не из `Dockerfile` — проверьте Root Directory `/frontend` |
| Нужно начать с чистыми данными | В сервисе Postgres: Data → удалить все таблицы, затем Redeploy backend — он создаст их заново |

## Переход на настоящую почту

Mailpit подходит только для теста: письма видны в его ящике, а не у пользователей.
Чтобы коды приходили людям на их email, замените в backend переменные почты на SMTP настоящего сервиса,
например Brevo (300 писем в день бесплатно) или Gmail с паролем приложения:

```
SPRING_MAIL_HOST=smtp-relay.brevo.com
SPRING_MAIL_PORT=587
SPRING_MAIL_USERNAME=<логин SMTP>
SPRING_MAIL_PASSWORD=<SMTP-ключ>
MAIL_SMTP_AUTH=true
MAIL_SMTP_STARTTLS=true
MAIL_FROM=<адрес отправителя>
```

Код проекта менять не нужно.
