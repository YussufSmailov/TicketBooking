import { useEffect, useRef, useState } from "react";
import SeatRows from "./SeatRows";
import SeatLegend from "./SeatLegend";
import ZoneTooltip from "./ZoneTooltip";
import { priceColor, summarizeZones } from "../seats";
import { formatPrice } from "../format";

function ZoneMap({ viewBox, shapes, background, foreground, seats, selectedSeat, onSelect, zoneWord }) {
  const [openZone, setOpenZone] = useState(null);
  const [hoveredZone, setHoveredZone] = useState(null);
  const [tooltipPosition, setTooltipPosition] = useState({ x: 0, y: 0 });
  const scrollRef = useRef(null);

  useEffect(() => {
    const box = scrollRef.current;
    if (box) {
      box.scrollLeft = (box.scrollWidth - box.clientWidth) / 2;
    }
  }, [openZone]);

  const zones = summarizeZones(seats);
  const prices = zones.map((zone) => zone.minPrice);
  const minPrice = Math.min(...prices);
  const maxPrice = Math.max(...prices);

  function findZone(name) {
    return zones.find((zone) => zone.name === name);
  }

  function moveTooltip(event) {
    const box = event.currentTarget.getBoundingClientRect();
    setTooltipPosition({ x: event.clientX - box.left + 14, y: event.clientY - box.top + 14 });
  }

  function openIfFree(zone) {
    if (zone && zone.freeCount > 0) {
      setOpenZone(zone.name);
      setHoveredZone(null);
    }
  }

  if (openZone) {
    const zoneSeats = seats.filter((seat) => seat.zone === openZone);
    return (
      <div className="scheme-box">
        <div className="zone-header">
          <button type="button" className="back-button" onClick={() => setOpenZone(null)}>Вернуться к схеме</button>
          <h3>{zoneWord} {openZone}</h3>
        </div>
        <p className="scheme-hint">Схему можно листать вбок →</p>
        <div className="scheme-scroll" ref={scrollRef}>
          <SeatRows seats={zoneSeats} selectedSeat={selectedSeat} onSelect={onSelect} withAisles={false} />
        </div>
        <SeatLegend />
      </div>
    );
  }

  return (
    <div className="scheme-box">
      <p className="scheme-hint">Схему можно листать вбок →</p>
      <div className="scheme-scroll" ref={scrollRef}>
        <div className="zone-map" onMouseMove={moveTooltip} onMouseLeave={() => setHoveredZone(null)}>
          <svg viewBox={viewBox} className="zone-svg" role="group" aria-label={`Схема: выберите ${zoneWord.toLowerCase()}`}>
            {background}
            {shapes.map((shape) => {
              const zone = findZone(shape.name);
              if (!zone) {
                return null;
              }
              const soldOut = zone.freeCount === 0;
              const color = soldOut ? "var(--taken)" : priceColor(zone.minPrice, minPrice, maxPrice);
              return (
                <g
                  key={shape.name}
                  className={soldOut ? "zone-shape zone-shape-sold" : "zone-shape"}
                  tabIndex={soldOut ? -1 : 0}
                  role="button"
                  aria-label={`${zoneWord} ${zone.name}, от ${formatPrice(zone.minPrice)}, свободно ${zone.freeCount}`}
                  onClick={() => openIfFree(zone)}
                  onKeyDown={(event) => {
                    if (event.key === "Enter" || event.key === " ") {
                      event.preventDefault();
                      openIfFree(zone);
                    }
                  }}
                  onMouseEnter={() => setHoveredZone(zone)}
                  onFocus={() => setHoveredZone(zone)}
                  onBlur={() => setHoveredZone(null)}
                >
                  <path d={shape.path} fill={color} />
                  <text
                    x={shape.labelX}
                    y={shape.labelY}
                    textAnchor="middle"
                    className="zone-label"
                    transform={shape.labelRotation ? `rotate(${shape.labelRotation} ${shape.labelX} ${shape.labelY - 5})` : undefined}
                  >
                    {shape.name}
                  </text>
                </g>
              );
            })}
            {foreground}
          </svg>
          <ZoneTooltip zone={hoveredZone} position={tooltipPosition} />
        </div>
      </div>

      <ul className="zone-list">
        {zones.map((zone) => (
          <li key={zone.name}>
            <button
              type="button"
              className="zone-button"
              disabled={zone.freeCount === 0}
              onClick={() => openIfFree(zone)}
            >
              <i className="zone-color" style={{ background: priceColor(zone.minPrice, minPrice, maxPrice) }}></i>
              <b>{zone.name}</b>
              <span>от {formatPrice(zone.minPrice)}</span>
              <span className="muted">{zone.freeCount > 0 ? `свободно ${zone.freeCount}` : "мест нет"}</span>
            </button>
          </li>
        ))}
      </ul>
    </div>
  );
}

export default ZoneMap;
