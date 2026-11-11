INSERT INTO users (full_name, email, phone) VALUES
('Amina Seitkali', 'amina@gmail.com', '+77011234567'),
('Arman Zhaksylyk', 'arman@mail.kz', '+77029876543'),
('Dana Nurlanovna', 'dana@gmail.com', '+77075554433');

INSERT INTO movies (title, duration_min, age_rating) VALUES
('The Hunger Games: Sunrise on the Reaping', 140, '16+'),
('Avengers: Doomsday', 150, '12+'),
('Dune: Part Three', 160, '16+'),
('Степной ветер', 118, '12+'),
('Последний рейс на Байконур', 132, '16+'),
('Алматы. Ночь', 104, '18+'),
('Код Тенгри', 127, '12+'),
('Снежный барс', 96, '6+'),
('Шёлковый путь: Наследие', 145, '12+'),
('Тихий двор', 101, '16+'),
('Космос в кармане', 88, '0+'),
('Дорога домой', 112, '12+'),
('Нулевой меридиан', 138, '16+'),
('Хан Тенгри: Восхождение', 124, '12+'),
('Музыка ветра', 99, '6+');

INSERT INTO movie_details (movie_id, description, poster_url, genre, release_date, is_premiere) VALUES
(1, 'Панем, 50-е Голодные игры. Молодой Хеймитч Эбернети выходит на арену, где правила меняются быстрее, чем он успевает их понять.', 'https://picsum.photos/seed/hunger-games-reaping/400/600', 'Фантастика', CURRENT_DATE - 3, TRUE),
(2, 'Команда героев собирается снова, чтобы встретить угрозу, способную стереть целые миры. Большое приключение для всей семьи.', 'https://picsum.photos/seed/avengers-doomsday/400/600', 'Фантастика', CURRENT_DATE - 1, TRUE),
(3, 'Продолжение пустынной саги: Пол Атрейдес и цена власти над Вселенной.', 'https://picsum.photos/seed/dune-part-three/400/600', 'Фантастика', CURRENT_DATE + 6, TRUE),
(4, 'Пастух из Жетысу находит в горах раненого коня и отправляется в долгий путь через всю степь, чтобы вернуть его хозяину.', 'https://picsum.photos/seed/stepnoy-veter/400/600', 'Драма', CURRENT_DATE - 10, FALSE),
(5, 'Бывший инженер космодрома получает последнее задание: успеть доставить груз до старта ракеты, пока за ним идёт погоня.', 'https://picsum.photos/seed/baikonur-reis/400/600', 'Боевик', CURRENT_DATE - 6, TRUE),
(6, 'Одна ночь в большом городе: таксист, музыкант и полицейский случайно оказываются втянуты в одну историю.', 'https://picsum.photos/seed/almaty-night/400/600', 'Триллер', CURRENT_DATE - 14, FALSE),
(7, 'Группа студентов-программистов находит в древней рукописи шифр, который открывает доступ к забытой технологии.', 'https://picsum.photos/seed/kod-tengri/400/600', 'Приключения', CURRENT_DATE - 4, FALSE),
(8, 'Анимационная история о детёныше снежного барса, который ищет свою стаю в горах Заилийского Алатау.', 'https://picsum.photos/seed/snow-leopard/400/600', 'Мультфильм', CURRENT_DATE - 8, FALSE),
(9, 'Эпическая сага о караване, который в XIV веке связал города Великого шёлкового пути.', 'https://picsum.photos/seed/silk-road-legacy/400/600', 'Исторический', CURRENT_DATE - 2, TRUE),
(10, 'Камерная драма о соседях одного алматинского двора и о том, как один летний вечер изменил их жизнь.', 'https://picsum.photos/seed/tihiy-dvor/400/600', 'Драма', CURRENT_DATE - 20, FALSE),
(11, 'Весёлый мультфильм о мальчике, который собрал ракету из старого пылесоса и отправился на Луну.', 'https://picsum.photos/seed/kosmos-v-karmane/400/600', 'Мультфильм', CURRENT_DATE + 3, FALSE),
(12, 'Семейная история о возвращении в родной аул спустя двадцать лет после отъезда в большой город.', 'https://picsum.photos/seed/doroga-domoy/400/600', 'Драма', CURRENT_DATE - 12, FALSE),
(13, 'Научный триллер: на арктической станции пропадает связь, а у экспедиции остаются сутки до шторма.', 'https://picsum.photos/seed/zero-meridian/400/600', 'Триллер', CURRENT_DATE + 8, FALSE),
(14, 'Альпинисты из Алматы решаются на зимнее восхождение на Хан Тенгри. Фильм основан на вымышленных событиях.', 'https://picsum.photos/seed/khan-tengri/400/600', 'Приключения', CURRENT_DATE - 5, FALSE),
(15, 'Музыкальная комедия о сельском ансамбле, который неожиданно попадает на большой фестиваль.', 'https://picsum.photos/seed/muzyka-vetra/400/600', 'Комедия', CURRENT_DATE - 9, FALSE);

