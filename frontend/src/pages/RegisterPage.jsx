import { useState } from "react";
import { Link, Navigate, useNavigate, useSearchParams } from "react-router-dom";
import { useAuth } from "../AuthContext";
import { register } from "../api";
import FieldError from "../components/FieldError";
import {
  EMAIL_HINT,
  EMAIL_PATTERN,
  NAME_HINT,
  NAME_PATTERN,
  PASSWORD_HINT,
  PASSWORD_PATTERN,
  PHONE_HINT,
  PHONE_PATTERN
} from "../validation";

function RegisterPage() {
  const { user } = useAuth();
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const nextParam = searchParams.get("next");
  const next = nextParam && nextParam.startsWith("/") ? nextParam : "/";
  const [form, setForm] = useState({ fullName: "", email: "", phone: "", password: "" });
  const [error, setError] = useState("");
  const [fieldErrors, setFieldErrors] = useState({});
  const [sending, setSending] = useState(false);

  function changeField(name, value) {
    setForm({ ...form, [name]: value });
  }

  async function handleSubmit(event) {
    event.preventDefault();
    setError("");
    setFieldErrors({});
    setSending(true);
    try {
      const response = await register(form);
      const nextPart = next === "/" ? "" : `&next=${encodeURIComponent(next)}`;
      navigate(`/verify?email=${encodeURIComponent(response.email)}${nextPart}`, {
        state: { sent: true, resendAfterSeconds: response.resendAfterSeconds }
      });
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
      <h2>Регистрация</h2>
      <form className="booking-form" onSubmit={handleSubmit}>
        <label>
          Имя и фамилия
          <input
            autoComplete="name"
            value={form.fullName}
            onChange={(e) => changeField("fullName", e.target.value)}
            pattern={NAME_PATTERN}
            title={NAME_HINT}
            minLength={2}
            maxLength={100}
            required
          />
          <FieldError message={fieldErrors.fullName} />
        </label>
        <label>
          Email
          <input
            type="email"
            autoComplete="email"
            value={form.email}
            onChange={(e) => changeField("email", e.target.value)}
            pattern={EMAIL_PATTERN}
            title={EMAIL_HINT}
            required
          />
          <FieldError message={fieldErrors.email} />
        </label>
        <label>
          Телефон
          <input
            type="tel"
            autoComplete="tel"
            value={form.phone}
            onChange={(e) => changeField("phone", e.target.value)}
            pattern={PHONE_PATTERN}
            title={PHONE_HINT}
            placeholder="+77001234567"
            required
          />
          <FieldError message={fieldErrors.phone} />
        </label>
        <label>
          Пароль
          <input
            type="password"
            autoComplete="new-password"
            value={form.password}
            onChange={(e) => changeField("password", e.target.value)}
            pattern={PASSWORD_PATTERN}
            title={PASSWORD_HINT}
            minLength={8}
            maxLength={72}
            required
          />
          <span className="muted small">{PASSWORD_HINT}</span>
          <FieldError message={fieldErrors.password} />
        </label>
        {error && <p className="error">{error}</p>}
        <p className="muted small">После регистрации мы пришлём на email 6-значный код подтверждения.</p>
        <button className="primary-button" type="submit" disabled={sending}>
          {sending ? "Отправляем код..." : "Зарегистрироваться"}
        </button>
        <p className="muted">
          Уже есть аккаунт? <Link className="link-button" to={next === "/" ? "/login" : `/login?next=${encodeURIComponent(next)}`}>Войти</Link>
        </p>
      </form>
    </section>
  );
}

export default RegisterPage;
