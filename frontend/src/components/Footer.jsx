import { Link } from "react-router-dom";

function Footer() {
  return (
    <footer className="footer">
      <div className="footer-column">
        <b>Билеты</b>
        <span>Учебный проект: кино, концерты и матчи КПЛ.</span>
        <span>Оплата ненастоящая, деньги не списываются.</span>
      </div>
      <nav className="footer-column" aria-label="Разделы сайта">
        <b>Афиша</b>
        <Link to="/cinema">Кино</Link>
        <Link to="/concerts">Концерты</Link>
        <Link to="/sport">Матчи КПЛ</Link>
      </nav>
      <nav className="footer-column" aria-label="Аккаунт">
        <b>Аккаунт</b>
        <Link to="/account">Личный кабинет</Link>
        <Link to="/account?tab=cards">Мои карты</Link>
      </nav>
      <div className="footer-column">
        <b>Контакты</b>
        <span>Алматы, Казахстан</span>
        <span>support@ticketbooking.kz</span>
      </div>
    </footer>
  );
}

export default Footer;
