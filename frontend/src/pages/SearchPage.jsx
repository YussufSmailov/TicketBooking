import { Link, useSearchParams } from "react-router-dom";
import { searchEvents } from "../api";
import { useData } from "../useData";
import { plural } from "../format";
import StatusMessage from "../components/StatusMessage";
import EventCard from "../components/EventCard";

const groups = [
  { type: "movie", title: "Кино", link: "/cinema" },
  { type: "concert", title: "Концерты", link: "/concerts" },
  { type: "match", title: "Матчи КПЛ", link: "/sport" }
];

function SearchPage() {
  const [params] = useSearchParams();
  const query = (params.get("q") || "").trim();
  const tooShort = query.length < 2;
  const { data, loading, error, reload } = useData(
    () => (tooShort ? Promise.resolve([]) : searchEvents(query)),
    [query]
  );
  const cards = data || [];

  return (
    <section>
      <h2 className="page-title">{tooShort ? "Поиск" : `Поиск: «${query}»`}</h2>
      {tooShort ? (
        <p className="muted">Введите в строке поиска хотя бы 2 символа: название фильма, артиста, команду или город.</p>
      ) : (
        <>
          <StatusMessage
            loading={loading}
            error={error}
            onRetry={reload}
            skeleton="cards"
            empty={!loading && !error && cards.length === 0}
            emptyText={`По запросу «${query}» ничего не нашлось. Проверьте написание или поищите по городу.`}
          />
          {!loading && cards.length > 0 && (
            <p className="muted small" aria-live="polite">
              Найдено {cards.length} {plural(cards.length, ["событие", "события", "событий"])}
            </p>
          )}
          {!loading && groups.map((group) => {
            const items = cards.filter((card) => card.type === group.type);
            if (items.length === 0) {
              return null;
            }
            return (
              <section key={group.type} className="search-group" aria-label={group.title}>
                <div className="carousel-header">
                  <h3>{group.title} <span className="muted">{items.length}</span></h3>
                  <Link className="link-button" to={`${group.link}?q=${encodeURIComponent(query)}`}>Фильтры раздела</Link>
                </div>
                <ul className="card-grid">
                  {items.map((card) => (
                    <li key={`${card.type}-${card.id}`}>
                      <EventCard card={card} />
                    </li>
                  ))}
                </ul>
              </section>
            );
          })}
        </>
      )}
    </section>
  );
}

export default SearchPage;
