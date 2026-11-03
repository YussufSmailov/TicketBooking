import { useEffect, useState } from "react";
import { getData, postData, putData, deleteData } from "./adminApi";
import SeatGroupForm from "./SeatGroupForm";

const emptyMatch = {
  homeTeamId: "",
  awayTeamId: "",
  tournament: "Kazakhstan Premier League",
  roundName: "",
  matchDate: "",
  matchTime: ""
};

function MatchesAdmin() {
  const [teams, setTeams] = useState([]);
  const [matches, setMatches] = useState([]);
  const [form, setForm] = useState(emptyMatch);
  const [editingId, setEditingId] = useState(null);
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");

  async function loadData() {
    try {
      setTeams(await getData("/sport/teams"));
      setMatches(await getData("/sport/matches"));
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

  async function saveMatch(e) {
    e.preventDefault();

    if (form.homeTeamId === form.awayTeamId) {
      setError("Команда не может играть сама с собой");
      return;
    }

    const body = {
      homeTeam: { teamId: Number(form.homeTeamId) },
      awayTeam: { teamId: Number(form.awayTeamId) },
      tournament: form.tournament,
      roundName: form.roundName,
      matchDate: form.matchDate,
      matchTime: form.matchTime
    };

    try {
      if (editingId === null) {
        await postData("/sport/matches", body);
        showResult("Матч добавлен. Теперь добавьте к нему места.");
      } else {
        await putData(`/sport/matches/${editingId}`, body);
        showResult("Матч изменён");
      }
      setForm(emptyMatch);
      setEditingId(null);
    } catch (err) {
      setError(err.message);
    }
  }

  function startEdit(match) {
    setEditingId(match.matchId);
    setForm({
      homeTeamId: match.homeTeam.teamId,
      awayTeamId: match.awayTeam.teamId,
      tournament: match.tournament,
      roundName: match.roundName,
      matchDate: match.matchDate,
      matchTime: match.matchTime.slice(0, 5)
    });
  }

  function cancelEdit() {
    setEditingId(null);
    setForm(emptyMatch);
  }

  async function removeMatch(id) {
    if (!window.confirm("Удалить матч и все его места?")) return;
    try {
      await deleteData(`/sport/matches/${id}`);
      showResult("Матч удалён");
    } catch (err) {
      setError("Нельзя удалить матч, на который уже есть брони.");
    }
  }

  const matchOptions = matches.map((match) => ({
    id: match.matchId,
    label: `${match.homeTeam.name} — ${match.awayTeam.name}, ${match.matchDate}`
  }));

  return (
    <div>
      {message && <p className="success">{message}</p>}
      {error && <p className="error">{error}</p>}

      <section className="admin-section">
        <h2>{editingId === null ? "Новый матч" : "Изменить матч"}</h2>
        <form className="admin-form" onSubmit={saveMatch}>
          <label>
            Хозяева
            <select name="homeTeamId" value={form.homeTeamId} onChange={handleChange} required>
              <option value="">Выберите клуб</option>
              {teams.map((team) => (
                <option key={team.teamId} value={team.teamId}>{team.name}</option>
              ))}
            </select>
          </label>
          <label>
            Гости
            <select name="awayTeamId" value={form.awayTeamId} onChange={handleChange} required>
              <option value="">Выберите клуб</option>
              {teams.map((team) => (
                <option key={team.teamId} value={team.teamId}>{team.name}</option>
              ))}
            </select>
          </label>
          <label>
            Турнир
            <input name="tournament" value={form.tournament} onChange={handleChange} required />
          </label>
          <label>
            Тур
            <input name="roundName" value={form.roundName} onChange={handleChange} placeholder="Round 27" />
          </label>
          <label>
            Дата
            <input name="matchDate" type="date" value={form.matchDate} onChange={handleChange} required />
          </label>
          <label>
            Время
            <input name="matchTime" type="time" value={form.matchTime} onChange={handleChange} required />
          </label>
          <div className="form-buttons">
            <button className="primary-button" type="submit">
              {editingId === null ? "Добавить матч" : "Сохранить"}
            </button>
            {editingId !== null && (
              <button className="secondary-button" type="button" onClick={cancelEdit}>Отмена</button>
            )}
          </div>
        </form>

        <div className="table-wrap">
          <table className="admin-table">
            <thead>
              <tr><th>ID</th><th>Матч</th><th>Стадион</th><th>Тур</th><th>Дата</th><th></th></tr>
            </thead>
            <tbody>
              {matches.map((match) => (
                <tr key={match.matchId}>
                  <td>{match.matchId}</td>
                  <td>{match.homeTeam.name} — {match.awayTeam.name}</td>
                  <td>{match.homeTeam.stadium}</td>
                  <td>{match.roundName}</td>
                  <td>{match.matchDate} {match.matchTime.slice(0, 5)}</td>
                  <td className="row-buttons">
                    <button className="secondary-button" onClick={() => startEdit(match)}>Изменить</button>
                    <button className="danger-button" onClick={() => removeMatch(match.matchId)}>Удалить</button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </section>

      <SeatGroupForm
        title="Добавить сектор мест на матч"
        events={matchOptions}
        urlFor={(id) => `/sport/matches/${id}/seats`}
        nameLabel="Сектор"
        namePlaceholder="East"
      />
    </div>
  );
}

export default MatchesAdmin;
