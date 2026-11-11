import { getMovies } from "../api";
import { useData } from "../useData";
import StatusMessage from "../components/StatusMessage";
import EventCard from "../components/EventCard";

function movieCard(movie) {
  const details = movie.details || {};
  return {
    type: "movie",
    id: movie.movieId,
    title: movie.title,
    subtitle: `${details.genre || "Кино"}, ${movie.durationMin} мин, ${movie.ageRating}`,
    posterUrl: details.posterUrl,
    date: null,
    minPrice: null
  };
}

function CinemaListPage() {
  const { data, loading, error, reload } = useData(getMovies, []);
  const movies = data || [];

  return (
    <section>
      <h2 className="page-title">Кино</h2>
      <StatusMessage loading={loading} error={error} onRetry={reload} skeleton="cards" empty={!loading && movies.length === 0} emptyText="Фильмов пока нет." />
      <ul className="card-grid">
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
