const months = [
  "января", "февраля", "марта", "апреля", "мая", "июня",
  "июля", "августа", "сентября", "октября", "ноября", "декабря"
];

const weekdays = ["вс", "пн", "вт", "ср", "чт", "пт", "сб"];

export function formatDate(dateText) {
  if (!dateText) {
    return "";
  }
  const [year, month, day] = dateText.split("-").map(Number);
  const date = new Date(year, month - 1, day);
  return `${day} ${months[month - 1]}, ${weekdays[date.getDay()]}`;
}

export function formatTime(timeText) {
  return timeText ? timeText.slice(0, 5) : "";
}

export function formatPrice(price) {
  return `${Number(price).toLocaleString("ru-RU")} ₸`;
}

export function plural(count, forms) {
  const n = Math.abs(count) % 100;
  const last = n % 10;
  if (n > 10 && n < 20) return forms[2];
  if (last > 1 && last < 5) return forms[1];
  if (last === 1) return forms[0];
  return forms[2];
}

export function formatRating(rating) {
  return Number(rating).toLocaleString("ru-RU", { minimumFractionDigits: 1, maximumFractionDigits: 1 });
}

export function formatDuration(minutes) {
  if (!minutes) return "";
  const hours = Math.floor(minutes / 60);
  const rest = minutes % 60;
  if (hours === 0) return `${rest} мин`;
  return rest === 0 ? `${hours} ч` : `${hours} ч ${rest} мин`;
}
