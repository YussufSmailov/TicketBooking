import { getMatches } from "../api";
import { useData } from "../useData";
import StatusMessage from "../components/StatusMessage";
import EventCard from "../components/EventCard";

function matchCard(match) {
  return {
    type: "match",
    id: match.matchId,
    title: `${match.homeTeam.name} — ${match.awayTeam.name}`,
    subtitle: `${match.roundName ? `${match.roundName} · ` : ""}${match.homeTeam.stadium}`,
    posterUrl: match.posterUrl,
    date: match.matchDate,
    time: match.matchTime,
    minPrice: null
  };
}

function SportListPage() {
  const { data, loading, error, reload } = useData(getMatches, []);
  const matches = data || [];

  return (
    <section>
      <h2 className="page-title">Матчи КПЛ</h2>
      <StatusMessage loading={loading} error={error} onRetry={reload} skeleton="cards" empty={!loading && matches.length === 0} emptyText="Матчей пока нет." />
      <ul className="card-grid">
        {matches.map((match) => (
          <li key={match.matchId}>
            <EventCard card={matchCard(match)} />
          </li>
        ))}
      </ul>
    </section>
  );
}

export default SportListPage;
