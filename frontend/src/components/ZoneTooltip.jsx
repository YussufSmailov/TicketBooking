import { formatPrice } from "../format";

function ZoneTooltip({ zone, position }) {
  if (!zone) {
    return null;
  }
  return (
    <div className="zone-tooltip" style={{ left: position.x, top: position.y }} role="status">
      <b>{zone.name}</b>
      <span>от {formatPrice(zone.minPrice)}</span>
      <span>{zone.freeCount > 0 ? `Свободно мест: ${zone.freeCount}` : "Мест нет"}</span>
    </div>
  );
}

export default ZoneTooltip;
