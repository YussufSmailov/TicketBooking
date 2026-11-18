import { useState } from "react";
import { Link, useParams } from "react-router-dom";
import { getConcert } from "../api";
import { useData } from "../useData";
import { formatDate, formatDuration, formatTime } from "../format";
import StatusMessage from "../components/StatusMessage";
import SeatBooking from "../components/SeatBooking";
import Poster from "../components/Poster";
import Reviews from "../components/Reviews";
import { RatingBadge } from "../components/Stars";

function ConcertPage() {
  const { id } = useParams();
  const { data: concert, loading, error, reload } = useData(() => getConcert(id), [id]);
  const [summary, setSummary] = useState(null);

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
          <p className="muted">{concert.title}{concert.genre ? ` · ${concert.genre}` : ""}</p>
          <p className="muted">
            {concert.venue}, {concert.city}, {formatDate(concert.concertDate)} в {formatTime(concert.concertTime)}
            {concert.durationMin ? ` · ${formatDuration(concert.durationMin)}` : ""}
          </p>
          <RatingBadge
            rating={summary ? summary.average : concert.rating}
            count={summary ? summary.count : concert.reviewCount}
            href="#reviews"
          />
          {concert.description && <p className="event-description">{concert.description}</p>}
        </div>
      </div>
      <SeatBooking key={id} category="concert" eventId={concert.concertId} />
      <Reviews eventType="concert" eventId={concert.concertId} onSummary={setSummary} />
    </section>
  );
}

export default ConcertPage;
