DROP TABLE IF EXISTS payment_cards CASCADE;
DROP TABLE IF EXISTS concert_payments CASCADE;
DROP TABLE IF EXISTS match_payments CASCADE;
DROP TABLE IF EXISTS payments CASCADE;
DROP TABLE IF EXISTS bookings CASCADE;
DROP TABLE IF EXISTS seats CASCADE;
DROP TABLE IF EXISTS sessions CASCADE;
DROP TABLE IF EXISTS movie_details CASCADE;
DROP TABLE IF EXISTS movies CASCADE;
DROP TABLE IF EXISTS concert_bookings CASCADE;
DROP TABLE IF EXISTS concert_seats CASCADE;
DROP TABLE IF EXISTS concerts CASCADE;
DROP TABLE IF EXISTS match_bookings CASCADE;
DROP TABLE IF EXISTS match_seats CASCADE;
DROP TABLE IF EXISTS matches CASCADE;
DROP TABLE IF EXISTS teams CASCADE;
DROP TABLE IF EXISTS user_credentials CASCADE;
DROP TABLE IF EXISTS users CASCADE;

CREATE TABLE users (
    user_id SERIAL PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    phone VARCHAR(20)
);

CREATE TABLE user_credentials (
    user_id INT PRIMARY KEY REFERENCES users (user_id) ON DELETE CASCADE,
    password_hash VARCHAR(100) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'CUSTOMER' CHECK (role IN ('CUSTOMER', 'ADMIN'))
);

CREATE TABLE movies (
    movie_id SERIAL PRIMARY KEY,
    title VARCHAR(150) NOT NULL,
    duration_min INT NOT NULL CHECK (duration_min > 0),
    age_rating VARCHAR(10)
);

