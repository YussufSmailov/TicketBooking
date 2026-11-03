import React from "react";
import ReactDOM from "react-dom/client";
import App from "./App";
import AdminPage from "./admin/AdminPage";
import "./App.css";

const isAdminPage = window.location.pathname.startsWith("/admin");

ReactDOM.createRoot(document.getElementById("root")).render(
  <React.StrictMode>
    {isAdminPage ? <AdminPage /> : <App />}
  </React.StrictMode>
);
