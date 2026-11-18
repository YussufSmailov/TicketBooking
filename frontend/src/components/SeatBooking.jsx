import { useEffect, useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import BookingForm from "./BookingForm";
import CinemaHall from "./CinemaHall";
import StadiumMap from "./StadiumMap";
import ConcertHall from "./ConcertHall";
import StatusMessage from "./StatusMessage";
import { useAuth } from "../AuthContext";
import { useData } from "../useData";
import { getSeats } from "../api";
import { isFree } from "../seats";
import { plural } from "../format";

export const MAX_SEATS = 6;

function SeatBooking({ category, eventId }) {
  const { user } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [selectedSeats, setSelectedSeats] = useState([]);
  const [successMessage, setSuccessMessage] = useState("");
  const [errorMessage, setErrorMessage] = useState("");
  const [limitMessage, setLimitMessage] = useState("");
  const [inCheckout, setInCheckout] = useState(false);
  const seats = useData(() => getSeats(category, eventId), [category, eventId]);

  useEffect(() => {
    if (!seats.data || inCheckout) {
      return;
    }
    setSelectedSeats((current) => current.filter((picked) => {
      const fresh = seats.data.find((seat) => seat.id === picked.id);
      return fresh && isFree(fresh);
    }));
  }, [seats.data, inCheckout]);

  function toggleSeat(seat) {
    setSuccessMessage("");
    setErrorMessage("");
    setSelectedSeats((current) => {
      if (current.some((picked) => picked.id === seat.id)) {
        setLimitMessage("");
        return current.filter((picked) => picked.id !== seat.id);
      }
      if (current.length >= MAX_SEATS) {
        setLimitMessage(`В один заказ можно добавить не больше ${MAX_SEATS} мест.`);
        return current;
      }
      setLimitMessage("");
      return [...current, seat];
    });
  }

  function handleFinished(result, seatCount) {
    setSelectedSeats([]);
    setInCheckout(false);
    seats.reload();
    if (result === null) {
      setSuccessMessage("");
      setErrorMessage(`Оплата не завершена. ${seatCount === 1 ? "Место держится" : "Места держатся"} за вами 10 минут — оплатить можно в личном кабинете.`);
      return;
    }
    if (result.paymentStatus === "Paid") {
      setErrorMessage("");
      const numbers = result.ticketNumbers.join(", ");
      setSuccessMessage(`Оплачено! ${result.ticketNumbers.length} ${plural(result.ticketNumbers.length, ["билет", "билета", "билетов"])}: № ${numbers}. Они в личном кабинете.`);
    } else {
      setSuccessMessage("");
      setErrorMessage(result.message);
    }
  }

  function goToLogin() {
    navigate(`/login?next=${encodeURIComponent(location.pathname)}`);
  }

  if (seats.loading && !seats.data) {
    return <StatusMessage loading={true} skeleton="page" />;
  }
  if (seats.error) {
    return <StatusMessage error={seats.error} onRetry={seats.reload} />;
  }

  const seatList = seats.data || [];
  if (seatList.length === 0) {
    return <StatusMessage empty={true} emptyText="Места для этого события ещё не добавлены." />;
  }

  return (
    <>
      {successMessage && <p className="success" role="status">{successMessage}</p>}
      {errorMessage && <p className="error-box" role="alert">{errorMessage}</p>}
      <p className="muted small">Нажмите на места, чтобы выбрать их — можно до {MAX_SEATS} мест в одном заказе. Повторное нажатие снимает выбор.</p>
      {category === "cinema" && <CinemaHall seats={seatList} selectedSeats={selectedSeats} onToggle={toggleSeat} />}
      {category === "sport" && <StadiumMap seats={seatList} selectedSeats={selectedSeats} onToggle={toggleSeat} />}
      {category === "concert" && <ConcertHall seats={seatList} selectedSeats={selectedSeats} onToggle={toggleSeat} />}
      {limitMessage && <p className="error" role="alert">{limitMessage}</p>}
      {selectedSeats.length > 0 && (
        <BookingForm
          category={category}
          seats={selectedSeats}
          user={user}
          onRemove={toggleSeat}
          onCheckout={setInCheckout}
          onFinished={handleFinished}
          onNeedLogin={goToLogin}
          onSeatsChanged={seats.reload}
        />
      )}
    </>
  );
}

export default SeatBooking;