INSERT INTO sessions (movie_id, session_date, session_time, hall_number)
SELECT m.movie_id,
       CURRENT_DATE + d.day_no,
       t.show_time,
       1 + ((m.movie_id + t.slot_no) % 5)
FROM movies m
JOIN movie_details md ON md.movie_id = m.movie_id
CROSS JOIN generate_series(0, 13) AS d (day_no)
CROSS JOIN (VALUES (1, TIME '11:30'), (2, TIME '14:10'), (3, TIME '16:50'), (4, TIME '19:20'), (5, TIME '21:55')) AS t (slot_no, show_time)
WHERE (d.day_no + m.movie_id) % 3 = 0
  AND (t.slot_no + m.movie_id) % 5 IN (0, 2, 4)
  AND CURRENT_DATE + d.day_no >= md.release_date
  AND (CURRENT_DATE + d.day_no) + t.show_time > LOCALTIMESTAMP + INTERVAL '30 minutes'
ORDER BY CURRENT_DATE + d.day_no, t.show_time, m.movie_id;

INSERT INTO seats (session_id, seat_number, price, status)
SELECT s.session_id,
       r.row_no || '-' || n.seat_no,
       CASE WHEN r.row_no >= 9 THEN 3500 WHEN r.row_no >= 4 THEN 2500 ELSE 2000 END,
       'Available'
FROM sessions s
CROSS JOIN (VALUES (1, 10), (2, 12), (3, 12), (4, 14), (5, 14), (6, 16), (7, 16), (8, 18), (9, 18), (10, 20)) AS r (row_no, row_length)
CROSS JOIN LATERAL generate_series(1, r.row_length) AS n (seat_no)
ORDER BY s.session_id, r.row_no, n.seat_no;

INSERT INTO concerts (artist, title, venue, city, concert_date, concert_time, description, poster_url) VALUES
('Dimash Qudaibergen', 'Stranger World Tour', 'Almaty Arena', 'Almaty', CURRENT_DATE + 24, '19:00', 'Большое шоу с оркестром и новыми песнями. Продолжительность около трёх часов с антрактом.', 'https://picsum.photos/seed/dimash-stranger/400/600'),
('Ninety One', 'Q-pop Night', 'Дворец Республики', 'Almaty', CURRENT_DATE + 17, '20:00', 'Самая известная Q-pop группа страны исполнит хиты и треки из нового альбома.', 'https://picsum.photos/seed/ninety-one-qpop/400/600'),
('Irina Kairatovna', 'Live in Astana', 'Barys Arena', 'Astana', CURRENT_DATE + 31, '19:30', 'Живой концерт с группой и гостями. Танцпол и сидячие места.', 'https://picsum.photos/seed/irina-astana/400/600'),
('Steppe Symphony Orchestra', 'Музыка Великой степи', 'Казахская национальная филармония', 'Almaty', CURRENT_DATE + 9, '19:00', 'Произведения Курмангазы и Таттимбета в симфонической обработке.', 'https://picsum.photos/seed/steppe-symphony/400/600'),
('Almaty Jazz Band', 'Jazz под звёздами', 'Halyk Arena', 'Almaty', CURRENT_DATE + 12, '20:30', 'Вечер джазовых стандартов и импровизаций с приглашёнными солистами.', 'https://picsum.photos/seed/almaty-jazz/400/600'),
('Kairat Nurtas', 'Жаңа ән', 'Конгресс-холл', 'Astana', CURRENT_DATE + 20, '19:00', 'Сольный концерт с новыми песнями и любимыми хитами.', 'https://picsum.photos/seed/kairat-nurtas/400/600'),
('Shattyq Band', 'Rock Steppe', 'Дворец Республики', 'Almaty', CURRENT_DATE + 38, '20:00', 'Рок-фестиваль одной группы: громко, честно и с кавер-версиями казахской эстрады.', 'https://picsum.photos/seed/rock-steppe/400/600'),
('Astana Strings', 'Классика при свечах', 'Astana Opera', 'Astana', CURRENT_DATE + 14, '19:00', 'Струнный квартет исполняет Вивальди, Баха и современные саундтреки.', 'https://picsum.photos/seed/astana-strings/400/600'),
('Qazaq Electronic', 'Night Rave', 'Barys Arena', 'Astana', CURRENT_DATE + 45, '22:00', 'Электронная музыка, световое шоу и диджеи со всей Центральной Азии.', 'https://picsum.photos/seed/qazaq-electronic/400/600'),
('Domra Folk', 'Домбыра фест', 'Almaty Arena', 'Almaty', CURRENT_DATE + 27, '18:00', 'Этно-концерт: домбыра, кобыз и горловое пение в современной аранжировке.', 'https://picsum.photos/seed/dombyra-fest/400/600');

