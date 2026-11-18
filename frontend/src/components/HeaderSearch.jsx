import { useEffect, useState } from "react";
import { useLocation, useNavigate, useSearchParams } from "react-router-dom";

function HeaderSearch() {
  const navigate = useNavigate();
  const location = useLocation();
  const [params] = useSearchParams();
  const onSearchPage = location.pathname === "/search";
  const [text, setText] = useState(onSearchPage ? params.get("q") || "" : "");

  useEffect(() => {
    if (onSearchPage) {
      setText(params.get("q") || "");
    }
  }, [onSearchPage, params]);

  function handleSubmit(event) {
    event.preventDefault();
    const query = text.trim();
    if (query.length > 0) {
      navigate(`/search?q=${encodeURIComponent(query)}`);
    }
  }

  return (
    <form className="header-search" role="search" onSubmit={handleSubmit}>
      <label className="visually-hidden" htmlFor="header-search-input">Поиск по сайту</label>
      <input
        id="header-search-input"
        type="search"
        value={text}
        onChange={(event) => setText(event.target.value)}
        placeholder="Фильмы, концерты, матчи"
        minLength={2}
      />
      <button type="submit" className="primary-button">Найти</button>
    </form>
  );
}

export default HeaderSearch;
