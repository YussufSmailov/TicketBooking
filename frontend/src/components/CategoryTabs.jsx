const tabs = [
  { id: "cinema", name: "Кино" },
  { id: "concert", name: "Концерты" },
  { id: "sport", name: "Спорт" }
];

function CategoryTabs({ active, onChange }) {
  return (
    <nav className="tabs">
      {tabs.map((tab) => (
        <button
          key={tab.id}
          className={tab.id === active ? "tab tab-active" : "tab"}
          onClick={() => onChange(tab.id)}
        >
          {tab.name}
        </button>
      ))}
    </nav>
  );
}

export default CategoryTabs;
