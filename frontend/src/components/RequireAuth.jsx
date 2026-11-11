import { Navigate, useLocation } from "react-router-dom";
import { useAuth } from "../AuthContext";

function RequireAuth({ children }) {
  const { user } = useAuth();
  const location = useLocation();

  if (!user) {
    return <Navigate to={`/login?next=${encodeURIComponent(location.pathname)}`} replace />;
  }
  return children;
}

export default RequireAuth;
