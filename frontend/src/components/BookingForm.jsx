import { useState } from "react";
import { createBooking } from "../api";

function BookingForm({ category, seat, onBooked }) {
  const [fullName, setFullName] = useState("");
  const [email, setEmail] = useState("");
  const [phone, setPhone] = useState("");
  const [error, setError] = useState("");
  const [sending, setSending] = useState(false);

  async function handleSubmit(event) {
    event.preventDefault();
    setError("");
    setSending(true);

    try {
      const booking = await createBooking(category, {
        fullName: fullName,
        email: email,
        phone: phone,
        seatId: seat.id
      });
      onBooked(booking);
    } catch (e) {
      setError(e.message);
    } finally {
      setSending(false);
    }
  }

  return (
    <form className="booking-form" onSubmit={handleSubmit}>
      <h3>Место {seat.label}, {seat.price} ₸</h3>

      <label>
        Имя и фамилия
        <input value={fullName} onChange={(e) => setFullName(e.target.value)} required />
      </label>

      <label>
        Email
        <input type="email" value={email} onChange={(e) => setEmail(e.target.value)} required />
      </label>

      <label>
        Телефон
        <input value={phone} onChange={(e) => setPhone(e.target.value)} placeholder="+7 700 000 00 00" />
      </label>

      {error && <p className="error">{error}</p>}

      <button className="primary-button" type="submit" disabled={sending}>
        {sending ? "Бронируем..." : "Забронировать"}
      </button>
    </form>
  );
}

export default BookingForm;
