import { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { api, getToken, setToken } from './api';

const USER_KEY = 'user';
const AuthContext = createContext(null);

/** Screens in the order we try them when picking where to send the user after login. */
const HOME_CANDIDATES = [
  ['/vehicles', 'VEHICLE_VIEW'],
  ['/users', 'USER_VIEW'],
  ['/roles', 'ROLE_VIEW'],
];

const readUser = () => {
  try {
    return getToken() ? JSON.parse(localStorage.getItem(USER_KEY)) : null;
  } catch {
    return null;
  }
};

export function AuthProvider({ children }) {
  const navigate = useNavigate();
  const [user, setUser] = useState(readUser);

  const clear = useCallback(() => {
    setToken(null);
    localStorage.removeItem(USER_KEY);
    setUser(null);
  }, []);

  const hasPermission = useCallback((code) => !!user?.permissions?.includes(code), [user]);

  const firstAllowedPath = useCallback(
    () => HOME_CANDIDATES.find(([, code]) => user?.permissions?.includes(code))?.[0] ?? '/forbidden',
    [user]
  );

  const login = async (showroomCode, email, password) => {
    const res = await api.post('/auth/login', { showroomCode, email, password });
    setToken(res.token);
    localStorage.setItem(USER_KEY, JSON.stringify(res.user));
    setUser(res.user);
    const first = HOME_CANDIDATES.find(([, code]) => res.user.permissions.includes(code));
    navigate(first ? first[0] : '/forbidden', { replace: true });
  };

  const logout = useCallback(async () => {
    try {
      await api.post('/auth/logout');
    } catch {
      /* the token is discarded either way */
    }
    clear();
    navigate('/login', { replace: true });
  }, [clear, navigate]);

  // Central handling of expired sessions (401) and denied calls (403) from any API request.
  useEffect(() => {
    const onExpired = () => { clear(); navigate('/login', { replace: true }); };
    const onForbidden = () => navigate('/forbidden', { replace: true });
    window.addEventListener('auth:expired', onExpired);
    window.addEventListener('auth:forbidden', onForbidden);
    return () => {
      window.removeEventListener('auth:expired', onExpired);
      window.removeEventListener('auth:forbidden', onForbidden);
    };
  }, [clear, navigate]);

  // Validate a stored token once on load; an expired one sends the user back to login.
  useEffect(() => {
    if (getToken()) api.get('/auth/me').catch(() => {});
  }, []);

  const value = useMemo(
    () => ({ user, hasPermission, firstAllowedPath, login, logout }),
    [user, hasPermission, firstAllowedPath, logout]
  );
  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export const useAuth = () => useContext(AuthContext);
