import { Link, useParams } from "react-router-dom";
import { getSession } from "../api";
import { useData } from "../useData";
import { formatDate, formatTime } from "../format";
import StatusMessage from "../components/StatusMessage";
import SeatBooking from "../components/SeatBooking";
import SessionTimes from "../components/SessionTimes";

function SessionPage() {
  const { id } = useParams();
  const { data: session, loading, error, reload } = useData(() => getSession(id), [id]);

  if (loading || error) {
    return <StatusMessage loading={loading} error={error} onRetry={reload} skeleton="page" />;
  }

  return (
    <section className="event-page">
      <Link className="back-button" to={`/movies/${session.movie.movieId}`}>К фильму</Link>
      <h2>{session.movie.title}</h2>
      <p className="muted">
        {formatDate(session.sessionDate)} в {formatTime(session.sessionTime)}, зал {session.hallNumber}
      </p>
      <SessionTimes session={session} />
      <SeatBooking key={session.sessionId} category="cinema" eventId={session.sessionId} />
    </section>
  );
}

export default SessionPage;
