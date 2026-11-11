const TOKEN_KEY = "ticket-booking-token";
const USER_KEY = "ticket-booking-user";

export function getToken() {
  try {
    return localStorage.getItem(TOKEN_KEY);
  } catch {
    return null;
  }
}

export function getSavedUser() {
  try {
    const text = localStorage.getItem(USER_KEY);
    return text ? JSON.parse(text) : null;
  } catch {
    return null;
  }
}

export function saveSession(token, user) {
  try {
    localStorage.setItem(TOKEN_KEY, token);
    localStorage.setItem(USER_KEY, JSON.stringify(user));
  } catch {
    return;
  }
}

export function saveUser(user) {
  try {
    localStorage.setItem(USER_KEY, JSON.stringify(user));
  } catch {
    return;
  }
}

export function clearSession() {
  try {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
  } catch {
    return;
  }
}
