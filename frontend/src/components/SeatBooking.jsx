import { useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import BookingForm from "./BookingForm";
import CinemaHall from "./CinemaHall";
import StadiumMap from "./StadiumMap";
import ConcertHall from "./ConcertHall";
import StatusMessage from "./StatusMessage";
import { useAuth } from "../AuthContext";
import { useData } from "../useData";
import { getSeats } from "../api";

function SeatBooking({ category, eventId }) {
  const { user } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [selectedSeat, setSelectedSeat] = useState(null);
  const [successMessage, setSuccessMessage] = useState("");
  const seats = useData(() => getSeats(category, eventId), [category, eventId]);

  const [errorMessage, setErrorMessage] = useState("");

  function handleFinished(result, seat) {
    setSelectedSeat(null);
    seats.reload();
    if (result === null) {
      setSuccessMessage("");
      setErrorMessage("Оплата не завершена. Место держится за вами 10 минут — оплатить можно в личном кабинете.");
      return;
    }
    if (result.paymentStatus === "Paid") {
      setErrorMessage("");
      setSuccessMessage(`Оплачено! Электронный билет № ${result.ticketNumber}: ${seat.label}. Он в личном кабинете.`);
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
      {category === "cinema" && <CinemaHall seats={seatList} selectedSeat={selectedSeat} onSelect={setSelectedSeat} />}
      {category === "sport" && <StadiumMap seats={seatList} selectedSeat={selectedSeat} onSelect={setSelectedSeat} />}
      {category === "concert" && <ConcertHall seats={seatList} selectedSeat={selectedSeat} onSelect={setSelectedSeat} />}
      {selectedSeat && (
        <BookingForm
          category={category}
          seat={selectedSeat}
          user={user}
          onFinished={handleFinished}
          onNeedLogin={goToLogin}
          onSeatsChanged={seats.reload}
        />
      )}
    </>
  );
}

export default SeatBooking;