INSERT INTO concert_seats (concert_id, zone, seat_number, price, status)
SELECT c.concert_id, z.zone, r.row_no || '-' || n.seat_no, z.price, 'Available'
FROM concerts c
CROSS JOIN (VALUES ('VIP', 3, 12, 40000), ('Fan zone', 4, 16, 20000), ('Tribune', 6, 20, 12000)) AS z (zone, row_count, row_length, price)
CROSS JOIN LATERAL generate_series(1, z.row_count) AS r (row_no)
CROSS JOIN LATERAL generate_series(1, z.row_length) AS n (seat_no)
ORDER BY c.concert_id, z.price DESC, r.row_no, n.seat_no;

INSERT INTO teams (name, city, stadium) VALUES
('Kairat', 'Almaty', 'Central Stadium Almaty'),
('Astana', 'Astana', 'Astana Arena'),
('Tobol', 'Kostanay', 'Central Stadium Kostanay'),
('Ordabasy', 'Shymkent', 'Kazhymukan Munaitpasov Stadium'),
('Aktobe', 'Aktobe', 'Central Stadium Aktobe'),
('Kaisar', 'Kyzylorda', 'Gany Muratbayev Stadium'),
('Yelimai', 'Semey', 'Spartak Stadium'),
('Kyzylzhar', 'Petropavl', 'Karasai Stadium'),
('Zhenis', 'Astana', 'Astana Arena'),
('Atyrau', 'Atyrau', 'Munaishy Stadium'),
('Okzhetpes', 'Kokshetau', 'Okzhetpes Stadium'),
('Zhetysu', 'Taldykorgan', 'Zhetysu Stadium');

INSERT INTO matches (home_team_id, away_team_id, tournament, round_name, match_date, match_time, description, poster_url) VALUES
(1, 2, 'Kazakhstan Premier League', 'Тур 1', CURRENT_DATE + 4, '19:00', 'Главное дерби страны: Кайрат принимает Астану на Центральном стадионе.', 'https://picsum.photos/seed/kairat-astana/400/600'),
(4, 3, 'Kazakhstan Premier League', 'Тур 1', CURRENT_DATE + 4, '16:00', 'Ордабасы и Тобол открывают сезон в Шымкенте.', 'https://picsum.photos/seed/ordabasy-tobol/400/600'),
(5, 6, 'Kazakhstan Premier League', 'Тур 1', CURRENT_DATE + 5, '17:00', 'Актобе против Кайсара: борьба за верхнюю часть таблицы.', 'https://picsum.photos/seed/aktobe-kaisar/400/600'),
(7, 8, 'Kazakhstan Premier League', 'Тур 1', CURRENT_DATE + 5, '15:00', 'Елимай принимает Кызылжар в Семее.', 'https://picsum.photos/seed/yelimai-kyzylzhar/400/600'),
(1, 9, 'Kazakhstan Premier League', 'Тур 2', CURRENT_DATE + 11, '19:00', 'Кайрат против Жениса. Вечерний матч в Алматы.', 'https://picsum.photos/seed/kairat-zhenis/400/600'),
(2, 4, 'Kazakhstan Premier League', 'Тур 2', CURRENT_DATE + 11, '18:00', 'Астана встречает Ордабасы на домашней арене.', 'https://picsum.photos/seed/astana-ordabasy/400/600'),
(10, 11, 'Kazakhstan Premier League', 'Тур 2', CURRENT_DATE + 12, '16:00', 'Атырау против Окжетпеса.', 'https://picsum.photos/seed/atyrau-okzhetpes/400/600'),
(12, 5, 'Kazakhstan Premier League', 'Тур 2', CURRENT_DATE + 12, '17:30', 'Жетысу принимает Актобе в Талдыкоргане.', 'https://picsum.photos/seed/zhetysu-aktobe/400/600'),
(3, 1, 'Kazakhstan Premier League', 'Тур 3', CURRENT_DATE + 18, '17:00', 'Тобол против Кайрата в Костанае.', 'https://picsum.photos/seed/tobol-kairat/400/600'),
(1, 7, 'Kazakhstan Premier League', 'Тур 4', CURRENT_DATE + 25, '19:00', 'Кайрат принимает Елимай на Центральном стадионе Алматы.', 'https://picsum.photos/seed/kairat-yelimai/400/600'),
(9, 12, 'Kazakhstan Premier League', 'Тур 3', CURRENT_DATE + 19, '15:00', 'Женис против Жетысу.', 'https://picsum.photos/seed/zhenis-zhetysu/400/600'),
(8, 10, 'Kazakhstan Premier League', 'Тур 3', CURRENT_DATE + 19, '16:00', 'Кызылжар принимает Атырау в Петропавловске.', 'https://picsum.photos/seed/kyzylzhar-atyrau/400/600');

