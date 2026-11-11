import { createContext, useContext, useEffect, useState } from "react";
import { getMe } from "./api";
import { clearSession, getSavedUser, getToken, saveSession, saveUser } from "./auth";

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(getSavedUser());

  useEffect(() => {
    if (getToken()) {
      getMe()
        .then((profile) => {
          saveUser(profile);
          setUser(profile);
        })
        .catch(() => {
          clearSession();
          setUser(null);
        });
    }
  }, []);

  function logIn(response) {
    saveSession(response.token, response.user);
    setUser(response.user);
  }

  function logOut() {
    clearSession();
    setUser(null);
  }

  function updateUser(profile) {
    saveUser(profile);
    setUser(profile);
  }

  const value = {
    user: user,
    isAdmin: user !== null && user.role === "ADMIN",
    logIn: logIn,
    logOut: logOut,
    updateUser: updateUser
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  return useContext(AuthContext);
}
