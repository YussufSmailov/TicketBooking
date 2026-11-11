import FieldError from "./FieldError";
import { detectBrand, formatCardNumber } from "../cards";

function CardFields({ card, onChange, errors }) {
  const currentYear = new Date().getFullYear();
  const years = [];
  for (let year = currentYear; year <= currentYear + 10; year++) {
    years.push(year);
  }
  const months = [];
  for (let month = 1; month <= 12; month++) {
    months.push(month);
  }
  const brand = detectBrand(card.cardNumber);

  function changeField(name, value) {
    onChange({ ...card, [name]: value });
  }

  return (
    <div className="card-fields">
      <label className="card-number-field">
        Номер карты {brand && <span className="card-brand">{brand}</span>}
        <input
          inputMode="numeric"
          autoComplete="cc-number"
          placeholder="0000 0000 0000 0000"
          value={card.cardNumber}
          onChange={(e) => changeField("cardNumber", formatCardNumber(e.target.value))}
          maxLength={23}
          required
        />
        <FieldError message={errors.cardNumber} />
      </label>
      <label>
        Месяц
        <select value={card.expMonth} onChange={(e) => changeField("expMonth", e.target.value)} autoComplete="cc-exp-month">
          {months.map((month) => (
            <option key={month} value={month}>{String(month).padStart(2, "0")}</option>
          ))}
        </select>
        <FieldError message={errors.expMonth} />
      </label>
      <label>
        Год
        <select value={card.expYear} onChange={(e) => changeField("expYear", e.target.value)} autoComplete="cc-exp-year">
          {years.map((year) => (
            <option key={year} value={year}>{year}</option>
          ))}
        </select>
        <FieldError message={errors.expYear} />
      </label>
      <label>
        CVV
        <input
          type="password"
          inputMode="numeric"
          autoComplete="cc-csc"
          placeholder="•••"
          value={card.cvv}
          onChange={(e) => changeField("cvv", e.target.value.replace(/[^0-9]/g, "").slice(0, 4))}
          required
        />
        <FieldError message={errors.cvv} />
      </label>
      <label className="card-holder-field">
        Имя владельца
        <input
          autoComplete="cc-name"
          placeholder="AMINA SEITKALI"
          value={card.holderName}
          onChange={(e) => changeField("holderName", e.target.value.toUpperCase())}
          maxLength={100}
          required
        />
        <FieldError message={errors.holderName} />
      </label>
    </div>
  );
}

export default CardFields;