CREATE TABLE movie_details (
    movie_id INT PRIMARY KEY REFERENCES movies (movie_id) ON DELETE CASCADE,
    description TEXT,
    poster_url VARCHAR(300),
    genre VARCHAR(50),
    release_date DATE,
    is_premiere BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE sessions (
    session_id SERIAL PRIMARY KEY,
    movie_id INT NOT NULL REFERENCES movies (movie_id),
    session_date DATE NOT NULL,
    session_time TIME NOT NULL,
    hall_number INT NOT NULL
);

CREATE TABLE seats (
    seat_id SERIAL PRIMARY KEY,
    session_id INT NOT NULL REFERENCES sessions (session_id),
    seat_number VARCHAR(10) NOT NULL,
    price DECIMAL(10, 2) NOT NULL CHECK (price > 0),
    status VARCHAR(20) NOT NULL DEFAULT 'Available' CHECK (status IN ('Available', 'Reserved', 'Booked')),
    UNIQUE (session_id, seat_number)
);

CREATE TABLE bookings (
    booking_id SERIAL PRIMARY KEY,
    user_id INT NOT NULL REFERENCES users (user_id),
    seat_id INT NOT NULL REFERENCES seats (seat_id),
    booking_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(20) NOT NULL CHECK (status IN ('Pending', 'Confirmed', 'Cancelled'))
);

CREATE UNIQUE INDEX one_active_booking_per_seat ON bookings (seat_id) WHERE status <> 'Cancelled';

CREATE TABLE payments (
    payment_id SERIAL PRIMARY KEY,
    booking_id INT NOT NULL UNIQUE REFERENCES bookings (booking_id),
    amount DECIMAL(10, 2) NOT NULL CHECK (amount > 0),
    payment_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(20) NOT NULL CHECK (status IN ('Pending', 'Paid', 'Failed'))
);

CREATE TABLE concerts (
    concert_id SERIAL PRIMARY KEY,
    artist VARCHAR(100) NOT NULL,
    title VARCHAR(150) NOT NULL,
    venue VARCHAR(100) NOT NULL,
    city VARCHAR(50) NOT NULL,
    concert_date DATE NOT NULL,
    concert_time TIME NOT NULL,
    description TEXT,
    poster_url VARCHAR(300)
);

CREATE TABLE concert_seats (
    seat_id SERIAL PRIMARY KEY,
    concert_id INT NOT NULL REFERENCES concerts (concert_id),
    zone VARCHAR(30) NOT NULL,
    seat_number VARCHAR(10) NOT NULL,
    price DECIMAL(10, 2) NOT NULL CHECK (price > 0),
    status VARCHAR(20) NOT NULL DEFAULT 'Available' CHECK (status IN ('Available', 'Reserved', 'Booked')),
    UNIQUE (concert_id, zone, seat_number)
);

CREATE TABLE concert_bookings (
    booking_id SERIAL PRIMARY KEY,
    user_id INT NOT NULL REFERENCES users (user_id),
    seat_id INT NOT NULL REFERENCES concert_seats (seat_id),
    booking_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(20) NOT NULL CHECK (status IN ('Pending', 'Confirmed', 'Cancelled'))
);

CREATE UNIQUE INDEX one_active_concert_booking_per_seat ON concert_bookings (seat_id) WHERE status <> 'Cancelled';

CREATE TABLE concert_payments (
    payment_id SERIAL PRIMARY KEY,
    booking_id INT NOT NULL UNIQUE REFERENCES concert_bookings (booking_id),
    amount DECIMAL(10, 2) NOT NULL CHECK (amount > 0),
    payment_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(20) NOT NULL CHECK (status IN ('Pending', 'Paid', 'Failed'))
);

CREATE TABLE teams (
    team_id SERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    city VARCHAR(50) NOT NULL,
    stadium VARCHAR(100) NOT NULL
);

CREATE TABLE matches (
    match_id SERIAL PRIMARY KEY,
    home_team_id INT NOT NULL REFERENCES teams (team_id),
    away_team_id INT NOT NULL REFERENCES teams (team_id),
    tournament VARCHAR(100) NOT NULL,
    round_name VARCHAR(30),
    match_date DATE NOT NULL,
    match_time TIME NOT NULL,
    description TEXT,
    poster_url VARCHAR(300),
    CHECK (home_team_id <> away_team_id)
);

CREATE TABLE match_seats (
    seat_id SERIAL PRIMARY KEY,
    match_id INT NOT NULL REFERENCES matches (match_id),
    sector VARCHAR(30) NOT NULL,
    seat_number VARCHAR(10) NOT NULL,
    price DECIMAL(10, 2) NOT NULL CHECK (price > 0),
    status VARCHAR(20) NOT NULL DEFAULT 'Available' CHECK (status IN ('Available', 'Reserved', 'Booked')),
    UNIQUE (match_id, sector, seat_number)
);

CREATE TABLE match_bookings (
    booking_id SERIAL PRIMARY KEY,
    user_id INT NOT NULL REFERENCES users (user_id),
    seat_id INT NOT NULL REFERENCES match_seats (seat_id),
    booking_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(20) NOT NULL CHECK (status IN ('Pending', 'Confirmed', 'Cancelled'))
);

CREATE UNIQUE INDEX one_active_match_booking_per_seat ON match_bookings (seat_id) WHERE status <> 'Cancelled';

CREATE TABLE match_payments (
    payment_id SERIAL PRIMARY KEY,
    booking_id INT NOT NULL UNIQUE REFERENCES match_bookings (booking_id),
    amount DECIMAL(10, 2) NOT NULL CHECK (amount > 0),
    payment_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(20) NOT NULL CHECK (status IN ('Pending', 'Paid', 'Failed'))
);

CREATE TABLE payment_cards (
    card_id SERIAL PRIMARY KEY,
    user_id INT NOT NULL REFERENCES users (user_id) ON DELETE CASCADE,
    holder_name VARCHAR(100) NOT NULL,
    last4 VARCHAR(4) NOT NULL,
    brand VARCHAR(20) NOT NULL,
    exp_month INT NOT NULL CHECK (exp_month BETWEEN 1 AND 12),
    exp_year INT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
