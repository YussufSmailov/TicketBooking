import { useDebouncedText } from "../useFilters";
import { plural } from "../format";

// fields: [{ name, label, options: [{ value, label }] } | { name, label, type: "date", min }]
function FilterBar({ filters, setFilter, reset, active, searchPlaceholder, fields, count, loading }) {
  const [text, setText] = useDebouncedText(filters.q, (value) => setFilter("q", value));

  return (
    <div className="filter-panel">
      <form className="filter-bar" role="search" onSubmit={(event) => event.preventDefault()}>
        <label className="filter-field filter-search">
          <span>Поиск</span>
          <input
            type="search"
            value={text}
            onChange={(event) => setText(event.target.value)}
            placeholder={searchPlaceholder}
          />
        </label>
        {fields.map((field) => (
          <label key={field.name} className="filter-field">
            <span>{field.label}</span>
            {field.type === "date" ? (
              <input
                type="date"
                value={filters[field.name]}
                min={field.min}
                onChange={(event) => setFilter(field.name, event.target.value)}
              />
            ) : (
              <select value={filters[field.name]} onChange={(event) => setFilter(field.name, event.target.value)}>
                {field.options.map((option) => (
                  <option key={option.value} value={option.value}>{option.label}</option>
                ))}
              </select>
            )}
          </label>
        ))}
      </form>
      <div className="filter-summary">
        <span className="muted small" aria-live="polite">
          {loading ? "Ищем..." : `Найдено: ${count} ${plural(count, ["событие", "события", "событий"])}`}
        </span>
        {active && (
          <button type="button" className="link-button" onClick={reset}>Сбросить фильтры</button>
        )}
      </div>
    </div>
  );
}

export const ratingOptions = [
  { value: "", label: "Любой" },
  { value: "3", label: "★ 3 и выше" },
  { value: "4", label: "★ 4 и выше" },
  { value: "4.5", label: "★ 4,5 и выше" }
];

export default FilterBar;
