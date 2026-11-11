import { useEffect, useState } from "react";
import FieldError from "../components/FieldError";
import SeatGroupForm from "./SeatGroupForm";
import { addMatchSeats, deleteMatch, getMatches, getTeams, saveMatch } from "./adminApi";
import { todayString } from "../validation";

const sectors = ["Западная", "Восточная", "Северная", "Южная", "VIP"];

const emptyMatch = {
  homeTeamId: "",
  awayTeamId: "",
  tournament: "Kazakhstan Premier League",
  roundName: "",
  matchDate: "",
  matchTime: "",
  posterUrl: "",
  description: ""
};

function MatchesAdmin() {
  const [teams, setTeams] = useState([]);
  const [matches, setMatches] = useState([]);
  const [form, setForm] = useState(emptyMatch);
  const [editingId, setEditingId] = useState(null);
  const [fieldErrors, setFieldErrors] = useState({});
  const [seatsMatchId, setSeatsMatchId] = useState(null);
  const [error, setError] = useState("");

  function loadMatches() {
    getMatches().then(setMatches).catch((e) => setError(e.message));
  }

  useEffect(() => {
    getTeams().then(setTeams).catch((e) => setError(e.message));
    loadMatches();
  }, []);

  function changeField(name, value) {
    setForm({ ...form, [name]: value });
  }

  async function handleSubmit(event) {
    event.preventDefault();
    setError("");
    setFieldErrors({});
    if (form.homeTeamId === form.awayTeamId) {
      setFieldErrors({ awayTeamId: "Команда не может играть сама с собой" });
      return;
    }
    try {
      await saveMatch(editingId, {
        ...form,
        homeTeamId: Number(form.homeTeamId),
        awayTeamId: Number(form.awayTeamId)
      });
      setForm(emptyMatch);
      setEditingId(null);
      loadMatches();
    } catch (e) {
      setError(e.message);
      setFieldErrors(e.fieldErrors || {});
    }
  }

  function startEdit(match) {
    setEditingId(match.matchId);
    setForm({
      homeTeamId: String(match.homeTeam.teamId),
      awayTeamId: String(match.awayTeam.teamId),
      tournament: match.tournament,
      roundName: match.roundName || "",
      matchDate: match.matchDate,
      matchTime: match.matchTime.slice(0, 5),
      posterUrl: match.posterUrl || "",
      description: match.description || ""
    });
  }

  async function removeMatch(id) {
    setError("");
    try {
      await deleteMatch(id);
      loadMatches();
    } catch (e) {
      setError(e.message);
    }
  }

  return (
    <div className="admin-section">
      {error && <p className="error">{error}</p>}

      <h3>{editingId ? "Изменить матч" : "Новый матч"}</h3>
      <form className="admin-form" onSubmit={handleSubmit}>
        <label>
          Хозяева
          <select value={form.homeTeamId} onChange={(e) => changeField("homeTeamId", e.target.value)} required>
            <option value="">Выберите команду</option>
            {teams.map((team) => <option key={team.teamId} value={team.teamId}>{team.name}</option>)}
          </select>
          <FieldError message={fieldErrors.homeTeamId} />
        </label>
        <label>
          Гости
          <select value={form.awayTeamId} onChange={(e) => changeField("awayTeamId", e.target.value)} required>
            <option value="">Выберите команду</option>
            {teams.map((team) => <option key={team.teamId} value={team.teamId}>{team.name}</option>)}
          </select>
          <FieldError message={fieldErrors.awayTeamId} />
        </label>
        <label>
          Турнир
          <input value={form.tournament} maxLength={100} onChange={(e) => changeField("tournament", e.target.value)} required />
          <FieldError message={fieldErrors.tournament} />
        </label>
        <label>
          Тур
          <input value={form.roundName} maxLength={30} onChange={(e) => changeField("roundName", e.target.value)} />
          <FieldError message={fieldErrors.roundName} />
        </label>
        <label>
          Дата
          <input type="date" min={todayString()} value={form.matchDate} onChange={(e) => changeField("matchDate", e.target.value)} required />
          <FieldError message={fieldErrors.matchDate} />
        </label>
        <label>
          Время
          <input type="time" value={form.matchTime} onChange={(e) => changeField("matchTime", e.target.value)} required />
          <FieldError message={fieldErrors.matchTime} />
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
            <button className="secondary-button" type="button" onClick={() => { setEditingId(null); setForm(emptyMatch); }}>
              Отмена
            </button>
          )}
        </div>
      </form>

      <ul className="admin-list">
        {matches.map((match) => (
          <li key={match.matchId}>
            <span>{match.homeTeam.name} — {match.awayTeam.name}, {match.matchDate} {match.matchTime.slice(0, 5)}</span>
            <span className="admin-buttons">
              <button className="secondary-button" onClick={() => setSeatsMatchId(match.matchId)}>Места</button>
              <button className="secondary-button" onClick={() => startEdit(match)}>Изменить</button>
              <button className="danger-button" onClick={() => removeMatch(match.matchId)}>Удалить</button>
            </span>
            {seatsMatchId === match.matchId && (
              <SeatGroupForm zoneLabel="Сектор" zoneOptions={sectors} onSubmit={(row) => addMatchSeats(match.matchId, row)} />
            )}
          </li>
        ))}
      </ul>
    </div>
  );
}

export default MatchesAdmin;
