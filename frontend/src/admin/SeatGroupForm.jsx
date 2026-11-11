import { useState } from "react";
import FieldError from "../components/FieldError";

function SeatGroupForm({ zoneLabel, zoneOptions, onSubmit }) {
  const [zone, setZone] = useState(zoneOptions ? zoneOptions[0] : "");
  const [rowNumber, setRowNumber] = useState("1");
  const [seatCount, setSeatCount] = useState("10");
  const [price, setPrice] = useState("2500");
  const [error, setError] = useState("");
  const [fieldErrors, setFieldErrors] = useState({});
  const [message, setMessage] = useState("");

  async function handleSubmit(event) {
    event.preventDefault();
    setError("");
    setFieldErrors({});
    setMessage("");

    const row = {
      rowNumber: Number(rowNumber),
      seatCount: Number(seatCount),
      price: Number(price)
    };
    if (zoneLabel) {
      row.zone = zone;
    }

    try {
      const seats = await onSubmit(row);
      setMessage(`Добавлено мест: ${seats.length}`);
    } catch (e) {
      setError(e.message);
      setFieldErrors(e.fieldErrors || {});
    }
  }

  return (
    <form className="admin-form admin-form-inline" onSubmit={handleSubmit}>
      {zoneLabel && (
        <label>
          {zoneLabel}
          {zoneOptions ? (
            <select value={zone} onChange={(e) => setZone(e.target.value)} required>
              {zoneOptions.map((option) => (
                <option key={option} value={option}>{option}</option>
              ))}
            </select>
          ) : (
            <input value={zone} onChange={(e) => setZone(e.target.value)} maxLength={30} required />
          )}
          <FieldError message={fieldErrors.zone} />
        </label>
      )}
      <label>
        Ряд
        <input type="number" min="1" max="50" value={rowNumber} onChange={(e) => setRowNumber(e.target.value)} required />
        <FieldError message={fieldErrors.rowNumber} />
      </label>
      <label>
        Мест в ряду
        <input type="number" min="1" max="60" value={seatCount} onChange={(e) => setSeatCount(e.target.value)} required />
        <FieldError message={fieldErrors.seatCount} />
      </label>
      <label>
        Цена, ₸
        <input type="number" min="1" step="1" value={price} onChange={(e) => setPrice(e.target.value)} required />
        <FieldError message={fieldErrors.price} />
      </label>
      <button className="primary-button" type="submit">Добавить ряд</button>
      {error && <p className="error">{error}</p>}
      {message && <p className="success">{message}</p>}
    </form>
  );
}

export default SeatGroupForm;
