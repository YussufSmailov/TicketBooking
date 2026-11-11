import { useState } from "react";
import FieldError from "./FieldError";
import { updateMe } from "../api";
import { useAuth } from "../AuthContext";
import { NAME_HINT, NAME_PATTERN, PHONE_HINT, PHONE_PATTERN } from "../validation";

function ProfileForm() {
  const { user, updateUser } = useAuth();
  const [fullName, setFullName] = useState(user.fullName);
  const [phone, setPhone] = useState(user.phone || "");
  const [fieldErrors, setFieldErrors] = useState({});
  const [error, setError] = useState("");
  const [message, setMessage] = useState("");
  const [saving, setSaving] = useState(false);

  async function handleSubmit(event) {
    event.preventDefault();
    setFieldErrors({});
    setError("");
    setMessage("");
    setSaving(true);
    try {
      const profile = await updateMe({ fullName: fullName, phone: phone });
      updateUser(profile);
      setMessage("Профиль сохранён");
    } catch (e) {
      setError(e.message);
      setFieldErrors(e.fieldErrors || {});
    } finally {
      setSaving(false);
    }
  }

  return (
    <form className="booking-form" onSubmit={handleSubmit}>
      <label>
        Email
        <input value={user.email} disabled />
      </label>
      <label>
        Имя и фамилия
        <input
          value={fullName}
          onChange={(e) => setFullName(e.target.value)}
          pattern={NAME_PATTERN}
          title={NAME_HINT}
          minLength={2}
          maxLength={100}
          required
        />
        <FieldError message={fieldErrors.fullName} />
      </label>
      <label>
        Телефон
        <input
          type="tel"
          value={phone}
          onChange={(e) => setPhone(e.target.value)}
          pattern={PHONE_PATTERN}
          title={PHONE_HINT}
          required
        />
        <FieldError message={fieldErrors.phone} />
      </label>
      {error && <p className="error">{error}</p>}
      {message && <p className="success" role="status">{message}</p>}
      <button className="primary-button" type="submit" disabled={saving}>
        {saving ? "Сохраняем..." : "Сохранить"}
      </button>
    </form>
  );
}

export default ProfileForm;
