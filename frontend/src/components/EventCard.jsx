import { Link } from "react-router-dom";
import Poster from "./Poster";
import MatchPoster from "./MatchPoster";
import { formatDate, formatPrice, formatRating, formatTime } from "../format";

export function cardLink(card) {
  if (card.type === "movie") return `/movies/${card.id}`;
  if (card.type === "concert") return `/concerts/${card.id}`;
  return `/matches/${card.id}`;
}

function EventCard({ card }) {
  const isMatch = card.type === "match" && (card.homeLogoUrl || card.awayLogoUrl);
  const teams = isMatch ? card.title.split(" — ") : [];
  const hasRating = card.rating !== null && card.rating !== undefined && card.reviewCount > 0;

  return (
    <Link className="event-card" to={cardLink(card)}>
      <div className="event-card-media">
        {isMatch ? (
          <MatchPoster
            homeName={teams[0]}
            awayName={teams[1] || ""}
            homeLogoUrl={card.homeLogoUrl}
            awayLogoUrl={card.awayLogoUrl}
            backgroundUrl={card.posterUrl}
          />
        ) : (
          <Poster url={card.posterUrl} title={card.title} />
        )}
        {hasRating && (
          <span className="card-rating" aria-label={`Рейтинг ${formatRating(card.rating)} из 5`}>
            <span aria-hidden="true">★</span> {formatRating(card.rating)}
          </span>
        )}
      </div>
      <div className="event-card-body">
        <b className="event-card-title">{card.title}</b>
        {card.subtitle && <span className="muted small">{card.subtitle}</span>}
        {card.date && (
          <span className="small">{formatDate(card.date)}{card.time ? `, ${formatTime(card.time)}` : ""}</span>
        )}
        {card.minPrice !== null && card.minPrice !== undefined && (
          <span className="event-card-price">от {formatPrice(card.minPrice)}</span>
        )}
      </div>
    </Link>
  );
}

export default EventCard;
