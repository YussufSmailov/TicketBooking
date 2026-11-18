import { groupSeatsByRow, isAisleAfter, isFree } from "../seats";
import { formatPrice } from "../format";

function SeatRows({ seats, selectedSeats, onToggle, withAisles }) {
  const selectedIds = new Set(selectedSeats.map((seat) => seat.id));
  const rows = groupSeatsByRow(seats);

  return (
    <div className="seat-rows">
      {rows.map((row) => {
        const rowLength = row.seats.length;
        const seatsRightToLeft = [...row.seats].reverse();
        const items = [];

        seatsRightToLeft.forEach((seat) => {
          if (withAisles && seat.number < rowLength && isAisleAfter(seat.number, rowLength)) {
            items.push(<span key={`aisle-${seat.number}`} className="seat-aisle" aria-hidden="true"></span>);
          }

          const free = isFree(seat);
          const selected = selectedIds.has(seat.id);
          let className = "seat-square";
          if (!free && !selected) className = "seat-square seat-square-taken";
          if (selected) className = "seat-square seat-square-selected";

          items.push(
            <button
              key={seat.id}
              type="button"
              className={className}
              disabled={!free && !selected}
              aria-pressed={selected}
              aria-label={free || selected ? `${seat.label}, ${formatPrice(seat.price)}` : `${seat.label}, занято`}
              title={free || selected ? `${seat.label}, ${formatPrice(seat.price)}` : "Место занято"}
              onClick={() => onToggle(seat)}
            >
              {free || selected ? seat.number : ""}
            </button>
          );
        });

        return (
          <div key={row.rowNumber} className="seat-row-line">
            <span className="row-label row-label-left" aria-hidden="true">{row.rowNumber}</span>
            <div className="seat-row-seats">{items}</div>
            <span className="row-label">Ряд {row.rowNumber}</span>
          </div>
        );
      })}
    </div>
  );
}

export default SeatRows;
