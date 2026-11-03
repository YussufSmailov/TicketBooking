import { useEffect, useState } from "react";
import { getData, postData, putData, deleteData } from "./adminApi";
import SeatGroupForm from "./SeatGroupForm";

const emptyConcert = { artist: "", title: "", venue: "", city: "", concertDate: "", concertTime: "" };

function ConcertsAdmin() {
  const [concerts, setConcerts] = useState([]);
  const [form, setForm] = useState(emptyConcert);
  const [editingId, setEditingId] = useState(null);
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");

  async function loadData() {
    try {
      setConcerts(await getData("/concert/concerts"));
    } catch {
      setError("Не удалось загрузить данные. Проверьте, что backend запущен.");
    }
  }

  useEffect(() => {
    loadData();
  }, []);

  function showResult(text) {
    setMessage(text);
    setError("");
    loadData();
  }

  function handleChange(e) {
    setForm({ ...form, [e.target.name]: e.target.value });
  }

  async function saveConcert(e) {
    e.preventDefault();
    try {
      if (editingId === null) {
        await postData("/concert/concerts", form);
        showResult("Концерт добавлен. Теперь добавьте к нему места.");
      } else {
        await putData(`/concert/concerts/${editingId}`, form);
        showResult("Концерт изменён");
      }
      setForm(emptyConcert);
      setEditingId(null);
    } catch (err) {
      setError(err.message);
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
      concertTime: concert.concertTime.slice(0, 5)
    });
  }

  function cancelEdit() {
    setEditingId(null);
    setForm(emptyConcert);
  }

  async function removeConcert(id) {
    if (!window.confirm("Удалить концерт и все его места?")) return;
    try {
      await deleteData(`/concert/concerts/${id}`);
      showResult("Концерт удалён");
    } catch (err) {
      setError("Нельзя удалить концерт, на который уже есть брони.");
    }
  }

  const concertOptions = concerts.map((concert) => ({
    id: concert.concertId,
    label: `${concert.artist}, ${concert.concertDate}`
  }));

  return (
    <div>
      {message && <p className="success">{message}</p>}
      {error && <p className="error">{error}</p>}

      <section className="admin-section">
        <h2>{editingId === null ? "Новый концерт" : "Изменить концерт"}</h2>
        <form className="admin-form" onSubmit={saveConcert}>
          <label>
            Артист
            <input name="artist" value={form.artist} onChange={handleChange} required />
          </label>
          <label>
            Название шоу
            <input name="title" value={form.title} onChange={handleChange} required />
          </label>
          <label>
            Площадка
            <input name="venue" value={form.venue} onChange={handleChange} required />
          </label>
          <label>
            Город
            <input name="city" value={form.city} onChange={handleChange} required />
          </label>
          <label>
            Дата
            <input name="concertDate" type="date" value={form.concertDate} onChange={handleChange} required />
          </label>
          <label>
            Время
            <input name="concertTime" type="time" value={form.concertTime} onChange={handleChange} required />
          </label>
          <div className="form-buttons">
            <button className="primary-button" type="submit">
              {editingId === null ? "Добавить концерт" : "Сохранить"}
            </button>
            {editingId !== null && (
              <button className="secondary-button" type="button" onClick={cancelEdit}>Отмена</button>
            )}
          </div>
        </form>

        <div className="table-wrap">
          <table className="admin-table">
            <thead>
              <tr><th>ID</th><th>Артист</th><th>Шоу</th><th>Площадка</th><th>Дата</th><th></th></tr>
            </thead>
            <tbody>
              {concerts.map((concert) => (
                <tr key={concert.concertId}>
                  <td>{concert.concertId}</td>
                  <td>{concert.artist}</td>
                  <td>{concert.title}</td>
                  <td>{concert.venue}, {concert.city}</td>
                  <td>{concert.concertDate} {concert.concertTime.slice(0, 5)}</td>
                  <td className="row-buttons">
                    <button className="secondary-button" onClick={() => startEdit(concert)}>Изменить</button>
                    <button className="danger-button" onClick={() => removeConcert(concert.concertId)}>Удалить</button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </section>

      <SeatGroupForm
        title="Добавить зону мест на концерт"
        events={concertOptions}
        urlFor={(id) => `/concert/concerts/${id}/seats`}
        nameLabel="Зона"
        namePlaceholder="VIP"
      />
    </div>
  );
}

export default ConcertsAdmin;
