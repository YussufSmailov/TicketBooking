import { Navigate, useLocation } from "react-router-dom";
import { useAuth } from "../AuthContext";

function RequireAdmin({ children }) {
  const { user, isAdmin } = useAuth();
  const location = useLocation();

  if (!user) {
    return <Navigate to={`/login?next=${encodeURIComponent(location.pathname)}`} replace />;
  }
  if (!isAdmin) {
    return <Navigate to="/" replace />;
  }
  return children;
}

export default RequireAdmin;
