import { useEffect, useRef, useState } from "react";
import CardFields from "./CardFields";
import Countdown from "./Countdown";
import { getMyCards, payBookings } from "../api";
import { cardForServer, checkCard, emptyCard } from "../cards";
import { formatPrice, plural } from "../format";

function serverFieldErrors(fieldErrors) {
  const result = {};
  Object.keys(fieldErrors || {}).forEach((key) => {
    result[key.replace("newCard.", "")] = fieldErrors[key];
  });
  return result;
}

function PaymentModal({ type, bookingIds, lines, total, onFinished, onClose }) {
  const [cards, setCards] = useState([]);
  const [selectedCardId, setSelectedCardId] = useState("new");
  const [card, setCard] = useState(emptyCard());
  const [saveCard, setSaveCard] = useState(false);
  const [errors, setErrors] = useState({});
  const [error, setError] = useState("");
  const [paying, setPaying] = useState(false);
  const [timeIsOver, setTimeIsOver] = useState(false);
  const dialogRef = useRef(null);

  useEffect(() => {
    getMyCards()
      .then((list) => {
        setCards(list);
        if (list.length > 0) {
          setSelectedCardId(String(list[0].cardId));
        }
      })
      .catch(() => setCards([]));
    if (dialogRef.current) {
      dialogRef.current.focus();
    }
  }, []);

  useEffect(() => {
    function closeOnEscape(event) {
      if (event.key === "Escape") {
        onClose();
      }
    }
    document.addEventListener("keydown", closeOnEscape);
    return () => document.removeEventListener("keydown", closeOnEscape);
  }, [onClose]);

  async function handlePay(event) {
    event.preventDefault();
    setError("");
    setErrors({});

    let payment;
    if (selectedCardId === "new") {
      const cardErrors = checkCard(card);
      if (Object.keys(cardErrors).length > 0) {
        setErrors(cardErrors);
        return;
      }
      payment = { newCard: cardForServer(card), saveCard: saveCard };
    } else {
      payment = { cardId: Number(selectedCardId), saveCard: false };
    }

    setPaying(true);
    try {
      const result = await payBookings(type, bookingIds, payment);
      onFinished(result);
    } catch (e) {
      setError(e.message);
      setErrors(serverFieldErrors(e.fieldErrors));
    } finally {
      setPaying(false);
    }
  }

  return (
    <div className="modal-backdrop">
      <div
        className="modal"
        role="dialog"
        aria-modal="true"
        aria-labelledby="payment-title"
        tabIndex={-1}
        ref={dialogRef}
      >
        <div className="modal-header">
          <h3 id="payment-title">{lines.length === 1 ? "Оплата билета" : `Оплата ${lines.length} ${plural(lines.length, ["билета", "билетов", "билетов"])}`}</h3>
          <button type="button" className="icon-button" onClick={onClose} aria-label="Закрыть">×</button>
        </div>

        <ul className="modal-lines">
          {lines.map((line) => <li key={line}>{line}</li>)}
        </ul>
        <p className="modal-price">{formatPrice(total)}</p>
        <p className="muted small">
          {lines.length === 1 ? "Место закреплено" : "Места закреплены"} за вами ещё{" "}
          <Countdown seconds={10 * 60} onFinish={() => setTimeIsOver(true)} />. Потом бронь отменится.
        </p>

        {timeIsOver ? (
          <p className="error">Время на оплату вышло. Выберите места заново.</p>
        ) : (
          <form onSubmit={handlePay} className="payment-form">
            <fieldset className="card-choice">
              <legend>Чем платить</legend>
              {cards.map((savedCard) => (
                <label key={savedCard.cardId} className="radio-row">
                  <input
                    type="radio"
                    name="card"
                    value={savedCard.cardId}
                    checked={selectedCardId === String(savedCard.cardId)}
                    onChange={(e) => setSelectedCardId(e.target.value)}
                  />
                  {savedCard.title}
                  <span className="muted small">до {String(savedCard.expMonth).padStart(2, "0")}/{savedCard.expYear}</span>
                </label>
              ))}
              <label className="radio-row">
                <input
                  type="radio"
                  name="card"
                  value="new"
                  checked={selectedCardId === "new"}
                  onChange={(e) => setSelectedCardId(e.target.value)}
                />
                Новая карта
              </label>
            </fieldset>

            {selectedCardId === "new" && (
              <>
                <CardFields card={card} onChange={setCard} errors={errors} />
                <label className="checkbox-row">
                  <input type="checkbox" checked={saveCard} onChange={(e) => setSaveCard(e.target.checked)} />
                  Сохранить карту
                </label>
              </>
            )}

            {error && <p className="error">{error}</p>}
            <button className="primary-button" type="submit" disabled={paying}>
              {paying ? "Оплачиваем..." : `Оплатить ${formatPrice(total)}`}
            </button>
            <p className="muted small">Это учебный проект: деньги не списываются, номер карты и CVV не сохраняются.</p>
          </form>
        )}
      </div>
    </div>
  );
}

export default PaymentModal;
