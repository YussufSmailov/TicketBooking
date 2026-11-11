import { useState } from "react";
import { Link, Navigate, useNavigate, useSearchParams } from "react-router-dom";
import { useAuth } from "../AuthContext";
import { login } from "../api";
import FieldError from "../components/FieldError";
import { EMAIL_HINT, EMAIL_PATTERN } from "../validation";

function LoginPage() {
  const { user, logIn } = useAuth();
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const nextParam = searchParams.get("next");
  const next = nextParam && nextParam.startsWith("/") ? nextParam : "/";
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [fieldErrors, setFieldErrors] = useState({});
  const [sending, setSending] = useState(false);

  async function handleSubmit(event) {
    event.preventDefault();
    setError("");
    setFieldErrors({});
    setSending(true);
    try {
      const response = await login({ email: email, password: password });
      logIn(response);
      navigate(next, { replace: true });
    } catch (e) {
      setError(e.message);
      setFieldErrors(e.fieldErrors || {});
    } finally {
      setSending(false);
    }
  }

  if (user) {
    return <Navigate to={next} replace />;
  }

  return (
    <section className="auth-page">
      <h2>Вход</h2>
      <form className="booking-form" onSubmit={handleSubmit}>
        <label>
          Email
          <input
            type="email"
            autoComplete="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            pattern={EMAIL_PATTERN}
            title={EMAIL_HINT}
            required
          />
          <FieldError message={fieldErrors.email} />
        </label>
        <label>
          Пароль
          <input
            type="password"
            autoComplete="current-password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
          />
          <FieldError message={fieldErrors.password} />
        </label>
        {error && <p className="error">{error}</p>}
        <button className="primary-button" type="submit" disabled={sending}>
          {sending ? "Входим..." : "Войти"}
        </button>
        <p className="muted">
          Нет аккаунта? <Link className="link-button" to={next === "/" ? "/register" : `/register?next=${encodeURIComponent(next)}`}>Зарегистрироваться</Link>
        </p>
      </form>
    </section>
  );
}

export default LoginPage;
