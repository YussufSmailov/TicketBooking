import { useEffect, useState } from "react";
import { getEvents } from "../api";

function EventList({ category, onSelect }) {
  const [events, setEvents] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    setLoading(true);
    setError("");
    getEvents(category)
      .then((data) => setEvents(data))
      .catch(() => setError("Не удалось загрузить события. Проверьте, что backend запущен на порту 8080."))
      .finally(() => setLoading(false));
  }, [category]);

  if (loading) {
    return <p className="muted">Загружаем события...</p>;
  }

  if (error) {
    return <p className="error">{error}</p>;
  }

  if (events.length === 0) {
    return <p className="muted">Событий пока нет. Добавьте их через API.</p>;
  }

  return (
    <ul className="event-list">
      {events.map((event) => (
        <li key={event.id} className="event-row">
          <div className="event-date">
            <span className="event-day">{event.date.slice(8, 10)}.{event.date.slice(5, 7)}</span>
            <span className="event-time">{event.time}</span>
          </div>
          <div className="event-info">
            <h3>{event.title}</h3>
            <p className="muted">{event.details}</p>
            <p className="muted">{event.place}</p>
          </div>
          <button className="primary-button" onClick={() => onSelect(event)}>Выбрать место</button>
        </li>
      ))}
    </ul>
  );
}

export default EventList;
