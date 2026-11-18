INSERT INTO users (full_name, email, phone) VALUES
('Amina Seitkali', 'amina@gmail.com', '+77011234567'),
('Arman Zhaksylyk', 'arman@mail.kz', '+77029876543'),
('Dana Nurlanovna', 'dana@gmail.com', '+77075554433');

INSERT INTO movies (title, duration_min, age_rating) VALUES
('Голодные игры: Рассвет жатвы', 158, '16+'),
('Мстители: Доктор Дум', 165, '12+'),
('Дюна: Часть третья', 150, '12+'),
('Одиссея', 173, '16+'),
('Человек-паук: Новый день', 145, '12+'),
('История игрушек 5', 102, '6+'),
('Супергёрл', 108, '12+'),
('Супер Марио: Галактическое кино', 98, '6+'),
('Проект «Конец света»', 156, '12+'),
('Майкл', 127, '16+'),
('Мандалорец и Грогу', 132, '12+'),
('Дьявол носит Prada 2', 119, '12+'),
('Миньоны и монстры', 90, '0+'),
('Моана', 115, '6+'),
('Уличный боец', 121, '16+');

INSERT INTO movie_details (movie_id, description, poster_url, genre, release_date, is_premiere) VALUES
(1, 'Приквел «Голодных игр» по роману Сьюзен Коллинз. 50-е Голодные игры и вторая Квартальная бойня: шестнадцатилетний Хеймитч Эбернети из Дистрикта 12 попадает на арену, где трибутов вдвое больше обычного. Режиссёр — Фрэнсис Лоуренс, в ролях Джозеф Зада, Джесси Племонс, Ральф Файнс и Эль Фаннинг.', '/images/movies/hunger-games-sunrise.jpg', 'Фантастика', DATE '2026-11-20', TRUE),
(2, 'Мстители, ваканданцы, Фантастическая четвёрка, Новые Мстители и Люди Икс собираются из разных вселенных, чтобы остановить Доктора Дума на фоне распада мультивселенной. Режиссёры — Энтони и Джо Руссо, Дума играет Роберт Дауни-младший.', '/images/movies/avengers-doomsday.jpg', 'Фантастика', DATE '2026-12-18', TRUE),
(3, 'Финал трилогии Дени Вильнёва по роману Фрэнка Герберта «Мессия Дюны». Пол Атрейдес стал императором Известной Вселенной и теперь расплачивается за своё восхождение: заговоры, мятеж и видения о войне, которую он не в силах остановить. В главной роли — Тимоти Шаламе.', '/images/movies/dune-part-three.jpg', 'Фантастика', DATE '2026-12-18', TRUE),
(4, 'Кристофер Нолан экранизирует поэму Гомера. После Троянской войны царь Итаки Одиссей (Мэтт Деймон) пробивается домой сквозь бури и встречи с мифическими существами, чтобы вернуться к жене Пенелопе (Энн Хэтэуэй). Также в ролях Том Холланд, Роберт Паттинсон, Зендея и Шарлиз Терон.', '/images/movies/the-odyssey.jpg', 'Приключения', DATE '2026-07-17', TRUE),
(5, 'Питер Паркер остался один: никто не помнит, кто он такой, и всё своё время он отдаёт защите Нью-Йорка. Режиссёр — Дестин Дэниел Креттон, в ролях Том Холланд, Зендея, Сэди Синк, Джон Бернтал и Марк Руффало.', '/images/movies/spider-man-brand-new-day.jpg', 'Фантастика', DATE '2026-07-31', TRUE),
(6, 'Через два года после четвёртой части у Бонни появляется новая любимица — планшет по имени Лилипад. Джесси, Вуди, Базз Лайтер и остальные игрушки пытаются доказать, что им всё ещё есть место рядом с гаджетом. Режиссёр — Эндрю Стэнтон, студия Pixar.', '/images/movies/toy-story-5.jpg', 'Мультфильм', DATE '2026-06-19', FALSE),
(7, 'Кара Зор-Эл отправляется через всю галактику, чтобы найти врага, ранившего её пса Крипто. Второй фильм новой киновселенной DC: режиссёр Крейг Гиллеспи, в главной роли Милли Олкок, также в ролях Маттиас Шонартс и Джейсон Момоа.', '/images/movies/supergirl.jpg', 'Фантастика', DATE '2026-06-26', TRUE),
(8, 'Продолжение «Братьев Супер Марио в кино» от Illumination и Nintendo. Марио, Луиджи и их друзья отправляются в космос, где им предстоит сразиться с Боузером и его сыном Боузером-младшим.', '/images/movies/super-mario-galaxy.jpg', 'Мультфильм', DATE '2026-04-01', FALSE),
(9, 'Экранизация романа Энди Вейра. Школьный учитель Райланд Грейс (Райан Гослинг) просыпается на борту межзвёздного корабля, не помня, как туда попал, и постепенно понимает, что от его миссии зависит судьба Земли. Режиссёры — Фил Лорд и Кристофер Миллер.', '/images/movies/project-hail-mary.jpg', 'Фантастика', DATE '2026-03-20', FALSE),
(10, 'Биографический фильм Антуана Фукуа о Майкле Джексоне: от The Jackson 5 в 1960-х до мирового турне Bad в конце 1980-х. Джексона играет его племянник Джафар Джексон, также в ролях Колман Доминго, Майлз Теллер и Майк Майерс.', '/images/movies/michael.png', 'Биография', DATE '2026-04-24', FALSE),
(11, 'Полнометражное продолжение сериала «Мандалорец» из вселенной «Звёздных войн». Дин Джарин (Педро Паскаль) и его юный подопечный Грогу отправляются в новое приключение. Режиссёр — Джон Фавро, также в ролях Сигурни Уивер и Джереми Аллен Уайт.', '/images/movies/mandalorian-and-grogu.jpg', 'Фантастика', DATE '2026-05-22', FALSE),
(12, 'Спустя двадцать лет Энди Сакс (Энн Хэтэуэй) помогает Миранде Пристли (Мерил Стрип) удержать журнал Runway на плаву в новой медиареальности и под давлением корпораций. Вернулись и Эмили Блант со Стэнли Туччи.', '/images/movies/devil-wears-prada-2.png', 'Комедия', DATE '2026-05-01', FALSE),
(13, '1927 год, Голливуд переходит от немого кино к звуковому. Миньоны мечтают о большом успехе в кино и в погоне за ним выпускают на волю настоящих монстров. Третий фильм о миньонах и седьмой во франшизе «Гадкий я», режиссёр Пьер Коффен.', '/images/movies/minions-and-monsters.jpg', 'Мультфильм', DATE '2026-07-01', FALSE),
(14, 'Игровая версия мультфильма Disney 2016 года. Дочь вождя Моана (Кэтрин Лагаайя) выходит в открытый океан, чтобы спасти свой народ, и встречает полубога Мауи, которого снова играет Дуэйн Джонсон.', '/images/movies/moana.jpg', 'Приключения', DATE '2026-07-10', FALSE),
(15, 'Экранизация культовой серии файтингов Capcom. Чунь Ли (Каллина Лян) собирает бойцов Рю и Кена Мастерса на турнир, чтобы бросить вызов диктатору М. Байсону. Также в ролях Джейсон Момоа, Роман Рейнс и 50 Cent.', '/images/movies/street-fighter.jpg', 'Боевик', DATE '2026-10-16', TRUE);

