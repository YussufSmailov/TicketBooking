import { useEffect, useState } from "react";
import { getSeats } from "../api";

function SeatPicker({ category, eventId, selectedSeat, onSelect }) {
  const [seats, setSeats] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    setLoading(true);
    setError("");
    getSeats(category, eventId)
      .then((data) => setSeats(data))
      .catch(() => setError("Не удалось загрузить места."))
      .finally(() => setLoading(false));
  }, [category, eventId]);

  if (loading) {
    return <p className="muted">Загружаем места...</p>;
  }

  if (error) {
    return <p className="error">{error}</p>;
  }

  const groups = [];
  seats.forEach((seat) => {
    if (!groups.includes(seat.group)) {
      groups.push(seat.group);
    }
  });

  return (
    <div className="seat-picker">
      {category === "cinema" && <div className="screen">Экран</div>}

      {groups.map((group) => {
        const groupSeats = seats.filter((seat) => seat.group === group);
        return (
          <div key={group} className="seat-group">
            <div className="seat-group-title">
              {group}, {groupSeats[0].price} ₸
            </div>
            <div className="seat-row">
              {groupSeats.map((seat) => {
                const isFree = seat.status === "Available";
                const isSelected = selectedSeat !== null && selectedSeat.id === seat.id;
                let className = "seat";
                if (!isFree) className = "seat seat-taken";
                if (isSelected) className = "seat seat-selected";

                return (
                  <button
                    key={seat.id}
                    className={className}
                    disabled={!isFree}
                    onClick={() => onSelect(seat)}
                    title={isFree ? `${seat.label}, ${seat.price} ₸` : "Место занято"}
                  >
                    {seat.label}
                  </button>
                );
              })}
            </div>
          </div>
        );
      })}

      <div className="legend">
        <span><i className="dot dot-free"></i>Свободно</span>
        <span><i className="dot dot-selected"></i>Выбрано</span>
        <span><i className="dot dot-taken"></i>Занято</span>
      </div>
    </div>
  );
}

export default SeatPicker;
