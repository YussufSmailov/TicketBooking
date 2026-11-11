import { useState } from "react";
import MoviesAdmin from "./MoviesAdmin";
import ConcertsAdmin from "./ConcertsAdmin";
import MatchesAdmin from "./MatchesAdmin";

const adminTabs = [
  { id: "movies", name: "Кино" },
  { id: "concerts", name: "Концерты" },
  { id: "matches", name: "Матчи" }
];

function AdminPage() {
  const [tab, setTab] = useState("movies");

  return (
    <section className="admin-page">
      <h2>Админка</h2>
      <nav className="tabs">
        {adminTabs.map((item) => (
          <button
            key={item.id}
            className={item.id === tab ? "tab tab-active" : "tab"}
            onClick={() => setTab(item.id)}
          >
            {item.name}
          </button>
        ))}
      </nav>
      {tab === "movies" && <MoviesAdmin />}
      {tab === "concerts" && <ConcertsAdmin />}
      {tab === "matches" && <MatchesAdmin />}
    </section>
  );
}

export default AdminPage;
