import { Navigate, useLocation } from 'react-router-dom';
import { useAuth } from '../auth.jsx';

/** No session -> /login. Logged in but missing `permission` -> /forbidden. */
export default function ProtectedRoute({ permission, children }) {
  const { user, hasPermission } = useAuth();
  const location = useLocation();
  if (!user) return <Navigate to="/login" replace state={{ from: location.pathname }} />;
  if (permission && !hasPermission(permission)) return <Navigate to="/forbidden" replace />;
  return children;
}
