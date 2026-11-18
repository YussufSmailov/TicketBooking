import { getMovies } from "../api";
import { useData } from "../useData";
import { useFilters } from "../useFilters";
import { formatDate } from "../format";
import { todayString } from "../validation";
import StatusMessage from "../components/StatusMessage";
import EventCard from "../components/EventCard";
import FilterBar, { ratingOptions } from "../components/FilterBar";

const lengths = {
  short: { minDuration: "", maxDuration: 105 },
  medium: { minDuration: 106, maxDuration: 140 },
  long: { minDuration: 141, maxDuration: "" }
};

function movieCard(movie) {
  const details = movie.details || {};
  return {
    type: "movie",
    id: movie.movieId,
    title: movie.title,
    subtitle: `${details.genre || "Кино"}, ${movie.durationMin} мин, ${movie.ageRating}`,
    posterUrl: details.posterUrl,
    date: null,
    minPrice: null,
    rating: movie.rating,
    reviewCount: movie.reviewCount
  };
}

function nextDays(count) {
  const days = [];
  const [year, month, day] = todayString().split("-").map(Number);
  for (let i = 0; i < count; i++) {
    const date = new Date(year, month - 1, day + i);
    const text = `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, "0")}-${String(date.getDate()).padStart(2, "0")}`;
    days.push({ value: text, label: i === 0 ? `Сегодня, ${formatDate(text)}` : i === 1 ? `Завтра, ${formatDate(text)}` : formatDate(text) });
  }
  return days;
}

function CinemaListPage() {
  const { filters, setFilter, reset, active, key } = useFilters(["q", "genre", "rating", "length", "date", "sort"]);
  const allMovies = useData(() => getMovies(), []);
  const length = lengths[filters.length] || {};
  const { data, loading, error, reload } = useData(() => getMovies({
    q: filters.q,
    genre: filters.genre,
    minRating: filters.rating,
    minDuration: length.minDuration,
    maxDuration: length.maxDuration,
    date: filters.date,
    sort: filters.sort
  }), [key]);
  const movies = data || [];

  const genres = [...new Set((allMovies.data || []).map((movie) => movie.details && movie.details.genre).filter(Boolean))].sort();
  const fields = [
    { name: "genre", label: "Жанр", options: [{ value: "", label: "Все жанры" }, ...genres.map((genre) => ({ value: genre, label: genre }))] },
    { name: "rating", label: "Рейтинг", options: ratingOptions },
    {
      name: "length", label: "Длительность", options: [
        { value: "", label: "Любая" },
        { value: "short", label: "До 1 ч 45 мин" },
        { value: "medium", label: "1 ч 45 мин – 2 ч 20 мин" },
        { value: "long", label: "Дольше 2 ч 20 мин" }
      ]
    },
    { name: "date", label: "Дата сеанса", options: [{ value: "", label: "Любой день" }, ...nextDays(14)] },
    {
      name: "sort", label: "Сортировка", options: [
        { value: "", label: "По рейтингу" },
        { value: "release", label: "Сначала новые" },
        { value: "title", label: "По названию" },
        { value: "duration", label: "Сначала короткие" },
        { value: "durationDesc", label: "Сначала длинные" }
      ]
    }
  ];

  return (
    <section>
      <h2 className="page-title">Кино</h2>
      <FilterBar
        filters={filters}
        setFilter={setFilter}
        reset={reset}
        active={active}
        searchPlaceholder="Название, жанр, актёр"
        fields={fields}
        count={movies.length}
        loading={loading}
      />
      <StatusMessage
        loading={loading && !data}
        error={error}
        onRetry={reload}
        skeleton="cards"
        empty={!loading && !error && movies.length === 0}
        emptyText={active ? "По этим фильтрам фильмов нет. Попробуйте ослабить условия." : "Фильмов пока нет."}
      />
      <ul className={loading ? "card-grid card-grid-loading" : "card-grid"}>
        {movies.map((movie) => (
          <li key={movie.movieId}>
            <EventCard card={movieCard(movie)} />
          </li>
        ))}
      </ul>
    </section>
  );
}

export default CinemaListPage;
