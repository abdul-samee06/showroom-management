import { useState } from 'react';
import { Navigate } from 'react-router-dom';
import { useAuth } from '../auth.jsx';
import { errorText } from '../api';

export default function LoginPage() {
  const { user, login, firstAllowedPath } = useAuth();
  const [f, setF] = useState({ showroomCode: localStorage.getItem('showroomCode') ?? '', email: '', password: '' });
  const [error, setError] = useState('');
  const [busy, setBusy] = useState(false);

  if (user) return <Navigate to={firstAllowedPath()} replace />;

  const set = (key) => (e) => setF({ ...f, [key]: e.target.value });

  const submit = async (e) => {
    e.preventDefault();
    setBusy(true);
    setError('');
    try {
      localStorage.setItem('showroomCode', f.showroomCode.trim());
      await login(f.showroomCode.trim(), f.email.trim(), f.password);
    } catch (err) {
      setError(errorText(err));
      setBusy(false);
    }
  };

  return (
    <div className="login-wrap">
      <form className="form login-card" onSubmit={submit}>
        <h1>Car Showroom</h1>
        <p className="muted">Log in to your showroom.</p>
        {error && <p className="alert" role="alert">{error}</p>}
        <label>Showroom code
          <input required autoFocus autoCapitalize="characters" value={f.showroomCode} onChange={set('showroomCode')} />
        </label>
        <label>Email
          <input required type="email" autoComplete="username" value={f.email} onChange={set('email')} />
        </label>
        <label>Password
          <input required type="password" autoComplete="current-password" value={f.password} onChange={set('password')} />
        </label>
        <button className="btn" disabled={busy}>{busy ? 'Logging in…' : 'Login'}</button>
      </form>
    </div>
  );
}
