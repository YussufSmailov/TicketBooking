import { useEffect, useState } from "react";
import { useSearchParams } from "react-router-dom";

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
