import { useState } from "react";
import { applyTheme, getCurrentTheme } from "../theme";

function ThemeToggle() {
  const [theme, setTheme] = useState(getCurrentTheme());

  function toggle() {
    const nextTheme = theme === "dark" ? "light" : "dark";
    applyTheme(nextTheme);
    setTheme(nextTheme);
  }

  const label = theme === "dark" ? "Включить светлую тему" : "Включить тёмную тему";

  return (
    <button type="button" className="icon-button theme-button" onClick={toggle} aria-label={label} title={label}>
      {theme === "dark" ? "☀" : "☾"}
    </button>
  );
}

export default ThemeToggle;
