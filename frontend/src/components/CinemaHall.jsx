import { useEffect, useRef } from "react";
import SeatRows from "./SeatRows";
import SeatLegend from "./SeatLegend";

function CinemaHall({ seats, selectedSeat, onSelect }) {
  const scrollRef = useRef(null);

  useEffect(() => {
    const box = scrollRef.current;
    if (box) {
      box.scrollLeft = (box.scrollWidth - box.clientWidth) / 2;
    }
  }, []);

  return (
    <div className="scheme-box">
      <p className="scheme-hint">Схему можно листать вбок →</p>
      <div className="scheme-scroll" ref={scrollRef}>
        <div className="cinema-hall">
          <svg className="screen-arc" viewBox="0 0 600 60" role="img" aria-label="Экран">
            <path d="M 40 50 Q 300 0 560 50" fill="none" stroke="currentColor" strokeWidth="6" strokeLinecap="round" />
            <text x="300" y="56" textAnchor="middle" fontSize="14" fill="currentColor" letterSpacing="6">ЭКРАН</text>
          </svg>
          <SeatRows seats={seats} selectedSeat={selectedSeat} onSelect={onSelect} withAisles={true} />
        </div>
      </div>
      <SeatLegend />
    </div>
  );
}

export default CinemaHall;
