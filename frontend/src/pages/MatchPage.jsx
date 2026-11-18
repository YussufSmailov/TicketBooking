import { useState } from "react";
import { Link, useParams } from "react-router-dom";
import { getMatch } from "../api";
import { useData } from "../useData";
import { formatDate, formatTime } from "../format";
import StatusMessage from "../components/StatusMessage";
import SeatBooking from "../components/SeatBooking";
import MatchPoster from "../components/MatchPoster";
import Reviews from "../components/Reviews";
import { RatingBadge } from "../components/Stars";

function MatchPage() {
  const { id } = useParams();
  const { data: match, loading, error, reload } = useData(() => getMatch(id), [id]);
  const [summary, setSummary] = useState(null);

  if (loading || error) {
    return <StatusMessage loading={loading} error={error} onRetry={reload} skeleton="page" />;
  }

  return (
    <section className="event-page">
      <Link className="back-button" to="/sport">Все матчи</Link>
      <div className="event-header">
        <MatchPoster
          homeName={match.homeTeam.name}
          awayName={match.awayTeam.name}
          homeLogoUrl={match.homeTeam.logoUrl}
          awayLogoUrl={match.awayTeam.logoUrl}
          backgroundUrl={match.posterUrl}
          className="event-poster"
        />
        <div className="event-header-info">
          <h2>{match.homeTeam.name} — {match.awayTeam.name}</h2>
          <p className="muted">{match.tournament}{match.roundName ? `, ${match.roundName}` : ""}</p>
          <p className="muted">
            {match.homeTeam.stadium}, {formatDate(match.matchDate)} в {formatTime(match.matchTime)}
          </p>
          <RatingBadge
            rating={summary ? summary.average : match.rating}
            count={summary ? summary.count : match.reviewCount}
            href="#reviews"
          />
          {match.description && <p className="event-description">{match.description}</p>}
        </div>
      </div>
      <SeatBooking key={id} category="sport" eventId={match.matchId} />
      <Reviews eventType="match" eventId={match.matchId} onSummary={setSummary} />
    </section>
  );
}

export default MatchPage;
