import { Link } from "react-router-dom";
import { getMovieSessions } from "../api";
import { useData } from "../useData";
import { formatTime } from "../format";

function SessionTimes({ session }) {
  const { data } = useData(() => getMovieSessions(session.movie.movieId), [session.movie.movieId]);
  const sameDaySessions = (data || []).filter((item) => item.sessionDate === session.sessionDate);

  if (sameDaySessions.length < 2) {
    return null;
  }

  return (
    <nav className="time-switcher" aria-label="Другие сеансы в этот день">
      {sameDaySessions.map((item) => (
        <Link
          key={item.sessionId}
          to={`/sessions/${item.sessionId}`}
          className={item.sessionId === session.sessionId ? "time-chip time-chip-active" : "time-chip"}
          aria-current={item.sessionId === session.sessionId ? "page" : undefined}
        >
          {formatTime(item.sessionTime)}
          <span>зал {item.hallNumber}</span>
        </Link>
      ))}
    </nav>
  );
}

export default SessionTimes;
