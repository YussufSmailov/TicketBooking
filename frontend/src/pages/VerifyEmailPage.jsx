import { useEffect, useRef, useState } from "react";
import { Link, Navigate, useLocation, useNavigate, useSearchParams } from "react-router-dom";
import { useAuth } from "../AuthContext";
import { resendCode, verifyEmail } from "../api";
import FieldError from "../components/FieldError";
import { EMAIL_HINT, EMAIL_PATTERN } from "../validation";

function VerifyEmailPage() {
  const { user, logIn } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [searchParams] = useSearchParams();
  const nextParam = searchParams.get("next");
  const next = nextParam && nextParam.startsWith("/") ? nextParam : "/";
  const state = location.state || {};

  const [email, setEmail] = useState(searchParams.get("email") || "");
  const [code, setCode] = useState("");
  const [error, setError] = useState("");
  const [fieldErrors, setFieldErrors] = useState({});
  const [info, setInfo] = useState(
    state.notVerified
      ? "Email ещё не подтверждён. Запросите код и введите его ниже."
      : state.sent ? `Мы отправили 6-значный код на ${searchParams.get("email")}. Проверьте также папку «Спам».` : ""
  );
  const [sending, setSending] = useState(false);
  const [cooldown, setCooldown] = useState(state.sent ? state.resendAfterSeconds || 60 : 0);
  const codeRef = useRef(null);

  useEffect(() => {
    if (codeRef.current && email) {
      codeRef.current.focus();
    }
  }, []);

  useEffect(() => {
    if (cooldown <= 0) {
      return;
    }
    const timer = setTimeout(() => setCooldown(cooldown - 1), 1000);
    return () => clearTimeout(timer);
  }, [cooldown]);

  async function handleSubmit(event) {
    event.preventDefault();
    setError("");
    setFieldErrors({});
    setSending(true);
    try {
      const response = await verifyEmail(email, code);
      logIn(response);
      navigate(next, { replace: true });
    } catch (e) {
      setError(e.message);
      setFieldErrors(e.fieldErrors || {});
    } finally {
      setSending(false);
    }
  }

  async function handleResend() {
    setError("");
    setInfo("");
    try {
      const response = await resendCode(email);
      setInfo(`Новый код отправлен на ${response.email}.`);
      setCooldown(response.resendAfterSeconds);
      setCode("");
      if (codeRef.current) {
        codeRef.current.focus();
      }
    } catch (e) {
      setError(e.message);
      setFieldErrors(e.fieldErrors || {});
    }
  }

  if (user) {
    return <Navigate to={next} replace />;
  }

  return (
    <section className="auth-page">
      <h2>Подтверждение email</h2>
      <form className="booking-form" onSubmit={handleSubmit}>
        {info && <p className="success" role="status">{info}</p>}
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
          Код из письма
          <input
            ref={codeRef}
            className="code-input"
            inputMode="numeric"
            autoComplete="one-time-code"
            value={code}
            onChange={(e) => setCode(e.target.value.replace(/\D/g, "").slice(0, 6))}
            pattern="[0-9]{6}"
            title="6 цифр из письма"
            placeholder="••••••"
            required
          />
          <FieldError message={fieldErrors.code} />
        </label>
        {error && <p className="error" role="alert">{error}</p>}
        <button className="primary-button" type="submit" disabled={sending || code.length !== 6}>
          {sending ? "Проверяем..." : "Подтвердить"}
        </button>
        <button type="button" className="secondary-button" onClick={handleResend} disabled={cooldown > 0 || !email}>
          {cooldown > 0 ? `Отправить код ещё раз через ${cooldown} сек.` : "Отправить код ещё раз"}
        </button>
        <p className="muted small">
          Код действует 15 минут. Ошиблись адресом? <Link className="link-button" to="/register">Зарегистрироваться заново</Link>
        </p>
      </form>
    </section>
  );
}

export default VerifyEmailPage;
