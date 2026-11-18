import { useEffect, useState } from "react";
import { useSearchParams } from "react-router-dom";

// Фильтры списка хранятся в адресной строке (?genre=...&sort=...), поэтому ссылкой можно поделиться,
// а кнопка «назад» возвращает прошлые фильтры.
export function useFilters(names) {
  const [params, setParams] = useSearchParams();
  const filters = {};
  names.forEach((name) => {
    filters[name] = params.get(name) || "";
  });

  function setFilter(name, value) {
    const next = new URLSearchParams(params);
    if (value === "" || value === null || value === undefined) {
      next.delete(name);
    } else {
      next.set(name, value);
    }
    setParams(next, { replace: true });
  }

  function reset() {
    setParams(new URLSearchParams(), { replace: true });
  }

  const active = names.some((name) => filters[name] !== "");
  return { filters, setFilter, reset, active, key: params.toString() };
}

// Текст поиска обновляет адрес не на каждую букву, а через 300 мс после последнего нажатия.
export function useDebouncedText(value, onCommit) {
  const [text, setText] = useState(value);

  useEffect(() => {
    setText(value);
  }, [value]);

  useEffect(() => {
    if (text === value) {
      return;
    }
    const timer = setTimeout(() => onCommit(text.trim()), 300);
    return () => clearTimeout(timer);
  }, [text]);

  return [text, setText];
}
