import { Route, Routes, useLocation } from "react-router-dom";
import Header from "./components/Header";
import Footer from "./components/Footer";
import RequireAuth from "./components/RequireAuth";
import RequireAdmin from "./components/RequireAdmin";
import HomePage from "./pages/HomePage";
import CinemaListPage from "./pages/CinemaListPage";
import ConcertListPage from "./pages/ConcertListPage";
import SportListPage from "./pages/SportListPage";
import MoviePage from "./pages/MoviePage";
import SessionPage from "./pages/SessionPage";
import ConcertPage from "./pages/ConcertPage";
import MatchPage from "./pages/MatchPage";
import LoginPage from "./pages/LoginPage";
import RegisterPage from "./pages/RegisterPage";
import AccountPage from "./pages/AccountPage";
import NotFoundPage from "./pages/NotFoundPage";
import AdminPage from "./admin/AdminPage";

function categoryFromPath(path) {
  if (path.startsWith("/cinema") || path.startsWith("/movies") || path.startsWith("/sessions")) {
    return "cinema";
  }
  if (path.startsWith("/concerts")) {
    return "concert";
  }
  if (path.startsWith("/sport") || path.startsWith("/matches")) {
    return "sport";
  }
  return "home";
}

function App() {
  const location = useLocation();

  return (
    <div className="app" data-category={categoryFromPath(location.pathname)}>
      <a className="skip-link" href="#main-content">Перейти к содержимому</a>
      <Header />
      <main id="main-content" tabIndex={-1}>
        <Routes>
          <Route path="/" element={<HomePage />} />
          <Route path="/cinema" element={<CinemaListPage />} />
          <Route path="/concerts" element={<ConcertListPage />} />
          <Route path="/sport" element={<SportListPage />} />
          <Route path="/movies/:id" element={<MoviePage />} />
          <Route path="/sessions/:id" element={<SessionPage />} />
          <Route path="/concerts/:id" element={<ConcertPage />} />
          <Route path="/matches/:id" element={<MatchPage />} />
          <Route path="/login" element={<LoginPage />} />
          <Route path="/register" element={<RegisterPage />} />
          <Route path="/account" element={<RequireAuth><AccountPage /></RequireAuth>} />
          <Route path="/admin" element={<RequireAdmin><AdminPage /></RequireAdmin>} />
          <Route path="*" element={<NotFoundPage />} />
        </Routes>
      </main>
      <Footer />
    </div>
  );
}

export default App;
