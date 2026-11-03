import { useState } from "react";
import { postData } from "./adminApi";

function SeatGroupForm({ title, events, urlFor, nameLabel, namePlaceholder }) {
  const [eventId, setEventId] = useState("");
  const [name, setName] = useState("");
  const [count, setCount] = useState(8);
  const [price, setPrice] = useState(2500);
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");

  async function handleSubmit(event) {
    event.preventDefault();
    setMessage("");
    setError("");

    try {
      const seats = await postData(urlFor(eventId), {
        name: name,
        count: Number(count),
        price: Number(price)
      });
      setMessage(`Добавлено мест: ${seats.length}`);
      setName("");
    } catch (e) {
      setError(e.message);
    }
  }

  return (
    <section className="admin-section">
      <h2>{title}</h2>
      <form className="admin-form" onSubmit={handleSubmit}>
        <label>
          Событие
          <select value={eventId} onChange={(e) => setEventId(e.target.value)} required>
            <option value="">Выберите событие</option>
            {events.map((item) => (
              <option key={item.id} value={item.id}>{item.label}</option>
            ))}
          </select>
        </label>
        <label>
          {nameLabel}
          <input value={name} onChange={(e) => setName(e.target.value)} placeholder={namePlaceholder} required />
        </label>
        <label>
          Количество мест
          <input type="number" min="1" max="50" value={count} onChange={(e) => setCount(e.target.value)} required />
        </label>
        <label>
          Цена, ₸
          <input type="number" min="1" value={price} onChange={(e) => setPrice(e.target.value)} required />
        </label>
        <button className="primary-button" type="submit">Добавить места</button>
      </form>
      {message && <p className="success">{message}</p>}
      {error && <p className="error">{error}</p>}
    </section>
  );
}

export default SeatGroupForm;
