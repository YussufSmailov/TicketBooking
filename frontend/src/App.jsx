import { useState } from "react";
import CategoryTabs from "./components/CategoryTabs";
import EventList from "./components/EventList";
import SeatPicker from "./components/SeatPicker";
import BookingForm from "./components/BookingForm";

function App() {
  const [category, setCategory] = useState("cinema");
  const [selectedEvent, setSelectedEvent] = useState(null);
  const [selectedSeat, setSelectedSeat] = useState(null);
  const [successMessage, setSuccessMessage] = useState("");
  const [reloadKey, setReloadKey] = useState(0);

  function changeCategory(newCategory) {
    setCategory(newCategory);
    setSelectedEvent(null);
    setSelectedSeat(null);
    setSuccessMessage("");
  }

  function openEvent(event) {
    setSelectedEvent(event);
    setSelectedSeat(null);
    setSuccessMessage("");
  }

  function goBack() {
    setSelectedEvent(null);
    setSelectedSeat(null);
    setSuccessMessage("");
  }

  function handleBooked(booking) {
    setSuccessMessage(`Билет забронирован. Номер брони: ${booking.bookingId}, место ${selectedSeat.label}.`);
    setSelectedSeat(null);
    setReloadKey(reloadKey + 1);
  }

  return (
    <div className="app" data-category={category}>
      <header className="header">
        <div className="header-top">
          <h1>Билеты</h1>
        </div>
        <p>Кино, концерты и матчи КПЛ в одном месте</p>
      </header>

      <CategoryTabs active={category} onChange={changeCategory} />

      {selectedEvent === null ? (
        <EventList category={category} onSelect={openEvent} />
      ) : (
        <section className="event-page">
          <button className="back-button" onClick={goBack}>Назад к списку</button>
          <h2>{selectedEvent.title}</h2>
          <p className="muted">{selectedEvent.details}</p>
          <p className="muted">{selectedEvent.place}, {selectedEvent.date} в {selectedEvent.time}</p>

          {successMessage && <p className="success">{successMessage}</p>}

          <SeatPicker
            key={reloadKey}
            category={category}
            eventId={selectedEvent.id}
            selectedSeat={selectedSeat}
            onSelect={setSelectedSeat}
          />

          {selectedSeat && (
            <BookingForm category={category} seat={selectedSeat} onBooked={handleBooked} />
          )}
        </section>
      )}
    </div>
  );
}

export default App;
