# Ticket Booking System (MVP)

## База данных
1. Установите PostgreSQL и создайте базу: `CREATE DATABASE ticket_booking;`
2. Логин и пароль поменяйте в `backend/src/main/resources/application.properties`.
3. Таблицы и тестовые данные создаются автоматически при запуске backend из `schema.sql` и `data.sql`.
   При каждом перезапуске база пересоздаётся заново.

## Backend
```
cd backend
mvn spring-boot:run
```
API: http://localhost:8080/api

## Тесты
1. Создайте тестовую базу: `CREATE DATABASE ticket_booking_test;`
2. Пароль поменяйте в `backend/src/test/resources/application.properties`.
3. Запуск: `mvn test` или в IntelliJ правый клик по `src/test/java` → Run 'All Tests'.

| Уровень пирамиды | Класс | Что проверяет |
|---|---|---|
| Unit (Mockito) | CinemaServiceTest, UserServiceTest | Логику сервисов без базы |
| Repository (@DataJpaTest) | RepositoryTest | Запросы и ограничения PostgreSQL |
| Controller (@WebMvcTest) | CinemaControllerTest | HTTP-статусы и JSON |
| Integration (@SpringBootTest) | BookingFlowIntegrationTest | Полный путь запроса до базы |

## Frontend
```
cd frontend
npm install
npm run dev
```
Сайт: http://localhost:5173

## Основные эндпоинты
| Метод | URL | Что делает |
|---|---|---|
| GET/POST | /api/cinema/movies | список фильмов / добавить фильм |
| GET/PUT/DELETE | /api/cinema/movies/{id} | один фильм |
| GET | /api/cinema/sessions | все сеансы |
| GET | /api/cinema/sessions/{id}/seats | места на сеанс |
| GET/POST | /api/cinema/bookings | брони кино / создать бронь (с оплатой) |
| PUT | /api/cinema/bookings/{id}/cancel | отменить бронь |
| GET/POST | /api/concert/concerts | концерты |
| GET/PUT/DELETE | /api/concert/concerts/{id} | один концерт |
| GET | /api/concert/concerts/{id}/seats | места на концерт |
| GET/POST | /api/concert/bookings | брони концертов |
| PUT | /api/concert/bookings/{id}/cancel | отменить бронь |
| GET | /api/sport/teams | клубы КПЛ |
| GET/POST | /api/sport/matches | матчи |
| GET/PUT/DELETE | /api/sport/matches/{id} | один матч |
| GET | /api/sport/matches/{id}/seats | места на матч |
| GET/POST | /api/sport/bookings | брони матчей |
| PUT | /api/sport/bookings/{id}/cancel | отменить бронь |
| POST | /api/cinema/sessions | добавить сеанс |
| DELETE | /api/cinema/sessions/{id} | удалить сеанс и его места |
| POST | /api/cinema/sessions/{id}/seats | добавить ряд мест в сеанс |
| POST | /api/concert/concerts/{id}/seats | добавить зону мест на концерт |
| POST | /api/sport/matches/{id}/seats | добавить сектор мест на матч |
| GET/POST | /api/users | пользователи |

## Админ-панель
Откройте http://localhost:5173/admin. Там можно добавлять, изменять и удалять фильмы, сеансы, концерты и матчи, а также добавлять к ним места. Пока без логина: доступ по роли Admin появится вместе со Spring Security.

Тело запроса на добавление мест:
```json
{ "name": "F", "count": 8, "price": 3000 }
```

Тело запроса на бронь:
```json
{ "fullName": "Amina Seitkali", "email": "amina@gmail.com", "phone": "+77011234567", "seatId": 5 }
```