INSERT INTO sessions (movie_id, session_date, session_time, hall_number)
SELECT m.movie_id,
       CURRENT_DATE + d.day_no,
       t.show_time,
       1 + ((m.movie_id + t.slot_no) % 5)
FROM movies m
JOIN movie_details md ON md.movie_id = m.movie_id
CROSS JOIN generate_series(0, 40) AS d (day_no)
CROSS JOIN (VALUES (1, TIME '11:30'), (2, TIME '14:10'), (3, TIME '16:50'), (4, TIME '19:20'), (5, TIME '21:55')) AS t (slot_no, show_time)
WHERE (d.day_no + m.movie_id) % 3 = 0
  AND (t.slot_no + m.movie_id) % 5 IN (0, 2, 4)
  AND CURRENT_DATE + d.day_no >= md.release_date
  AND (d.day_no <= 13 OR CURRENT_DATE + d.day_no < md.release_date + 3)
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

INSERT INTO concerts (artist, title, venue, city, concert_date, concert_time, duration_min, genre, description, poster_url) VALUES
('Dimash Qudaibergen', 'Большой сольный концерт', 'Astana Arena', 'Astana', CURRENT_DATE + 24, '19:00', 180, 'Поп', 'Димаш Кудайберген — казахстанский певец с диапазоном больше шести октав, известный по песням «SOS d''un terrien en détresse», «Love of Tired Swans» и «Знай». Хиты на казахском, русском, английском и других языках с оркестром и шоу-балетом.', '/images/concerts/dimash.jpg'),
('Kairat Nurtas', 'Сольный концерт', 'Almaty Arena', 'Almaty', CURRENT_DATE + 17, '20:00', 150, 'Поп', 'Кайрат Нуртас — один из самых популярных исполнителей казахской эстрады, который собирает полные арены по всей стране. Любимые хиты и новые песни.', '/images/concerts/kairat-nurtas.jpg'),
('Roza Rymbayeva', 'Золотые хиты', 'Дворец Республики', 'Almaty', CURRENT_DATE + 9, '19:00', 150, 'Эстрада', 'Народная артистка Казахстана Роза Рымбаева — легенда казахстанской эстрады, исполнительница песен «Любовь настала» и «Алия». Вечер золотых хитов с живым оркестром.', '/images/concerts/roza-rymbayeva.jpg'),
('A''Studio', 'Сольный концерт', 'Halyk Arena', 'Almaty', CURRENT_DATE + 12, '20:00', 120, 'Поп', 'А''Студио — группа, начинавшая в Алма-Ате в конце 1980-х. Вживую звучат «Улетаю», «S.O.S.», «Fashion Girl» и другие хиты разных лет.', '/images/concerts/a-studio.jpg'),
('Скриптонит', 'Сольный концерт', 'Barys Arena', 'Astana', CURRENT_DATE + 20, '20:00', 120, 'Хип-хоп', 'Скриптонит (Адиль Жалелов) — рэпер из Павлодара, один из самых влиятельных артистов русскоязычного хип-хопа. Треки с альбомов «Дом с нормальными явлениями» и «Уроборос» с живым бэндом.', '/images/concerts/scriptonite.jpg'),
('Imanbek', 'Night Show', 'Barys Arena', 'Astana', CURRENT_DATE + 45, '22:00', 150, 'Электроника', 'Иманбек Зейкенов — диджей и продюсер из Аксу, обладатель премии «Грэмми» за ремикс на «Roses» SAINt JHN. Ночное шоу с электронной музыкой и световыми эффектами.', '/images/concerts/imanbek.png'),
('Jah Khalib', 'Сольный концерт', 'Halyk Arena', 'Almaty', CURRENT_DATE + 31, '20:00', 120, 'Хип-хоп', 'Jah Khalib (Бахтияр Мамедов) — алматинский исполнитель и продюсер, автор хитов «Леля», «Медина» и «Если чё, я Баха».', '/images/concerts/jah-khalib.png'),
('MiyaGi & Andy Panda', 'Сольный концерт', 'Almaty Arena', 'Almaty', CURRENT_DATE + 38, '20:00', 120, 'Хип-хоп', 'Дуэт из Владикавказа, автор хитов «I Got Love», «Minimal», «Kosandra» и «Тамада».', '/images/concerts/miyagi-andy-panda.png'),
('Макс Корж', 'Стадионный концерт', 'Центральный стадион', 'Almaty', CURRENT_DATE + 27, '19:00', 150, 'Поп-рок', 'Белорусский исполнитель Макс Корж, автор хитов «Малый повзрослел», «Мотылёк» и «Жить в кайф». Его концерты — одни из самых массовых на постсоветском пространстве.', '/images/concerts/max-korzh.jpg'),
('Jony', 'Сольный концерт', 'Дворец Республики', 'Almaty', CURRENT_DATE + 14, '19:30', 110, 'Поп', 'Jony (Джахид Гусейнли) — исполнитель хитов «Комета», «Love Your Voice» и «Аллея».', '/images/concerts/jony.jpg');

