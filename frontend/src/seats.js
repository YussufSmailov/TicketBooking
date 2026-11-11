export function parseSeatNumber(seatNumber) {
  const parts = String(seatNumber).split("-");
  return {
    row: Number(parts[0]),
    seat: Number(parts[1])
  };
}

export function seatLabel(zone, seatNumber) {
  const place = parseSeatNumber(seatNumber);
  const rowAndSeat = `ряд ${place.row}, место ${place.seat}`;
  if (zone) {
    return `${zone}, ${rowAndSeat}`;
  }
  return `Ряд ${place.row}, место ${place.seat}`;
}

export function groupSeatsByRow(seats) {
  const rows = [];
  seats.forEach((seat) => {
    let row = rows.find((item) => item.rowNumber === seat.row);
    if (!row) {
      row = { rowNumber: seat.row, seats: [] };
      rows.push(row);
    }
    row.seats.push(seat);
  });
  rows.sort((a, b) => a.rowNumber - b.rowNumber);
  rows.forEach((row) => row.seats.sort((a, b) => a.number - b.number));
  return rows;
}

export function isAisleAfter(seatNumber, rowLength) {
  if (rowLength < 14) {
    return false;
  }
  return seatNumber === 4 || seatNumber === rowLength - 4;
}

export function isFree(seat) {
  return seat.status === "Available";
}

export function summarizeZones(seats) {
  const zones = [];
  seats.forEach((seat) => {
    let zone = zones.find((item) => item.name === seat.zone);
    if (!zone) {
      zone = { name: seat.zone, minPrice: Number(seat.price), freeCount: 0, totalCount: 0 };
      zones.push(zone);
    }
    zone.minPrice = Math.min(zone.minPrice, Number(seat.price));
    zone.totalCount = zone.totalCount + 1;
    if (isFree(seat)) {
      zone.freeCount = zone.freeCount + 1;
    }
  });
  return zones;
}

const priceColors = ["#8fd19e", "#6CBB72", "#e8b04a", "#e9803a", "#d6403a"];

export function priceColor(price, minPrice, maxPrice) {
  if (maxPrice === minPrice) {
    return priceColors[1];
  }
  const share = (price - minPrice) / (maxPrice - minPrice);
  const index = Math.min(priceColors.length - 1, Math.floor(share * priceColors.length));
  return priceColors[index];
}
