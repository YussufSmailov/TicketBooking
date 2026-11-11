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
