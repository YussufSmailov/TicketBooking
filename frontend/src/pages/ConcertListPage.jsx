import { getConcerts } from "../api";
import { useData } from "../useData";
import StatusMessage from "../components/StatusMessage";
import EventCard from "../components/EventCard";

function concertCard(concert) {
  return {
    type: "concert",
    id: concert.concertId,
    title: concert.artist,
    subtitle: `${concert.title} · ${concert.venue}, ${concert.city}`,
    posterUrl: concert.posterUrl,
    date: concert.concertDate,
    time: concert.concertTime,
    minPrice: null
  };
}

function ConcertListPage() {
  const { data, loading, error, reload } = useData(getConcerts, []);
  const concerts = data || [];

  return (
    <section>
      <h2 className="page-title">Концерты</h2>
      <StatusMessage loading={loading} error={error} onRetry={reload} skeleton="cards" empty={!loading && concerts.length === 0} emptyText="Концертов пока нет." />
      <ul className="card-grid">
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