INSERT INTO concert_seats (concert_id, zone, seat_number, price, status)
SELECT c.concert_id, z.zone, r.row_no || '-' || n.seat_no, z.price, 'Available'
FROM concerts c
CROSS JOIN (VALUES ('VIP', 3, 12, 40000), ('Fan zone', 4, 16, 20000), ('Tribune', 6, 20, 12000)) AS z (zone, row_count, row_length, price)
CROSS JOIN LATERAL generate_series(1, z.row_count) AS r (row_no)
CROSS JOIN LATERAL generate_series(1, z.row_length) AS n (seat_no)
ORDER BY c.concert_id, z.price DESC, r.row_no, n.seat_no;

INSERT INTO teams (name, city, stadium, logo_url) VALUES
('Kairat', 'Almaty', 'Central Stadium Almaty', '/images/teams/kairat.png'),
('Astana', 'Astana', 'Astana Arena', '/images/teams/astana.png'),
('Tobol', 'Kostanay', 'Central Stadium Kostanay', '/images/teams/tobol.png'),
('Ordabasy', 'Shymkent', 'Kazhymukan Munaitpasov Stadium', '/images/teams/ordabasy.png'),
('Aktobe', 'Aktobe', 'Central Stadium Aktobe', '/images/teams/aktobe.png'),
('Kaisar', 'Kyzylorda', 'Gany Muratbayev Stadium', '/images/teams/kaisar.png'),
('Yelimay', 'Semey', 'Spartak Stadium', '/images/teams/yelimay.png'),
('Kyzylzhar', 'Petropavl', 'Karasai Stadium', '/images/teams/kyzylzhar.png'),
('Zhenis', 'Astana', 'Astana Arena', '/images/teams/zhenis.png'),
('Atyrau', 'Atyrau', 'Munaishy Stadium', '/images/teams/atyrau.png'),
('Okzhetpes', 'Kokshetau', 'Okzhetpes Stadium', '/images/teams/okzhetpes.png'),
('Zhetysu', 'Taldykorgan', 'Zhetysu Stadium', '/images/teams/zhetysu.png');

