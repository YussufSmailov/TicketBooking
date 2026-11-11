import { useEffect, useState } from "react";
import FieldError from "../components/FieldError";
import SeatGroupForm from "./SeatGroupForm";
import { addSessionSeats, createSession, deleteMovie, deleteSession, getMovies, getSessions, saveMovie } from "./adminApi";
import { todayString } from "../validation";

const ageRatings = ["0+", "6+", "12+", "14+", "16+", "18+", "21+"];

const emptyMovie = {
  title: "",
  durationMin: "",
  ageRating: "12+",
  genre: "",
  releaseDate: "",
  premiere: false,
  posterUrl: "",
  description: ""
};
const emptySession = { movieId: "", sessionDate: "", sessionTime: "", hallNumber: "1" };

function MoviesAdmin() {
  const [movies, setMovies] = useState([]);
  const [sessions, setSessions] = useState([]);
  const [movieForm, setMovieForm] = useState(emptyMovie);
  const [editingId, setEditingId] = useState(null);
  const [movieErrors, setMovieErrors] = useState({});
  const [sessionForm, setSessionForm] = useState(emptySession);
  const [sessionErrors, setSessionErrors] = useState({});
  const [seatsSessionId, setSeatsSessionId] = useState(null);
  const [error, setError] = useState("");

  function loadAll() {
    getMovies().then(setMovies).catch((e) => setError(e.message));
    getSessions().then(setSessions).catch((e) => setError(e.message));
  }

  useEffect(loadAll, []);

  async function submitMovie(event) {
    event.preventDefault();
    setError("");
    setMovieErrors({});
    try {
      await saveMovie(editingId, {
        ...movieForm,
        durationMin: Number(movieForm.durationMin),
        releaseDate: movieForm.releaseDate || null
      });
      setMovieForm(emptyMovie);
      setEditingId(null);
      loadAll();
    } catch (e) {
      setError(e.message);
      setMovieErrors(e.fieldErrors || {});
    }
  }

  function startEdit(movie) {
    setEditingId(movie.movieId);
    const details = movie.details || {};
    setMovieForm({
      title: movie.title,
      durationMin: String(movie.durationMin),
      ageRating: movie.ageRating,
      genre: details.genre || "",
      releaseDate: details.releaseDate || "",
      premiere: Boolean(details.premiere),
      posterUrl: details.posterUrl || "",
      description: details.description || ""
    });
  }

  function changeMovieField(name, value) {
    setMovieForm({ ...movieForm, [name]: value });
  }

  async function removeMovie(id) {
    setError("");
    try {
      await deleteMovie(id);
      loadAll();
    } catch (e) {
      setError(e.message);
    }
  }

  async function submitSession(event) {
    event.preventDefault();
    setError("");
    setSessionErrors({});
    try {
      await createSession({
        ...sessionForm,
        movieId: Number(sessionForm.movieId),
        hallNumber: Number(sessionForm.hallNumber)
      });
      setSessionForm(emptySession);
      loadAll();
    } catch (e) {
      setError(e.message);
      setSessionErrors(e.fieldErrors || {});
    }
  }

  async function removeSession(id) {
    setError("");
    try {
      await deleteSession(id);
      loadAll();
    } catch (e) {
      setError(e.message);
    }
  }

  return (
    <div className="admin-section">
      {error && <p className="error">{error}</p>}

      <h3>{editingId ? "Изменить фильм" : "Новый фильм"}</h3>
      <form className="admin-form" onSubmit={submitMovie}>
        <label>
          Название
          <input value={movieForm.title} maxLength={150} onChange={(e) => setMovieForm({ ...movieForm, title: e.target.value })} required />
          <FieldError message={movieErrors.title} />
        </label>
        <label>
          Длительность, мин
          <input type="number" min="1" max="600" value={movieForm.durationMin} onChange={(e) => setMovieForm({ ...movieForm, durationMin: e.target.value })} required />
          <FieldError message={movieErrors.durationMin} />
        </label>
        <label>
          Возраст
          <select value={movieForm.ageRating} onChange={(e) => setMovieForm({ ...movieForm, ageRating: e.target.value })} required>
            {ageRatings.map((rating) => <option key={rating} value={rating}>{rating}</option>)}
          </select>
          <FieldError message={movieErrors.ageRating} />
        </label>
        <label>
          Жанр
          <input value={movieForm.genre} maxLength={50} onChange={(e) => changeMovieField("genre", e.target.value)} />
          <FieldError message={movieErrors.genre} />
        </label>
        <label>
          Дата выхода
          <input type="date" value={movieForm.releaseDate} onChange={(e) => changeMovieField("releaseDate", e.target.value)} />
          <FieldError message={movieErrors.releaseDate} />
        </label>
        <label className="checkbox-row">
          <input type="checkbox" checked={movieForm.premiere} onChange={(e) => changeMovieField("premiere", e.target.checked)} />
          Премьера
        </label>
        <label>
          Ссылка на постер
          <input type="url" value={movieForm.posterUrl} maxLength={300} placeholder="https://picsum.photos/seed/name/400/600" onChange={(e) => changeMovieField("posterUrl", e.target.value)} />
          <FieldError message={movieErrors.posterUrl} />
        </label>
        <label className="admin-wide">
          Описание
          <textarea rows={3} value={movieForm.description} maxLength={2000} onChange={(e) => changeMovieField("description", e.target.value)} />
          <FieldError message={movieErrors.description} />
        </label>
        <div className="admin-buttons">
          <button className="primary-button" type="submit">{editingId ? "Сохранить" : "Добавить"}</button>
          {editingId && (
            <button className="secondary-button" type="button" onClick={() => { setEditingId(null); setMovieForm(emptyMovie); }}>
              Отмена
            </button>
          )}
        </div>
      </form>

      <ul className="admin-list">
        {movies.map((movie) => (
          <li key={movie.movieId}>
            <span>{movie.title}, {movie.durationMin} мин, {movie.ageRating}</span>
            <span className="admin-buttons">
              <button className="secondary-button" onClick={() => startEdit(movie)}>Изменить</button>
              <button className="danger-button" onClick={() => removeMovie(movie.movieId)}>Удалить</button>
            </span>
          </li>
        ))}
      </ul>

      <h3>Новый сеанс</h3>
      <form className="admin-form" onSubmit={submitSession}>
        <label>
          Фильм
          <select value={sessionForm.movieId} onChange={(e) => setSessionForm({ ...sessionForm, movieId: e.target.value })} required>
            <option value="">Выберите фильм</option>
            {movies.map((movie) => <option key={movie.movieId} value={movie.movieId}>{movie.title}</option>)}
          </select>
          <FieldError message={sessionErrors.movieId} />
        </label>
        <label>
          Дата
          <input type="date" min={todayString()} value={sessionForm.sessionDate} onChange={(e) => setSessionForm({ ...sessionForm, sessionDate: e.target.value })} required />
          <FieldError message={sessionErrors.sessionDate} />
        </label>
        <label>
          Время
          <input type="time" value={sessionForm.sessionTime} onChange={(e) => setSessionForm({ ...sessionForm, sessionTime: e.target.value })} required />
          <FieldError message={sessionErrors.sessionTime} />
        </label>
        <label>
          Зал
          <input type="number" min="1" max="20" value={sessionForm.hallNumber} onChange={(e) => setSessionForm({ ...sessionForm, hallNumber: e.target.value })} required />
          <FieldError message={sessionErrors.hallNumber} />
        </label>
        <button className="primary-button" type="submit">Добавить сеанс</button>
      </form>

      <ul className="admin-list">
        {sessions.map((session) => (
          <li key={session.sessionId}>
            <span>{session.movie.title}: {session.sessionDate} {session.sessionTime.slice(0, 5)}, зал {session.hallNumber}</span>
            <span className="admin-buttons">
              <button className="secondary-button" onClick={() => setSeatsSessionId(session.sessionId)}>Места</button>
              <button className="danger-button" onClick={() => removeSession(session.sessionId)}>Удалить</button>
            </span>
            {seatsSessionId === session.sessionId && (
              <SeatGroupForm onSubmit={(row) => addSessionSeats(session.sessionId, row)} />
            )}
          </li>
        ))}
      </ul>
    </div>
  );
}

export default MoviesAdmin;
