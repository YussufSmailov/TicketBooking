import ZoneMap from "./ZoneMap";

const concertShapes = [
  { name: "VIP", path: "M 180 100 L 420 100 L 420 165 L 180 165 Z", labelX: 300, labelY: 138 },
  { name: "Fan zone", path: "M 120 185 L 480 185 L 480 280 L 120 280 Z", labelX: 300, labelY: 238 },
  { name: "Tribune", path: "M 40 305 Q 300 335 560 305 L 525 400 Q 300 425 75 400 Z", labelX: 300, labelY: 370 }
];

const stage = (
  <g aria-hidden="true">
    <rect x="150" y="20" width="300" height="56" rx="10" className="stage-shape" />
    <text x="300" y="55" textAnchor="middle" className="stage-label">СЦЕНА</text>
  </g>
);

function ConcertHall({ seats, selectedSeats, onToggle }) {
  return (
    <ZoneMap
      viewBox="0 0 600 430"
      shapes={concertShapes}
      background={stage}
      seats={seats}
      selectedSeats={selectedSeats}
      onToggle={onToggle}
      zoneWord="Зона"
    />
  );
}

export default ConcertHall;
