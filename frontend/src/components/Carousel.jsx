import { useRef } from "react";
import EventCard from "./EventCard";

function Carousel({ title, cards }) {
  const trackRef = useRef(null);

  function scroll(direction) {
    const track = trackRef.current;
    if (track) {
      track.scrollBy({ left: direction * track.clientWidth * 0.8, behavior: "smooth" });
    }
  }

  return (
    <section className="carousel" aria-label={title}>
      <div className="carousel-header">
        <h3>{title}</h3>
        {cards.length > 2 && (
          <div className="carousel-buttons">
            <button type="button" className="icon-button" onClick={() => scroll(-1)} aria-label={`${title}: назад`}>‹</button>
            <button type="button" className="icon-button" onClick={() => scroll(1)} aria-label={`${title}: вперёд`}>›</button>
          </div>
        )}
      </div>
      {cards.length === 0 ? (
        <p className="muted">Пока ничего нет.</p>
      ) : (
        <ul className="carousel-track" ref={trackRef}>
          {cards.map((card) => (
            <li key={`${card.type}-${card.id}`} className="carousel-item">
              <EventCard card={card} />
            </li>
          ))}
        </ul>
      )}
    </section>
  );
}

export default Carousel;