INSERT INTO matches (home_team_id, away_team_id, tournament, round_name, match_date, match_time, description, poster_url) VALUES
(1, 2, 'Kazakhstan Premier League', 'Тур 1', CURRENT_DATE + 4, '19:00', 'Кайрат — Астана. Главное дерби страны на Центральном стадионе Алматы.', '/images/stadiums/almaty-central.jpg'),
(4, 3, 'Kazakhstan Premier League', 'Тур 1', CURRENT_DATE + 4, '16:00', 'Ордабасы — Тобол. Матч в Шымкенте на стадионе имени Кажымукана Мунайтпасова.', NULL),
(5, 6, 'Kazakhstan Premier League', 'Тур 1', CURRENT_DATE + 5, '17:00', 'Актобе — Кайсар. Борьба за верхнюю часть таблицы на Центральном стадионе Актобе.', '/images/stadiums/aktobe-central.jpg'),
(7, 8, 'Kazakhstan Premier League', 'Тур 1', CURRENT_DATE + 5, '15:00', 'Елимай — Кызылжар. Матч на стадионе «Спартак» в Семее.', '/images/stadiums/semey-spartak.jpg'),
(1, 9, 'Kazakhstan Premier League', 'Тур 2', CURRENT_DATE + 11, '19:00', 'Кайрат — Женис. Вечерний матч в Алматы.', '/images/stadiums/almaty-central.jpg'),
(2, 4, 'Kazakhstan Premier League', 'Тур 2', CURRENT_DATE + 11, '18:00', 'Астана — Ордабасы на «Астана Арене».', '/images/stadiums/astana-arena.jpg'),
(10, 11, 'Kazakhstan Premier League', 'Тур 2', CURRENT_DATE + 12, '16:00', 'Атырау — Окжетпес. Матч на стадионе «Мунайшы».', NULL),
(12, 5, 'Kazakhstan Premier League', 'Тур 2', CURRENT_DATE + 12, '17:30', 'Жетысу — Актобе. Матч в Талдыкоргане.', '/images/stadiums/taldykorgan-zhetysu.png'),
(3, 1, 'Kazakhstan Premier League', 'Тур 3', CURRENT_DATE + 18, '17:00', 'Тобол — Кайрат на Центральном стадионе Костаная.', '/images/stadiums/kostanay-central.jpg'),
(1, 7, 'Kazakhstan Premier League', 'Тур 4', CURRENT_DATE + 25, '19:00', 'Кайрат — Елимай на Центральном стадионе Алматы.', '/images/stadiums/almaty-central.jpg'),
(9, 12, 'Kazakhstan Premier League', 'Тур 3', CURRENT_DATE + 19, '15:00', 'Женис — Жетысу на «Астана Арене».', '/images/stadiums/astana-arena.jpg'),
(8, 10, 'Kazakhstan Premier League', 'Тур 3', CURRENT_DATE + 19, '16:00', 'Кызылжар — Атырау. Матч на стадионе «Карасай» в Петропавловске.', NULL);

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

