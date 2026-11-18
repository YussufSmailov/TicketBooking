import { useState } from "react";
import { Link, useParams } from "react-router-dom";
import { getMovie, getMovieSessions } from "../api";
import { useData } from "../useData";
import { formatDate, formatDuration, formatTime } from "../format";
import StatusMessage from "../components/StatusMessage";
import Poster from "../components/Poster";
import Reviews from "../components/Reviews";
import { RatingBadge } from "../components/Stars";

function groupByDate(sessions) {
  const days = [];
  sessions.forEach((session) => {
    let day = days.find((item) => item.date === session.sessionDate);
    if (!day) {
      day = { date: session.sessionDate, sessions: [] };
      days.push(day);
    }
    day.sessions.push(session);
  });
  return days;
}

function MoviePage() {
  const { id } = useParams();
  const movie = useData(() => getMovie(id), [id]);
  const sessions = useData(() => getMovieSessions(id), [id]);
  const [summary, setSummary] = useState(null);

  if (movie.loading || movie.error) {
    return <StatusMessage loading={movie.loading} error={movie.error} onRetry={movie.reload} skeleton="page" />;
  }

  const details = movie.data.details || {};
  const days = groupByDate(sessions.data || []);

  return (
    <section className="event-page">
      <Link className="back-button" to="/cinema">Все фильмы</Link>
      <div className="event-header">
        <Poster url={details.posterUrl} title={movie.data.title} className="event-poster" />
        <div className="event-header-info">
          {details.premiere && <span className="hero-badge">Премьера</span>}
          <h2>{movie.data.title}</h2>
          <p className="muted">
            {details.genre ? `${details.genre}, ` : ""}{formatDuration(movie.data.durationMin)}, {movie.data.ageRating}
          </p>
          <RatingBadge
            rating={summary ? summary.average : movie.data.rating}
            count={summary ? summary.count : movie.data.reviewCount}
            href="#reviews"
          />
          {details.releaseDate && <p className="muted">В прокате с {formatDate(details.releaseDate)}</p>}
          {details.description && <p className="event-description">{details.description}</p>}
        </div>
      </div>

      <h3 className="section-title">Сеансы</h3>
      <StatusMessage
        loading={sessions.loading}
        error={sessions.error}
        empty={!sessions.loading && days.length === 0}
        emptyText="Сеансов пока нет."
      />
      {days.map((day) => (
        <div key={day.date} className="session-day">
          <h4>{formatDate(day.date)}</h4>
          <ul className="session-list">
            {day.sessions.map((session) => (
              <li key={session.sessionId}>
                <Link className="session-chip" to={`/sessions/${session.sessionId}`}>
                  <b>{formatTime(session.sessionTime)}</b>
                  <span>зал {session.hallNumber}</span>
                </Link>
              </li>
            ))}
          </ul>
        </div>
      ))}

      <Reviews eventType="movie" eventId={movie.data.movieId} onSummary={setSummary} />
    </section>
  );
}

export default MoviePage;
