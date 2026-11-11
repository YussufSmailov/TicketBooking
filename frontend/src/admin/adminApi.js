import { API_URL, request } from "../api";

export function getMovies() {
  return request(`${API_URL}/cinema/movies`);
}

export function saveMovie(id, movie) {
  if (id) {
    return request(`${API_URL}/cinema/movies/${id}`, { method: "PUT", body: movie });
  }
  return request(`${API_URL}/cinema/movies`, { method: "POST", body: movie });
}

export function deleteMovie(id) {
  return request(`${API_URL}/cinema/movies/${id}`, { method: "DELETE" });
}

export function getSessions() {
  return request(`${API_URL}/cinema/sessions`);
}

export function createSession(session) {
  return request(`${API_URL}/cinema/sessions`, { method: "POST", body: session });
}

export function deleteSession(id) {
  return request(`${API_URL}/cinema/sessions/${id}`, { method: "DELETE" });
}

export function addSessionSeats(sessionId, row) {
  return request(`${API_URL}/cinema/sessions/${sessionId}/seats`, { method: "POST", body: row });
}

export function getConcerts() {
  return request(`${API_URL}/concert/concerts`);
}

export function saveConcert(id, concert) {
  if (id) {
    return request(`${API_URL}/concert/concerts/${id}`, { method: "PUT", body: concert });
  }
  return request(`${API_URL}/concert/concerts`, { method: "POST", body: concert });
}

export function deleteConcert(id) {
  return request(`${API_URL}/concert/concerts/${id}`, { method: "DELETE" });
}

export function addConcertSeats(concertId, row) {
  return request(`${API_URL}/concert/concerts/${concertId}/seats`, { method: "POST", body: row });
}

export function getTeams() {
  return request(`${API_URL}/sport/teams`);
}

export function getMatches() {
  return request(`${API_URL}/sport/matches`);
}

export function saveMatch(id, match) {
  if (id) {
    return request(`${API_URL}/sport/matches/${id}`, { method: "PUT", body: match });
  }
  return request(`${API_URL}/sport/matches`, { method: "POST", body: match });
}

export function deleteMatch(id) {
  return request(`${API_URL}/sport/matches/${id}`, { method: "DELETE" });
}

export function addMatchSeats(matchId, row) {
  return request(`${API_URL}/sport/matches/${matchId}/seats`, { method: "POST", body: row });
}
