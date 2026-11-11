import { useState } from "react";
import CardFields from "./CardFields";
import StatusMessage from "./StatusMessage";
import { addMyCard, deleteMyCard, getMyCards } from "../api";
import { cardForServer, checkCard, emptyCard } from "../cards";
import { useData } from "../useData";

function MyCards() {
  const { data, loading, error, reload } = useData(getMyCards, []);
  const [showForm, setShowForm] = useState(false);
  const [card, setCard] = useState(emptyCard());
  const [errors, setErrors] = useState({});
  const [formError, setFormError] = useState("");
  const [saving, setSaving] = useState(false);

  const cards = data || [];

  async function handleAdd(event) {
    event.preventDefault();
    setFormError("");
    const cardErrors = checkCard(card);
    setErrors(cardErrors);
    if (Object.keys(cardErrors).length > 0) {
      return;
    }
    setSaving(true);
    try {
      await addMyCard(cardForServer(card));
      setCard(emptyCard());
      setShowForm(false);
      reload();
    } catch (e) {
      setFormError(e.message);
      setErrors(e.fieldErrors || {});
    } finally {
      setSaving(false);
    }
  }

  async function handleDelete(cardId) {
    setFormError("");
    try {
      await deleteMyCard(cardId);
      reload();
    } catch (e) {
      setFormError(e.message);
    }
  }

  return (
    <div className="account-section">
      <StatusMessage loading={loading} error={error} empty={!loading && cards.length === 0} emptyText="Сохранённых карт нет." />
      <ul className="saved-cards">
        {cards.map((savedCard) => (
          <li key={savedCard.cardId} className="saved-card">
            <div>
              <b>{savedCard.title}</b>
              <span className="muted small">
                {savedCard.holderName}, до {String(savedCard.expMonth).padStart(2, "0")}/{savedCard.expYear}
              </span>
            </div>
            <button className="danger-button" onClick={() => handleDelete(savedCard.cardId)}>Удалить</button>
          </li>
        ))}
      </ul>

      {formError && <p className="error">{formError}</p>}

      {showForm ? (
        <form className="booking-form" onSubmit={handleAdd}>
          <h3>Новая карта</h3>
          <CardFields card={card} onChange={setCard} errors={errors} />
          <div className="admin-buttons">
            <button className="primary-button" type="submit" disabled={saving}>{saving ? "Сохраняем..." : "Сохранить карту"}</button>
            <button className="secondary-button" type="button" onClick={() => setShowForm(false)}>Отмена</button>
          </div>
        </form>
      ) : (
        <button className="primary-button" onClick={() => setShowForm(true)}>Добавить карту</button>
      )}
    </div>
  );
}

export default MyCards;
