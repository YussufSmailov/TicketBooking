import { useState } from "react";
import MoviesAdmin from "./MoviesAdmin";
import ConcertsAdmin from "./ConcertsAdmin";
import MatchesAdmin from "./MatchesAdmin";

const tabs = [
  { id: "movies", name: "Фильмы и сеансы" },
  { id: "concerts", name: "Концерты" },
  { id: "matches", name: "Матчи КПЛ" }
];

function AdminPage() {
  const [activeTab, setActiveTab] = useState("movies");

  return (
    <div className="app" data-category="admin">
      <header className="header">
        <div className="header-top">
          <h1>Админ-панель</h1>
          <a className="header-link" href="/">На сайт</a>
        </div>
        <p>Добавляйте события, сеансы и места без работы с базой напрямую</p>
      </header>

      <nav className="tabs">
        {tabs.map((tab) => (
          <button
            key={tab.id}
            className={tab.id === activeTab ? "tab tab-active" : "tab"}
            onClick={() => setActiveTab(tab.id)}
          >
            {tab.name}
          </button>
        ))}
      </nav>

      {activeTab === "movies" && <MoviesAdmin />}
      {activeTab === "concerts" && <ConcertsAdmin />}
      {activeTab === "matches" && <MatchesAdmin />}
    </div>
  );
}

export default AdminPage;
