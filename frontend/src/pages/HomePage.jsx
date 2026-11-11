import { Link } from "react-router-dom";
import { getHome } from "../api";
import { useData } from "../useData";
import { formatDate } from "../format";
import Carousel from "../components/Carousel";
import StatusMessage from "../components/StatusMessage";
import { cardLink } from "../components/EventCard";

function HomePage() {
  const { data, loading, error, reload } = useData(getHome, []);

  if (loading || error) {
    return (
      <>
        {loading && <div className="skeleton hero-banner" aria-hidden="true"></div>}
        <div className="carousel">
          <StatusMessage loading={loading} error={error} onRetry={reload} skeleton="cards" />
        </div>
      </>
    );
  }

  const sections = data || [];
  const premieres = sections.find((section) => section.id === "premieres");
  const heroCard = premieres && premieres.items.length > 0 ? premieres.items[0] : null;

  return (
    <>
      {heroCard ? (
        <section className="hero hero-banner" style={{ backgroundImage: `linear-gradient(90deg, rgba(8, 10, 16, 0.85), rgba(8, 10, 16, 0.25)), url(https://picsum.photos/seed/hero-${heroCard.id}/1200/500)` }}>
          <div className="hero-content">
            <span className="hero-badge">Премьера</span>
            <h2>{heroCard.title}</h2>
            <p>{heroCard.subtitle} · ближайший сеанс {formatDate(heroCard.date)}</p>
            <Link className="primary-button" to={cardLink(heroCard)}>Купить билет</Link>
          </div>
        </section>
      ) : (
        <section className="hero">
          <h2>Кино, концерты и матчи КПЛ</h2>
          <p>Выберите событие и место — билет будет в личном кабинете.</p>
        </section>
      )}

      {sections.map((section) => (
        <Carousel key={section.id} title={section.title} cards={section.items} />
      ))}
    </>
  );
}

export default HomePage;
