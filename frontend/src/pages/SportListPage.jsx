import { getMatches, getTeams } from "../api";
import { useData } from "../useData";
import { useFilters } from "../useFilters";
import { todayString } from "../validation";
import StatusMessage from "../components/StatusMessage";
import EventCard from "../components/EventCard";
import FilterBar, { ratingOptions } from "../components/FilterBar";

function matchCard(match) {
  return {
    type: "match",
    id: match.matchId,
    title: `${match.homeTeam.name} — ${match.awayTeam.name}`,
    subtitle: `${match.roundName ? `${match.roundName} · ` : ""}${match.homeTeam.stadium}`,
    posterUrl: match.posterUrl,
    homeLogoUrl: match.homeTeam.logoUrl,
    awayLogoUrl: match.awayTeam.logoUrl,
    date: match.matchDate,
    time: match.matchTime,
    minPrice: null,
    rating: match.rating,
    reviewCount: match.reviewCount
  };
}

function SportListPage() {
  const { filters, setFilter, reset, active, key } = useFilters(["q", "team", "rating", "from", "to", "sort"]);
  const teams = useData(getTeams, []);
  const { data, loading, error, reload } = useData(() => getMatches({
    q: filters.q,
    teamId: filters.team,
    minRating: filters.rating,
    dateFrom: filters.from,
    dateTo: filters.to,
    sort: filters.sort
  }), [key]);
  const matches = data || [];

  const teamOptions = [...(teams.data || [])].sort((a, b) => a.name.localeCompare(b.name));
  const fields = [
    {
      name: "team", label: "Команда", options: [
        { value: "", label: "Все команды" },
        ...teamOptions.map((team) => ({ value: String(team.teamId), label: `${team.name} (${team.city})` }))
      ]
    },
    { name: "rating", label: "Рейтинг", options: ratingOptions },
    { name: "from", label: "С даты", type: "date", min: todayString() },
    { name: "to", label: "По дату", type: "date", min: filters.from || todayString() },
    {
      name: "sort", label: "Сортировка", options: [
        { value: "", label: "По дате" },
        { value: "rating", label: "По рейтингу" }
      ]
    }
  ];

  return (
    <section>
      <h2 className="page-title">Матчи КПЛ</h2>
      <FilterBar
        filters={filters}
        setFilter={setFilter}
        reset={reset}
        active={active}
        searchPlaceholder="Команда, город, стадион"
        fields={fields}
        count={matches.length}
        loading={loading}
      />
      <StatusMessage
        loading={loading && !data}
        error={error}
        onRetry={reload}
        skeleton="cards"
        empty={!loading && !error && matches.length === 0}
        emptyText={active ? "По этим фильтрам матчей нет." : "Матчей пока нет."}
      />
      <ul className={loading ? "card-grid card-grid-loading" : "card-grid"}>
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
