import { useState } from "react";
import { createBooking } from "../api";
import { formatPrice, plural } from "../format";
import PaymentModal from "./PaymentModal";

function BookingForm({ category, seats, user, onRemove, onCheckout, onFinished, onNeedLogin, onSeatsChanged }) {
  const [error, setError] = useState("");
  const [sending, setSending] = useState(false);
  const [bookings, setBookings] = useState(null);
  const [bookedSeats, setBookedSeats] = useState([]);

  const total = seats.reduce((sum, seat) => sum + seat.price, 0);
  const ticketWord = plural(seats.length, ["билет", "билета", "билетов"]);

  async function handleBook() {
    setError("");
    setSending(true);
    onCheckout(true);
    try {
      const created = await createBooking(category, seats.map((seat) => seat.id));
      setBookedSeats(seats);
      setBookings(created);
      onSeatsChanged();
    } catch (e) {
      setError(e.message);
      onCheckout(false);
      onSeatsChanged();
    } finally {
      setSending(false);
    }
  }

  function handlePaymentFinished(result) {
    const count = bookedSeats.length;
    setBookings(null);
    onFinished(result, count);
  }

  function handleClose() {
    const count = bookedSeats.length;
    setBookings(null);
    onFinished(null, count);
  }

  return (
    <div className="booking-form booking-summary" aria-live="polite">
      <div className="booking-summary-head">
        <h3>{seats.length} {ticketWord} · {formatPrice(total)}</h3>
      </div>
      <ul className="picked-seats" aria-label="Выбранные места">
        {seats.map((seat) => (
          <li key={seat.id} className="picked-seat">
            <span>{seat.label}</span>
            <span className="muted">{formatPrice(seat.price)}</span>
            <button type="button" className="icon-button picked-seat-remove" disabled={bookings !== null} onClick={() => onRemove(seat)} aria-label={`Убрать: ${seat.label}`}>×</button>
          </li>
        ))}
      </ul>
      {user ? (
        <>
          <p className="muted small">Билеты будут оформлены на {user.fullName} ({user.email})</p>
          {error && <p className="error" role="alert">{error}</p>}
          <button className="primary-button" onClick={handleBook} disabled={sending}>
            {sending ? "Бронируем..." : `Перейти к оплате ${formatPrice(total)}`}
          </button>
        </>
      ) : (
        <>
          <p className="muted">Бронирование доступно только после входа.</p>
          <button className="primary-button" onClick={onNeedLogin}>Войти, чтобы забронировать</button>
        </>
      )}
      {bookings && (
        <PaymentModal
          type={category}
          bookingIds={bookings.map((booking) => booking.bookingId)}
          lines={bookedSeats.map((seat) => seat.label)}
          total={bookedSeats.reduce((sum, seat) => sum + seat.price, 0)}
          onFinished={handlePaymentFinished}
          onClose={handleClose}
        />
      )}
    </div>
  );
}

export default BookingForm;
