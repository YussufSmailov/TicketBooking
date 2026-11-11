import { Link } from "react-router-dom";
import Poster from "./Poster";
import { formatDate, formatPrice, formatTime } from "../format";

export function cardLink(card) {
  if (card.type === "movie") return `/movies/${card.id}`;
  if (card.type === "concert") return `/concerts/${card.id}`;
  return `/matches/${card.id}`;
}

function EventCard({ card }) {
  return (
    <Link className="event-card" to={cardLink(card)}>
      <Poster url={card.posterUrl} title={card.title} />
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
