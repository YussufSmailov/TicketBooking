const API_URL = "http://localhost:8080/api";

function shortTime(time) {
  return time.slice(0, 5);
}

const categories = {
  cinema: {
    eventsUrl: `${API_URL}/cinema/sessions`,
    seatsUrl: (id) => `${API_URL}/cinema/sessions/${id}/seats`,
    bookingUrl: `${API_URL}/cinema/bookings`,
    toEvent: (session) => ({
      id: session.sessionId,
      title: session.movie.title,
      details: `${session.movie.durationMin} мин, ${session.movie.ageRating}`,
      place: `Зал ${session.hallNumber}`,
      date: session.sessionDate,
      time: shortTime(session.sessionTime)
    }),
    toSeat: (seat) => ({
      id: seat.seatId,
      group: `Ряд ${seat.seatNumber.charAt(0)}`,
      label: seat.seatNumber,
      price: seat.price,
      status: seat.status
    })
  },
  concert: {
    eventsUrl: `${API_URL}/concert/concerts`,
    seatsUrl: (id) => `${API_URL}/concert/concerts/${id}/seats`,
    bookingUrl: `${API_URL}/concert/bookings`,
    toEvent: (concert) => ({
      id: concert.concertId,
      title: concert.artist,
      details: concert.title,
      place: `${concert.venue}, ${concert.city}`,
      date: concert.concertDate,
      time: shortTime(concert.concertTime)
    }),
    toSeat: (seat) => ({
      id: seat.seatId,
      group: seat.zone,
      label: seat.seatNumber,
      price: seat.price,
      status: seat.status
    })
  },
  sport: {
    eventsUrl: `${API_URL}/sport/matches`,
    seatsUrl: (id) => `${API_URL}/sport/matches/${id}/seats`,
    bookingUrl: `${API_URL}/sport/bookings`,
    toEvent: (match) => ({
      id: match.matchId,
      title: `${match.homeTeam.name} — ${match.awayTeam.name}`,
      details: `${match.tournament}, ${match.roundName}`,
      place: `${match.homeTeam.stadium}, ${match.homeTeam.city}`,
      date: match.matchDate,
      time: shortTime(match.matchTime)
    }),
    toSeat: (seat) => ({
      id: seat.seatId,
      group: seat.sector,
      label: seat.seatNumber,
      price: seat.price,
      status: seat.status
    })
  }
};

async function readError(response) {
  try {
    const data = await response.json();
    return data.message || "Ошибка сервера";
  } catch {
    return "Ошибка сервера";
  }
}

export async function getEvents(category) {
  const config = categories[category];
  const response = await fetch(config.eventsUrl);
  if (!response.ok) {
    throw new Error(await readError(response));
  }
  const data = await response.json();
  return data.map(config.toEvent);
}

export async function getSeats(category, eventId) {
  const config = categories[category];
  const response = await fetch(config.seatsUrl(eventId));
  if (!response.ok) {
    throw new Error(await readError(response));
  }
  const data = await response.json();
  return data.map(config.toSeat);
}

export async function createBooking(category, booking) {
  const config = categories[category];
  const response = await fetch(config.bookingUrl, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(booking)
  });
  if (!response.ok) {
    throw new Error(await readError(response));
  }
  return response.json();
}