INSERT INTO match_seats (match_id, sector, seat_number, price, status)
SELECT m.match_id, s.sector, r.row_no || '-' || n.seat_no, s.price, 'Available'
FROM matches m
CROSS JOIN (VALUES ('VIP', 3, 10, 8000),
                   ('Западная', 6, 20, 4000),
                   ('Восточная', 6, 20, 3500),
                   ('Северная', 5, 16, 2000),
                   ('Южная', 5, 16, 2000)) AS s (sector, row_count, row_length, price)
CROSS JOIN LATERAL generate_series(1, s.row_count) AS r (row_no)
CROSS JOIN LATERAL generate_series(1, s.row_length) AS n (seat_no)
ORDER BY m.match_id, s.price DESC, r.row_no, n.seat_no;

INSERT INTO bookings (user_id, seat_id, booking_date, status)
SELECT 1, seat_id, NOW() - INTERVAL '3 days', 'Confirmed'
FROM seats
WHERE seat_number = '5-7'
  AND session_id = (SELECT MIN(session_id) FROM sessions WHERE movie_id = 1);

INSERT INTO bookings (user_id, seat_id, booking_date, status)
SELECT 2 + (seat_id % 2), seat_id, NOW() - INTERVAL '2 days', 'Confirmed'
FROM seats
WHERE (seat_id * 37) % 100 < 18
  AND seat_id NOT IN (SELECT seat_id FROM bookings);

UPDATE seats SET status = 'Booked'
WHERE seat_id IN (SELECT seat_id FROM bookings WHERE status = 'Confirmed');

INSERT INTO payments (booking_id, amount, payment_date, status)
SELECT b.booking_id, s.price, b.booking_date + INTERVAL '1 minute', 'Paid'
FROM bookings b
JOIN seats s ON s.seat_id = b.seat_id;

INSERT INTO concert_bookings (user_id, seat_id, booking_date, status)
SELECT 2 + (seat_id % 2), seat_id, NOW() - INTERVAL '2 days', 'Confirmed'
FROM concert_seats
WHERE (seat_id * 41) % 100 < 22;

UPDATE concert_seats SET status = 'Booked'
WHERE seat_id IN (SELECT seat_id FROM concert_bookings WHERE status = 'Confirmed');

INSERT INTO concert_payments (booking_id, amount, payment_date, status)
SELECT b.booking_id, s.price, b.booking_date + INTERVAL '1 minute', 'Paid'
FROM concert_bookings b
JOIN concert_seats s ON s.seat_id = b.seat_id;

INSERT INTO match_bookings (user_id, seat_id, booking_date, status)
SELECT 2 + (seat_id % 2), seat_id, NOW() - INTERVAL '2 days', 'Confirmed'
FROM match_seats
WHERE (seat_id * 43) % 100 < 20
   OR (match_id = 1 AND sector = 'Северная');

UPDATE match_seats SET status = 'Booked'
WHERE seat_id IN (SELECT seat_id FROM match_bookings WHERE status = 'Confirmed');

INSERT INTO match_payments (booking_id, amount, payment_date, status)
SELECT b.booking_id, s.price, b.booking_date + INTERVAL '1 minute', 'Paid'
FROM match_bookings b
JOIN match_seats s ON s.seat_id = b.seat_id;
