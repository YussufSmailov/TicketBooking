import { Link } from "react-router-dom";

function NotFoundPage() {
  return (
    <section className="empty-page">
      <h2 className="page-title">404</h2>
      <p>Такой страницы нет.</p>
      <Link className="primary-button" to="/">На главную</Link>
    </section>
  );
}

export default NotFoundPage;
