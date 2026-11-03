import { useEffect, useState } from "react";
import { getData, postData, putData, deleteData } from "./adminApi";
import SeatGroupForm from "./SeatGroupForm";

const emptyMovie = { title: "", durationMin: "", ageRating: "" };
const emptySession = { movieId: "", sessionDate: "", sessionTime: "", hallNumber: "" };

function MoviesAdmin() {
  const [movies, setMovies] = useState([]);
  const [sessions, setSessions] = useState([]);
  const [movieForm, setMovieForm] = useState(emptyMovie);
  const [editingId, setEditingId] = useState(null);
  const [sessionForm, setSessionForm] = useState(emptySession);
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");

  async function loadData() {
    try {
      setMovies(await getData("/cinema/movies"));
      setSessions(await getData("/cinema/sessions"));
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

  function handleMovieChange(e) {
    setMovieForm({ ...movieForm, [e.target.name]: e.target.value });
  }

  function handleSessionChange(e) {
    setSessionForm({ ...sessionForm, [e.target.name]: e.target.value });
  }

  async function saveMovie(e) {
    e.preventDefault();
    const body = {
      title: movieForm.title,
      durationMin: Number(movieForm.durationMin),
      ageRating: movieForm.ageRating
    };

    try {
      if (editingId === null) {
        await postData("/cinema/movies", body);
        showResult("Фильм добавлен");
      } else {
        await putData(`/cinema/movies/${editingId}`, body);
        showResult("Фильм изменён");
      }
      setMovieForm(emptyMovie);
      setEditingId(null);
    } catch (err) {
      setError(err.message);
    }
  }

  function startEdit(movie) {
    setEditingId(movie.movieId);
    setMovieForm({ title: movie.title, durationMin: movie.durationMin, ageRating: movie.ageRating });
  }

  function cancelEdit() {
    setEditingId(null);
    setMovieForm(emptyMovie);
  }

  async function removeMovie(id) {
    if (!window.confirm("Удалить фильм?")) return;
    try {
      await deleteData(`/cinema/movies/${id}`);
      showResult("Фильм удалён");
    } catch (err) {
      setError("Нельзя удалить фильм, у которого есть сеансы. Сначала удалите сеансы.");
    }
  }

  async function saveSession(e) {
    e.preventDefault();
    try {
      await postData("/cinema/sessions", {
        movieId: Number(sessionForm.movieId),
        sessionDate: sessionForm.sessionDate,
        sessionTime: sessionForm.sessionTime,
        hallNumber: Number(sessionForm.hallNumber)
      });
      setSessionForm(emptySession);
      showResult("Сеанс добавлен. Теперь добавьте к нему места.");
    } catch (err) {
      setError(err.message);
    }
  }

  async function removeSession(id) {
    if (!window.confirm("Удалить сеанс и все его места?")) return;
    try {
      await deleteData(`/cinema/sessions/${id}`);
      showResult("Сеанс удалён");
    } catch (err) {
      setError("Нельзя удалить сеанс, на который уже есть брони.");
    }
  }

  const sessionOptions = sessions.map((session) => ({
    id: session.sessionId,
    label: `${session.movie.title}, ${session.sessionDate} ${session.sessionTime.slice(0, 5)}, зал ${session.hallNumber}`
  }));

  return (
    <div>
      {message && <p className="success">{message}</p>}
      {error && <p className="error">{error}</p>}

      <section className="admin-section">
        <h2>{editingId === null ? "Новый фильм" : "Изменить фильм"}</h2>
        <form className="admin-form" onSubmit={saveMovie}>
          <label>
            Название
            <input name="title" value={movieForm.title} onChange={handleMovieChange} required />
          </label>
          <label>
            Длительность, мин
            <input name="durationMin" type="number" min="1" value={movieForm.durationMin} onChange={handleMovieChange} required />
          </label>
          <label>
            Возрастной рейтинг
            <input name="ageRating" value={movieForm.ageRating} onChange={handleMovieChange} placeholder="16+" />
          </label>
          <div className="form-buttons">
            <button className="primary-button" type="submit">
              {editingId === null ? "Добавить фильм" : "Сохранить"}
            </button>
            {editingId !== null && (
              <button className="secondary-button" type="button" onClick={cancelEdit}>Отмена</button>
            )}
          </div>
        </form>

        <div className="table-wrap">
          <table className="admin-table">
            <thead>
              <tr><th>ID</th><th>Название</th><th>Длительность</th><th>Рейтинг</th><th></th></tr>
            </thead>
            <tbody>
              {movies.map((movie) => (
                <tr key={movie.movieId}>
                  <td>{movie.movieId}</td>
                  <td>{movie.title}</td>
                  <td>{movie.durationMin} мин</td>
                  <td>{movie.ageRating}</td>
                  <td className="row-buttons">
                    <button className="secondary-button" onClick={() => startEdit(movie)}>Изменить</button>
                    <button className="danger-button" onClick={() => removeMovie(movie.movieId)}>Удалить</button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </section>

      <section className="admin-section">
        <h2>Новый сеанс</h2>
        <form className="admin-form" onSubmit={saveSession}>
          <label>
            Фильм
            <select name="movieId" value={sessionForm.movieId} onChange={handleSessionChange} required>
              <option value="">Выберите фильм</option>
              {movies.map((movie) => (
                <option key={movie.movieId} value={movie.movieId}>{movie.title}</option>
              ))}
            </select>
          </label>
          <label>
            Дата
            <input name="sessionDate" type="date" value={sessionForm.sessionDate} onChange={handleSessionChange} required />
          </label>
          <label>
            Время
            <input name="sessionTime" type="time" value={sessionForm.sessionTime} onChange={handleSessionChange} required />
          </label>
          <label>
            Зал
            <input name="hallNumber" type="number" min="1" value={sessionForm.hallNumber} onChange={handleSessionChange} required />
          </label>
          <button className="primary-button" type="submit">Добавить сеанс</button>
        </form>

        <div className="table-wrap">
          <table className="admin-table">
            <thead>
              <tr><th>ID</th><th>Фильм</th><th>Дата</th><th>Время</th><th>Зал</th><th></th></tr>
            </thead>
            <tbody>
              {sessions.map((session) => (
                <tr key={session.sessionId}>
                  <td>{session.sessionId}</td>
                  <td>{session.movie.title}</td>
                  <td>{session.sessionDate}</td>
                  <td>{session.sessionTime.slice(0, 5)}</td>
                  <td>{session.hallNumber}</td>
                  <td className="row-buttons">
                    <button className="danger-button" onClick={() => removeSession(session.sessionId)}>Удалить</button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </section>

      <SeatGroupForm
        title="Добавить ряд мест в сеанс"
        events={sessionOptions}
        urlFor={(id) => `/cinema/sessions/${id}/seats`}
        nameLabel="Ряд"
        namePlaceholder="F"
      />
    </div>
  );
}

export default MoviesAdmin;
