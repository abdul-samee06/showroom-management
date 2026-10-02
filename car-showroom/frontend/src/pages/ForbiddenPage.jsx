import { Link } from 'react-router-dom';
import { useAuth } from '../auth.jsx';

export default function ForbiddenPage() {
  const { user, firstAllowedPath, logout } = useAuth();
  const home = firstAllowedPath();
  return (
    <div className="login-wrap">
      <div className="form login-card">
        <h1>403</h1>
        <p>You don't have permission to view this page.</p>
        {user && home !== '/forbidden'
          ? <Link className="btn" to={home}>Back to a page you can open</Link>
          : <p className="muted">{user ? 'Your account has no roles yet. Ask an admin to assign one.' : ''}</p>}
        {user
          ? <button className="btn ghost" onClick={logout}>Log out</button>
          : <Link className="btn ghost" to="/login">Go to login</Link>}
      </div>
    </div>
  );
}
