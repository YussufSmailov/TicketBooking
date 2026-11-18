import { useEffect, useState } from "react";
import { Link, useLocation } from "react-router-dom";
import { useAuth } from "../AuthContext";
import { deleteReview, getReviews, saveReview } from "../api";
import { useData } from "../useData";
import { formatRating, plural } from "../format";
import { Stars, StarInput } from "./Stars";
import StatusMessage from "./StatusMessage";

const ratingWords = ["", "Плохо", "Так себе", "Нормально", "Хорошо", "Отлично"];

function formatReviewDate(dateTime) {
  const date = new Date(dateTime);
  return date.toLocaleDateString("ru-RU", { day: "numeric", month: "long", year: "numeric" });
}

function ReviewForm({ eventType, eventId, mine, onSaved }) {
  const [rating, setRating] = useState(mine ? mine.rating : 0);
  const [comment, setComment] = useState(mine && mine.comment ? mine.comment : "");
  const [error, setError] = useState("");
  const [sending, setSending] = useState(false);

  useEffect(() => {
    setRating(mine ? mine.rating : 0);
    setComment(mine && mine.comment ? mine.comment : "");
  }, [mine]);

  async function handleSubmit(event) {
    event.preventDefault();
    if (rating === 0) {
      setError("Поставьте оценку от 1 до 5 звёзд");
      return;
    }
    setError("");
    setSending(true);
    try {
      await saveReview(eventType, eventId, { rating: rating, comment: comment });
      onSaved(mine ? "Отзыв обновлён." : "Спасибо! Ваш отзыв опубликован.");
    } catch (e) {
      setError(e.fieldErrors && e.fieldErrors.comment ? e.fieldErrors.comment : e.message);
    } finally {
      setSending(false);
    }
  }

  return (
    <form className="review-form" onSubmit={handleSubmit}>
      <h4>{mine ? "Ваш отзыв" : "Оцените событие"}</h4>
      <div className="review-form-rating">
        <StarInput value={rating} onChange={setRating} disabled={sending} />
        <span className="muted" aria-live="polite">{ratingWords[rating]}</span>
      </div>
      <label>
        Комментарий <span className="muted small">(необязательно)</span>
        <textarea
          value={comment}
          onChange={(e) => setComment(e.target.value)}
          maxLength={1000}
          rows={3}
          placeholder="Что понравилось, а что нет?"
        />
        <span className="muted small">{comment.length} / 1000</span>
      </label>
      {error && <p className="error" role="alert">{error}</p>}
      <div>
        <button className="primary-button" type="submit" disabled={sending}>
          {sending ? "Сохраняем..." : mine ? "Обновить отзыв" : "Опубликовать"}
        </button>
      </div>
    </form>
  );
}

function Reviews({ eventType, eventId, onSummary }) {
  const { user, isAdmin } = useAuth();
  const location = useLocation();
  const { data, loading, error, reload } = useData(() => getReviews(eventType, eventId), [eventType, eventId, user ? user.email : ""]);
  const [message, setMessage] = useState("");
  const [actionError, setActionError] = useState("");

  useEffect(() => {
    if (data && onSummary) {
      onSummary(data);
    }
  }, [data]);

  function handleSaved(text) {
    setMessage(text);
    setActionError("");
    reload();
  }

  async function handleDelete(review) {
    const question = review.mine ? "Удалить ваш отзыв?" : `Удалить отзыв пользователя ${review.userName}?`;
    if (!window.confirm(question)) {
      return;
    }
    try {
      await deleteReview(review.reviewId);
      setMessage("Отзыв удалён.");
      setActionError("");
      reload();
    } catch (e) {
      setActionError(e.message);
    }
  }

  const summary = data;
  const mine = summary ? summary.reviews.find((review) => review.mine) : null;
  const maxBar = summary ? Math.max(1, ...summary.distribution) : 1;

  return (
    <section className="reviews" id="reviews" aria-labelledby="reviews-title">
      <h3 id="reviews-title" className="section-title">Отзывы и рейтинг</h3>
      {loading && !summary && <StatusMessage loading={true} />}
      {error && <StatusMessage error={error} onRetry={reload} />}
      {summary && (
        <>
          <div className="reviews-summary">
            <div className="rating-big">
              {summary.count > 0 ? (
                <>
                  <b>{formatRating(summary.average)}</b>
                  <Stars value={summary.average} size="large" />
                  <span className="muted">{summary.count} {plural(summary.count, ["оценка", "оценки", "оценок"])}</span>
                </>
              ) : (
                <span className="muted">Оценок пока нет — станьте первым.</span>
              )}
            </div>
            {summary.count > 0 && (
              <ul className="rating-bars" aria-label="Распределение оценок">
                {[5, 4, 3, 2, 1].map((star) => {
                  const count = summary.distribution[star - 1];
                  return (
                    <li key={star}>
                      <span>{star} ★</span>
                      <span className="rating-bar"><span style={{ width: `${(count / maxBar) * 100}%` }}></span></span>
                      <span className="muted small">{count}</span>
                    </li>
                  );
                })}
              </ul>
            )}
          </div>

          {message && <p className="success" role="status">{message}</p>}
          {actionError && <p className="error-box" role="alert">{actionError}</p>}

          {user ? (
            <ReviewForm eventType={eventType} eventId={eventId} mine={mine} onSaved={handleSaved} />
          ) : (
            <p className="review-login muted">
              <Link className="link-button" to={`/login?next=${encodeURIComponent(location.pathname)}`}>Войдите</Link>, чтобы поставить оценку и написать отзыв.
            </p>
          )}

          <ul className="review-list">
            {summary.reviews.map((review) => (
              <li key={review.reviewId} className={review.mine ? "review review-mine" : "review"}>
                <div className="review-head">
                  <b>{review.userName}{review.mine ? " (вы)" : ""}</b>
                  <span className="review-stars" aria-label={`Оценка ${review.rating} из 5`}><Stars value={review.rating} /></span>
                  <span className="muted small">{formatReviewDate(review.updatedAt)}</span>
                </div>
                {review.comment && <p>{review.comment}</p>}
                {(review.mine || isAdmin) && (
                  <button type="button" className="link-button danger-link" onClick={() => handleDelete(review)}>Удалить</button>
                )}
              </li>
            ))}
          </ul>
        </>
      )}
    </section>
  );
}

export default Reviews;
