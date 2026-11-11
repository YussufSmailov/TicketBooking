import { Link, useParams } from "react-router-dom";
import { getConcert } from "../api";
import { useData } from "../useData";
import { formatDate, formatTime } from "../format";
import StatusMessage from "../components/StatusMessage";
import SeatBooking from "../components/SeatBooking";
import Poster from "../components/Poster";

function ConcertPage() {
  const { id } = useParams();
  const { data: concert, loading, error, reload } = useData(() => getConcert(id), [id]);

  if (loading || error) {
    return <StatusMessage loading={loading} error={error} onRetry={reload} skeleton="page" />;
  }

  return (
    <section className="event-page">
      <Link className="back-button" to="/concerts">Все концерты</Link>
      <div className="event-header">
        <Poster url={concert.posterUrl} title={concert.artist} className="event-poster" />
        <div className="event-header-info">
          <h2>{concert.artist}</h2>
          <p className="muted">{concert.title}</p>
          <p className="muted">
            {concert.venue}, {concert.city}, {formatDate(concert.concertDate)} в {formatTime(concert.concertTime)}
          </p>
          {concert.description && <p className="event-description">{concert.description}</p>}
        </div>
      </div>
      <SeatBooking key={id} category="concert" eventId={concert.concertId} />
    </section>
  );
}

export default ConcertPage;
