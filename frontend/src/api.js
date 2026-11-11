import { clearSession, getToken } from "./auth";
import { parseSeatNumber, seatLabel } from "./seats";

export const API_URL = import.meta.env.VITE_API_URL || "http://localhost:8080/api";

function toSeat(seatId, zone, seatNumber, price, status) {
  const place = parseSeatNumber(seatNumber);
  return {
    id: seatId,
    zone: zone,
    row: place.row,
    number: place.seat,
    price: Number(price),
    status: status,
    label: seatLabel(zone, seatNumber)
  };
}

function cinemaSeat(seat) {
  return toSeat(seat.seatId, null, seat.seatNumber, seat.price, seat.status);
}

function concertSeat(seat) {
  return toSeat(seat.seatId, seat.zone, seat.seatNumber, seat.price, seat.status);
}

function matchSeat(seat) {
  return toSeat(seat.seatId, seat.sector, seat.seatNumber, seat.price, seat.status);
}

const categories = {
  cinema: {
    seatsUrl: (id) => `${API_URL}/cinema/sessions/${id}/seats`,
    bookingUrl: `${API_URL}/cinema/bookings`,
    toSeat: cinemaSeat
  },
  concert: {
    seatsUrl: (id) => `${API_URL}/concert/concerts/${id}/seats`,
    bookingUrl: `${API_URL}/concert/bookings`,
    toSeat: concertSeat
  },
  sport: {
    seatsUrl: (id) => `${API_URL}/sport/matches/${id}/seats`,
    bookingUrl: `${API_URL}/sport/bookings`,
    toSeat: matchSeat
  }
};

export async function request(url, options = {}) {
  const headers = { ...(options.headers || {}) };
  if (options.body !== undefined) {
    headers["Content-Type"] = "application/json";
  }
  const token = getToken();
  if (token) {
    headers["Authorization"] = `Bearer ${token}`;
  }

  const response = await fetch(url, {
    method: options.method || "GET",
    headers: headers,
    body: options.body !== undefined ? JSON.stringify(options.body) : undefined
  });

  if (!response.ok) {
    let data = {};
    try {
      data = await response.json();
    } catch {
      data = {};
    }
    if (response.status === 401 && token) {
      clearSession();
    }
    const error = new Error(data.message || "Ошибка сервера");
    error.status = response.status;
    error.fieldErrors = data.errors || {};
    throw error;
  }

  if (response.status === 204) {
    return null;
  }
  return response.json();
}

export async function getSeats(category, eventId) {
  const config = categories[category];
  const data = await request(config.seatsUrl(eventId));
  return data.map(config.toSeat);
}

export function createBooking(category, booking) {
  const config = categories[category];
  return request(config.bookingUrl, { method: "POST", body: booking });
}

export function getMovies() {
  return request(`${API_URL}/cinema/movies`);
}

export function getMovie(id) {
  return request(`${API_URL}/cinema/movies/${id}`);
}

export function getMovieSessions(movieId) {
  return request(`${API_URL}/cinema/movies/${movieId}/sessions`);
}

export function getSessions() {
  return request(`${API_URL}/cinema/sessions`);
}

export function getSession(id) {
  return request(`${API_URL}/cinema/sessions/${id}`);
}

export function getConcerts() {
  return request(`${API_URL}/concert/concerts`);
}

export function getConcert(id) {
  return request(`${API_URL}/concert/concerts/${id}`);
}

export function getMatches() {
  return request(`${API_URL}/sport/matches`);
}

export function getMatch(id) {
  return request(`${API_URL}/sport/matches/${id}`);
}

export function register(form) {
  return request(`${API_URL}/auth/register`, { method: "POST", body: form });
}

export function login(form) {
  return request(`${API_URL}/auth/login`, { method: "POST", body: form });
}

export function getMe() {
  return request(`${API_URL}/me`);
}

export function payBooking(type, bookingId, payment) {
  return request(`${API_URL}/payments/${type}/${bookingId}`, { method: "POST", body: payment });
}

export function getMyCards() {
  return request(`${API_URL}/me/cards`);
}

export function addMyCard(card) {
  return request(`${API_URL}/me/cards`, { method: "POST", body: card });
}

export function deleteMyCard(cardId) {
  return request(`${API_URL}/me/cards/${cardId}`, { method: "DELETE" });
}

export function updateMe(profile) {
  return request(`${API_URL}/me`, { method: "PUT", body: profile });
}

export function getMyBookings() {
  return request(`${API_URL}/me/bookings`);
}

export function cancelMyBooking(type, bookingId) {
  return request(`${API_URL}/me/bookings/${type}/${bookingId}/cancel`, { method: "PUT" });
}

export function getHome() {
  return request(`${API_URL}/home`);
}
