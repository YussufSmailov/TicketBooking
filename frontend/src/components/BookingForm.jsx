import { useState } from "react";
import { createBooking } from "../api";
import { formatPrice } from "../format";
import PaymentModal from "./PaymentModal";

function BookingForm({ category, seat, user, onFinished, onNeedLogin, onSeatsChanged }) {
  const [error, setError] = useState("");
  const [sending, setSending] = useState(false);
  const [booking, setBooking] = useState(null);

  async function handleBook() {
    setError("");
    setSending(true);
    try {
      const created = await createBooking(category, { seatId: seat.id });
      setBooking(created);
      onSeatsChanged();
    } catch (e) {
      setError(e.message);
      onSeatsChanged();
    } finally {
      setSending(false);
    }
  }

  function handlePaymentFinished(result) {
    setBooking(null);
    onFinished(result, seat);
  }

  function handleClose() {
    setBooking(null);
    onFinished(null, seat);
  }

  return (
    <div className="booking-form">
      <h3>{seat.label}, {formatPrice(seat.price)}</h3>
      {user ? (
        <>
          <p className="muted">Билет будет оформлен на {user.fullName} ({user.email})</p>
          {error && <p className="error">{error}</p>}
          <button className="primary-button" onClick={handleBook} disabled={sending}>
            {sending ? "Бронируем..." : "Перейти к оплате"}
          </button>
        </>
      ) : (
        <>
          <p className="muted">Бронирование доступно только после входа.</p>
          <button className="primary-button" onClick={onNeedLogin}>Войти, чтобы забронировать</button>
        </>
      )}
      {booking && (
        <PaymentModal
          type={category}
          booking={booking}
          seat={seat}
          onFinished={handlePaymentFinished}
          onClose={handleClose}
        />
      )}
    </div>
  );
}

export default BookingForm;
