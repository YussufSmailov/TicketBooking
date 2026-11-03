INSERT INTO users (full_name, email, phone) VALUES
('Amina Seitkali', 'amina@gmail.com', '+77011234567'),
('Arman Zhaksylyk', 'arman@mail.kz', '+77029876543'),
('Dana Nurlanovna', 'dana@gmail.com', '+77075554433');

INSERT INTO movies (title, duration_min, age_rating) VALUES
('The Hunger Games: Sunrise on the Reaping', 140, '16+'),
('Avengers: Doomsday', 150, '12+'),
('Dune: Part Three', 160, '16+');

INSERT INTO sessions (movie_id, session_date, session_time, hall_number) VALUES
(1, '2026-11-21', '18:30', 1),
(1, '2026-11-22', '21:00', 2),
(2, '2026-12-18', '19:00', 1),
(3, '2026-12-19', '20:00', 3);

INSERT INTO seats (session_id, seat_number, price, status)
SELECT s.session_id,
       r.seat_row || n.seat_no,
       CASE WHEN r.seat_row IN ('D', 'E') THEN 3500 ELSE 2500 END,
       'Available'
FROM sessions s
CROSS JOIN (VALUES ('A'), ('B'), ('C'), ('D'), ('E')) AS r (seat_row)
CROSS JOIN generate_series(1, 8) AS n (seat_no)
ORDER BY s.session_id, r.seat_row, n.seat_no;

INSERT INTO concerts (artist, title, venue, city, concert_date, concert_time) VALUES
('Dimash Qudaibergen', 'Stranger World Tour', 'Almaty Arena', 'Almaty', '2026-12-05', '19:00'),
('Ninety One', 'Q-pop Night', 'Дворец Республики', 'Almaty', '2026-11-28', '20:00'),
('Irina Kairatovna', 'Live in Astana', 'Barys Arena', 'Astana', '2026-12-12', '19:30');

INSERT INTO concert_seats (concert_id, zone, seat_number, price, status)
SELECT c.concert_id, z.zone, z.prefix || '-' || n.seat_no, z.price, 'Available'
FROM concerts c
CROSS JOIN (VALUES ('VIP', 'V', 40000), ('Fan zone', 'F', 20000), ('Tribune', 'T', 12000)) AS z (zone, prefix, price)
CROSS JOIN generate_series(1, 8) AS n (seat_no)
ORDER BY c.concert_id, z.price DESC, n.seat_no;

INSERT INTO teams (name, city, stadium) VALUES
('Kairat', 'Almaty', 'Central Stadium Almaty'),
('Astana', 'Astana', 'Astana Arena'),
('Tobol', 'Kostanay', 'Central Stadium Kostanay'),
('Ordabasy', 'Shymkent', 'Kazhymukan Munaitpasov Stadium'),
('Aktobe', 'Aktobe', 'Central Stadium Aktobe'),
('Kaisar', 'Kyzylorda', 'Gany Muratbayev Stadium'),
('Yelimai', 'Semey', 'Spartak Stadium'),
('Kyzylzhar', 'Petropavl', 'Karasai Stadium');

INSERT INTO matches (home_team_id, away_team_id, tournament, round_name, match_date, match_time) VALUES
(1, 2, 'Kazakhstan Premier League', 'Round 26', '2026-11-08', '17:00'),
(4, 3, 'Kazakhstan Premier League', 'Round 26', '2026-11-08', '15:00'),
(5, 6, 'Kazakhstan Premier League', 'Round 26', '2026-11-09', '16:00'),
(7, 8, 'Kazakhstan Premier League', 'Round 26', '2026-11-09', '14:00');

INSERT INTO match_seats (match_id, sector, seat_number, price, status)
SELECT m.match_id, s.sector, s.prefix || '-' || n.seat_no, s.price, 'Available'
FROM matches m
CROSS JOIN (VALUES ('VIP', 'V', 6000), ('Central', 'C', 3000), ('Fan sector', 'F', 1500)) AS s (sector, prefix, price)
CROSS JOIN generate_series(1, 8) AS n (seat_no)
ORDER BY m.match_id, s.price DESC, n.seat_no;

INSERT INTO bookings (user_id, seat_id, booking_date, status) VALUES
(1, 3, '2026-11-01 12:00', 'Confirmed');

UPDATE seats SET status = 'Booked' WHERE seat_id = 3;

INSERT INTO payments (booking_id, amount, payment_date, status) VALUES
(1, 2500, '2026-11-01 12:01', 'Paid');

INSERT INTO concert_bookings (user_id, seat_id, booking_date, status) VALUES
(2, 1, '2026-11-02 10:00', 'Confirmed');

UPDATE concert_seats SET status = 'Booked' WHERE seat_id = 1;

INSERT INTO match_bookings (user_id, seat_id, booking_date, status) VALUES
(3, 9, '2026-11-02 18:00', 'Confirmed');

UPDATE match_seats SET status = 'Booked' WHERE seat_id = 9;
