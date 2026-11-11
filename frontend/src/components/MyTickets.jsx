import { useState } from "react";
import { Link } from "react-router-dom";
import StatusMessage from "./StatusMessage";
import PaymentModal from "./PaymentModal";
import { cancelMyBooking, getMyBookings } from "../api";
import { useData } from "../useData";
import { formatDate, formatPrice, formatTime } from "../format";
import { seatLabel } from "../seats";

const typeNames = { cinema: "Кино", concert: "Концерт", sport: "Матч КПЛ" };

const statusNames = {
  Pending: "Ждёт оплаты",
  Confirmed: "Оплачено",
  Cancelled: "Отменено"
};

function eventLink(ticket) {
  if (ticket.type === "cinema") return `/sessions/${ticket.eventId}`;
  if (ticket.type === "concert") return `/concerts/${ticket.eventId}`;
  return `/matches/${ticket.eventId}`;
}

function MyTickets() {
  const { data, loading, error, reload } = useData(getMyBookings, []);
  const [actionError, setActionError] = useState("");
  const [message, setMessage] = useState("");
  const [payingTicket, setPayingTicket] = useState(null);

  const tickets = data || [];

  async function handleCancel(ticket) {
    if (!window.confirm(`Отменить бронь «${ticket.eventTitle}»?`)) {
      return;
    }
    setActionError("");
    setMessage("");
    try {
      await cancelMyBooking(ticket.type, ticket.bookingId);
      setMessage("Бронь отменена, место освобождено.");
      reload();
    } catch (e) {
      setActionError(e.message);
    }
  }

  function handlePaid(result) {
    setPayingTicket(null);
    if (result.paymentStatus === "Paid") {
      setMessage(result.message);
      setActionError("");
    } else {
      setActionError(result.message);
      setMessage("");
    }
    reload();
  }

  return (
    <div className="account-section">
      {message && <p className="success" role="status">{message}</p>}
      {actionError && <p className="error-box" role="alert">{actionError}</p>}
      <StatusMessage
        loading={loading}
        error={error}
        empty={!loading && tickets.length === 0}
        emptyText="Билетов пока нет. Выберите событие на главной."
      />
      <ul className="ticket-list">
        {tickets.map((ticket) => (
          <li key={`${ticket.type}-${ticket.bookingId}`} className={`ticket ticket-${ticket.status.toLowerCase()}`}>
            <div className="ticket-main">
              <span className="ticket-type">{typeNames[ticket.type]}</span>
              <Link to={eventLink(ticket)} className="ticket-title">{ticket.eventTitle}</Link>
              <span className="muted">
                {formatDate(ticket.eventDate)} в {formatTime(ticket.eventTime)}, {ticket.place}
              </span>
              <span>{seatLabel(ticket.zone, ticket.seatNumber)} · {formatPrice(ticket.price)}</span>
            </div>
            <div className="ticket-side">
              <span className={`status-badge status-${ticket.status.toLowerCase()}`}>{statusNames[ticket.status]}</span>
              {ticket.ticketNumber && (
                <span className="ticket-number" aria-label="Номер электронного билета">№ {ticket.ticketNumber}</span>
              )}
              <div className="admin-buttons">
                {ticket.status === "Pending" && (
                  <button className="primary-button" onClick={() => setPayingTicket(ticket)}>Оплатить</button>
                )}
                {ticket.status !== "Cancelled" && (
                  <button className="danger-button" onClick={() => handleCancel(ticket)}>Отменить</button>
                )}
              </div>
            </div>
          </li>
        ))}
      </ul>
      {payingTicket && (
        <PaymentModal
          type={payingTicket.type}
          booking={{ bookingId: payingTicket.bookingId }}
          seat={{ label: `${payingTicket.eventTitle}, ${seatLabel(payingTicket.zone, payingTicket.seatNumber)}`, price: payingTicket.price }}
          onFinished={handlePaid}
          onClose={() => setPayingTicket(null)}
        />
      )}
    </div>
  );
}

export default MyTickets;
