import { useSearchParams } from "react-router-dom";
import MyTickets from "../components/MyTickets";
import MyCards from "../components/MyCards";
import ProfileForm from "../components/ProfileForm";
import { useAuth } from "../AuthContext";

const accountTabs = [
  { id: "tickets", name: "Мои билеты" },
  { id: "cards", name: "Мои карты" },
  { id: "profile", name: "Профиль" }
];

function AccountPage() {
  const { user } = useAuth();
  const [searchParams, setSearchParams] = useSearchParams();
  const tabFromUrl = searchParams.get("tab");
  const activeTab = accountTabs.some((tab) => tab.id === tabFromUrl) ? tabFromUrl : "tickets";

  return (
    <section>
      <h2 className="page-title">Личный кабинет</h2>
      <p className="muted">{user.fullName}, {user.email}</p>
      <div className="tabs" role="tablist" aria-label="Разделы кабинета">
        {accountTabs.map((tab) => (
          <button
            key={tab.id}
            role="tab"
            aria-selected={tab.id === activeTab}
            className={tab.id === activeTab ? "tab tab-active" : "tab"}
            onClick={() => setSearchParams({ tab: tab.id })}
          >
            {tab.name}
          </button>
        ))}
      </div>
      <div role="tabpanel">
        {activeTab === "tickets" && <MyTickets />}
        {activeTab === "cards" && <MyCards />}
        {activeTab === "profile" && <ProfileForm />}
      </div>
    </section>
  );
}

export default AccountPage;
