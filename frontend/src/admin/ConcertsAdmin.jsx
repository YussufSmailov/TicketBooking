import { useEffect, useState } from "react";
import FieldError from "../components/FieldError";
import SeatGroupForm from "./SeatGroupForm";
import { addConcertSeats, deleteConcert, getConcerts, saveConcert } from "./adminApi";
import { todayString } from "../validation";

const zones = ["Fan zone", "VIP", "Tribune"];

const emptyConcert = {
  artist: "",
  title: "",
  venue: "",
  city: "Almaty",
  concertDate: "",
  concertTime: "",
  posterUrl: "",
  description: ""
};

function ConcertsAdmin() {
  const [concerts, setConcerts] = useState([]);
  const [form, setForm] = useState(emptyConcert);
  const [editingId, setEditingId] = useState(null);
  const [fieldErrors, setFieldErrors] = useState({});
  const [seatsConcertId, setSeatsConcertId] = useState(null);
  const [error, setError] = useState("");

  function loadConcerts() {
    getConcerts().then(setConcerts).catch((e) => setError(e.message));
  }

  useEffect(loadConcerts, []);

  function changeField(name, value) {
    setForm({ ...form, [name]: value });
  }

  async function handleSubmit(event) {
    event.preventDefault();
    setError("");
    setFieldErrors({});
    try {
      await saveConcert(editingId, form);
      setForm(emptyConcert);
      setEditingId(null);
      loadConcerts();
    } catch (e) {
      setError(e.message);
      setFieldErrors(e.fieldErrors || {});
    }
  }

  function startEdit(concert) {
    setEditingId(concert.concertId);
    setForm({
      artist: concert.artist,
      title: concert.title,
      venue: concert.venue,
      city: concert.city,
      concertDate: concert.concertDate,
      concertTime: concert.concertTime.slice(0, 5),
      posterUrl: concert.posterUrl || "",
      description: concert.description || ""
    });
  }

  async function removeConcert(id) {
    setError("");
    try {
      await deleteConcert(id);
      loadConcerts();
    } catch (e) {
      setError(e.message);
    }
  }

  return (
    <div className="admin-section">
      {error && <p className="error">{error}</p>}

      <h3>{editingId ? "Изменить концерт" : "Новый концерт"}</h3>
      <form className="admin-form" onSubmit={handleSubmit}>
        <label>
          Исполнитель
          <input value={form.artist} maxLength={100} onChange={(e) => changeField("artist", e.target.value)} required />
          <FieldError message={fieldErrors.artist} />
        </label>
        <label>
          Название
          <input value={form.title} maxLength={150} onChange={(e) => changeField("title", e.target.value)} required />
          <FieldError message={fieldErrors.title} />
        </label>
        <label>
          Площадка
          <input value={form.venue} maxLength={100} onChange={(e) => changeField("venue", e.target.value)} required />
          <FieldError message={fieldErrors.venue} />
        </label>
        <label>
          Город
          <input value={form.city} maxLength={50} onChange={(e) => changeField("city", e.target.value)} required />
          <FieldError message={fieldErrors.city} />
        </label>
        <label>
          Дата
          <input type="date" min={todayString()} value={form.concertDate} onChange={(e) => changeField("concertDate", e.target.value)} required />
          <FieldError message={fieldErrors.concertDate} />
        </label>
        <label>
          Время
          <input type="time" value={form.concertTime} onChange={(e) => changeField("concertTime", e.target.value)} required />
          <FieldError message={fieldErrors.concertTime} />
        </label>
        <label>
          Ссылка на постер
          <input type="url" value={form.posterUrl} maxLength={300} placeholder="https://picsum.photos/seed/name/400/600" onChange={(e) => changeField("posterUrl", e.target.value)} />
          <FieldError message={fieldErrors.posterUrl} />
        </label>
        <label className="admin-wide">
          Описание
          <textarea rows={3} value={form.description} maxLength={2000} onChange={(e) => changeField("description", e.target.value)} />
          <FieldError message={fieldErrors.description} />
        </label>
        <div className="admin-buttons">
          <button className="primary-button" type="submit">{editingId ? "Сохранить" : "Добавить"}</button>
          {editingId && (
            <button className="secondary-button" type="button" onClick={() => { setEditingId(null); setForm(emptyConcert); }}>
              Отмена
            </button>
          )}
        </div>
      </form>

      <ul className="admin-list">
        {concerts.map((concert) => (
          <li key={concert.concertId}>
            <span>{concert.artist} — {concert.title}, {concert.city}, {concert.concertDate}</span>
            <span className="admin-buttons">
              <button className="secondary-button" onClick={() => setSeatsConcertId(concert.concertId)}>Места</button>
              <button className="secondary-button" onClick={() => startEdit(concert)}>Изменить</button>
              <button className="danger-button" onClick={() => removeConcert(concert.concertId)}>Удалить</button>
            </span>
            {seatsConcertId === concert.concertId && (
              <SeatGroupForm zoneLabel="Зона" zoneOptions={zones} onSubmit={(row) => addConcertSeats(concert.concertId, row)} />
            )}
          </li>
        ))}
      </ul>
    </div>
  );
}

export default ConcertsAdmin;