INSERT INTO reviews (user_id, event_type, event_id, rating, comment, created_at, updated_at) VALUES
(1, 'movie', 4, 5, 'Нолан снова удивил: масштаб, звук и Мэтт Деймон в роли Одиссея. Смотреть только в большом зале.', NOW() - INTERVAL '20 days', NOW() - INTERVAL '20 days'),
(2, 'movie', 4, 4, 'Очень красиво, но почти три часа — к концу устаёшь.', NOW() - INTERVAL '18 days', NOW() - INTERVAL '18 days'),
(3, 'movie', 4, 5, NULL, NOW() - INTERVAL '15 days', NOW() - INTERVAL '15 days'),
(1, 'movie', 5, 4, 'Хороший перезапуск истории Питера, Сэди Синк отлично вписалась.', NOW() - INTERVAL '30 days', NOW() - INTERVAL '30 days'),
(2, 'movie', 5, 5, 'Лучший Человек-паук со времён «Нет пути домой».', NOW() - INTERVAL '25 days', NOW() - INTERVAL '25 days'),
(3, 'movie', 6, 5, 'Ходили всей семьёй, дети в восторге.', NOW() - INTERVAL '40 days', NOW() - INTERVAL '40 days'),
(1, 'movie', 6, 4, NULL, NOW() - INTERVAL '38 days', NOW() - INTERVAL '38 days'),
(2, 'movie', 7, 3, 'Неплохо, но сюжет слишком простой.', NOW() - INTERVAL '35 days', NOW() - INTERVAL '35 days'),
(3, 'movie', 8, 4, 'Ярко и смешно, особенно Боузер-младший.', NOW() - INTERVAL '60 days', NOW() - INTERVAL '60 days'),
(1, 'movie', 9, 5, 'Лучшая фантастика года. Рокки — любимый персонаж.', NOW() - INTERVAL '70 days', NOW() - INTERVAL '70 days'),
(2, 'movie', 9, 5, NULL, NOW() - INTERVAL '65 days', NOW() - INTERVAL '65 days'),
(3, 'movie', 10, 4, 'Музыка и танцы — мурашки. Сюжет местами поверхностный.', NOW() - INTERVAL '55 days', NOW() - INTERVAL '55 days'),
(1, 'movie', 11, 3, 'Грогу милый, но фильм похож на длинную серию сериала.', NOW() - INTERVAL '50 days', NOW() - INTERVAL '50 days'),
(2, 'movie', 12, 4, 'Мерил Стрип как всегда великолепна.', NOW() - INTERVAL '45 days', NOW() - INTERVAL '45 days'),
(3, 'movie', 13, 3, 'Для детей — да, для взрослых — на один раз.', NOW() - INTERVAL '33 days', NOW() - INTERVAL '33 days'),
(1, 'movie', 14, 4, NULL, NOW() - INTERVAL '28 days', NOW() - INTERVAL '28 days'),
(2, 'movie', 15, 4, 'Драки поставлены круто, фанатам игры понравится.', NOW() - INTERVAL '10 days', NOW() - INTERVAL '10 days'),
(3, 'movie', 15, 3, 'Весело, но сюжета почти нет.', NOW() - INTERVAL '8 days', NOW() - INTERVAL '8 days'),
(1, 'concert', 1, 5, 'Была на прошлом концерте Димаша — это невероятно. Беру билеты снова!', NOW() - INTERVAL '5 days', NOW() - INTERVAL '5 days'),
(2, 'concert', 5, 4, 'Скриптонит вживую с бэндом звучит лучше, чем на записи.', NOW() - INTERVAL '4 days', NOW() - INTERVAL '4 days'),
(3, 'concert', 3, 5, NULL, NOW() - INTERVAL '3 days', NOW() - INTERVAL '3 days'),
(2, 'match', 1, 5, 'Дерби всегда огонь, атмосфера на Центральном — лучшая в лиге.', NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days');
