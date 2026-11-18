import { Link, NavLink, useNavigate } from "react-router-dom";
import { useAuth } from "../AuthContext";
import ThemeToggle from "./ThemeToggle";
import HeaderSearch from "./HeaderSearch";

function Header() {
  const { user, isAdmin, logOut } = useAuth();
  const navigate = useNavigate();

  function handleLogout() {
    logOut();
    navigate("/");
  }

  return (
    <header className="header">
      <div className="header-top">
        <Link to="/" className="logo">Билеты</Link>
        <HeaderSearch />
        <nav className="header-nav" aria-label="Аккаунт">
          {user ? (
            <>
              <span className="header-user">{user.fullName}</span>
              <Link className="secondary-button" to="/account">Личный кабинет</Link>
              {isAdmin && <Link className="secondary-button" to="/admin">Админка</Link>}
              <button className="secondary-button" onClick={handleLogout}>Выйти</button>
            </>
          ) : (
            <>
              <Link className="secondary-button" to="/login">Войти</Link>
              <Link className="primary-button" to="/register">Регистрация</Link>
            </>
          )}
        </nav>
        <ThemeToggle />
      </div>
      <nav className="tabs" aria-label="Разделы">
        <NavLink to="/" end className={({ isActive }) => (isActive ? "tab tab-active" : "tab")}>Главная</NavLink>
        <NavLink to="/cinema" className={({ isActive }) => (isActive ? "tab tab-active" : "tab")}>Кино</NavLink>
        <NavLink to="/concerts" end className={({ isActive }) => (isActive ? "tab tab-active" : "tab")}>Концерты</NavLink>
        <NavLink to="/sport" className={({ isActive }) => (isActive ? "tab tab-active" : "tab")}>Спорт</NavLink>
      </nav>
    </header>
  );
}

export default Header;
