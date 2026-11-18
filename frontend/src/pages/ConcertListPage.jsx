import { getConcerts } from "../api";
import { useData } from "../useData";
import { useFilters } from "../useFilters";
import { formatDuration } from "../format";
import { todayString } from "../validation";
import StatusMessage from "../components/StatusMessage";
import EventCard from "../components/EventCard";
import FilterBar, { ratingOptions } from "../components/FilterBar";

const lengths = {
  short: { minDuration: "", maxDuration: 120 },
  long: { minDuration: 121, maxDuration: "" }
};

function concertCard(concert) {
  const duration = concert.durationMin ? ` · ${formatDuration(concert.durationMin)}` : "";
  return {
    type: "concert",
    id: concert.concertId,
    title: concert.artist,
    subtitle: `${concert.title} · ${concert.venue}, ${concert.city}${duration}`,
    posterUrl: concert.posterUrl,
    date: concert.concertDate,
    time: concert.concertTime,
    minPrice: null,
    rating: concert.rating,
    reviewCount: concert.reviewCount
  };
}

function ConcertListPage() {
  const { filters, setFilter, reset, active, key } = useFilters(["q", "city", "genre", "rating", "length", "from", "to", "sort"]);
  const all = useData(() => getConcerts(), []);
  const length = lengths[filters.length] || {};
  const { data, loading, error, reload } = useData(() => getConcerts({
    q: filters.q,
    city: filters.city,
    genre: filters.genre,
    minRating: filters.rating,
    minDuration: length.minDuration,
    maxDuration: length.maxDuration,
    dateFrom: filters.from,
    dateTo: filters.to,
    sort: filters.sort
  }), [key]);
  const concerts = data || [];

  const allConcerts = all.data || [];
  const cities = [...new Set(allConcerts.map((concert) => concert.city))].sort();
  const genres = [...new Set(allConcerts.map((concert) => concert.genre).filter(Boolean))].sort();
  const fields = [
    { name: "city", label: "Город", options: [{ value: "", label: "Все города" }, ...cities.map((city) => ({ value: city, label: city }))] },
    { name: "genre", label: "Жанр", options: [{ value: "", label: "Все жанры" }, ...genres.map((genre) => ({ value: genre, label: genre }))] },
    { name: "rating", label: "Рейтинг", options: ratingOptions },
    {
      name: "length", label: "Длительность", options: [
        { value: "", label: "Любая" },
        { value: "short", label: "До 2 часов" },
        { value: "long", label: "Больше 2 часов" }
      ]
    },
    { name: "from", label: "С даты", type: "date", min: todayString() },
    { name: "to", label: "По дату", type: "date", min: filters.from || todayString() },
    {
      name: "sort", label: "Сортировка", options: [
        { value: "", label: "По дате" },
        { value: "rating", label: "По рейтингу" },
        { value: "duration", label: "Сначала короткие" },
        { value: "durationDesc", label: "Сначала длинные" }
      ]
    }
  ];

  return (
    <section>
      <h2 className="page-title">Концерты</h2>
      <FilterBar
        filters={filters}
        setFilter={setFilter}
        reset={reset}
        active={active}
        searchPlaceholder="Артист, площадка, город"
        fields={fields}
        count={concerts.length}
        loading={loading}
      />
      <StatusMessage
        loading={loading && !data}
        error={error}
        onRetry={reload}
        skeleton="cards"
        empty={!loading && !error && concerts.length === 0}
        emptyText={active ? "По этим фильтрам концертов нет." : "Концертов пока нет."}
      />
      <ul className={loading ? "card-grid card-grid-loading" : "card-grid"}>
        {concerts.map((concert) => (
          <li key={concert.concertId}>
            <EventCard card={concertCard(concert)} />
          </li>
        ))}
      </ul>
    </section>
  );
}

export default ConcertListPage;
