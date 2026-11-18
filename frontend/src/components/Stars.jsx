import { formatRating, plural } from "../format";

export function Stars({ value, size }) {
  const percent = Math.max(0, Math.min(100, (Number(value) / 5) * 100));
  return (
    <span className={`stars ${size === "large" ? "stars-large" : ""}`} aria-hidden="true">
      <span className="stars-empty">★★★★★</span>
      <span className="stars-full" style={{ width: `${percent}%` }}>★★★★★</span>
    </span>
  );
}

export function RatingBadge({ rating, count, href }) {
  if (rating === null || rating === undefined || !count) {
    return <span className="rating-badge rating-badge-empty">Нет оценок</span>;
  }
  const text = `${formatRating(rating)} · ${count} ${plural(count, ["оценка", "оценки", "оценок"])}`;
  const content = (
    <>
      <span className="rating-star" aria-hidden="true">★</span>
      {text}
    </>
  );
  const label = `Рейтинг ${formatRating(rating)} из 5, ${count} ${plural(count, ["оценка", "оценки", "оценок"])}`;
  if (href) {
    return <a className="rating-badge" href={href} aria-label={label}>{content}</a>;
  }
  return <span className="rating-badge" aria-label={label}>{content}</span>;
}

export function StarInput({ value, onChange, disabled }) {
  return (
    <fieldset className="star-input" disabled={disabled}>
      <legend>Ваша оценка</legend>
      <div className="star-input-row">
        {[1, 2, 3, 4, 5].map((star) => (
          <label key={star} className={star <= value ? "star-option star-option-on" : "star-option"}>
            <input
              type="radio"
              name="rating"
              value={star}
              checked={value === star}
              onChange={() => onChange(star)}
            />
            <span aria-hidden="true">★</span>
            <span className="visually-hidden">{star} из 5</span>
          </label>
        ))}
      </div>
    </fieldset>
  );
}
