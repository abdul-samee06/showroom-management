import { useEffect, useState } from 'react';
import { NavLink, Navigate, Route, Routes } from 'react-router-dom';
import { api, getShowroomId, setShowroomId, errorText } from './api';
import VehiclesPage from './pages/VehiclesPage.jsx';
import VehicleFormPage from './pages/VehicleFormPage.jsx';
import UsersPage from './pages/UsersPage.jsx';

export default function App() {
  const [showrooms, setShowrooms] = useState(null);
  const [sid, setSid] = useState(getShowroomId());
  const [error, setError] = useState('');

  const load = async (preferId) => {
    try {
      const list = await api.get('/showrooms');
      setShowrooms(list);
      const wanted = preferId ?? sid;
      const match = list.find((s) => String(s.id) === String(wanted)) ?? list[0];
      if (match) {
        setShowroomId(match.id);
        setSid(String(match.id));
      }
    } catch (e) {
      setError(errorText(e));
    }
  };

  useEffect(() => {
    load();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const addShowroom = async () => {
    const name = window.prompt('Showroom name');
    if (!name || !name.trim()) return;
    try {
      const created = await api.post('/showrooms', { name });
      await load(created.id);
    } catch (e) {
      setError(errorText(e));
    }
  };

  if (error) return <div className="page"><p className="alert">{error} Is the backend running on port 8080?</p></div>;
  if (!showrooms) return <div className="page"><p className="muted">Loading…</p></div>;

  return (
    <>
      <header className="topbar">
        <div className="brand">Car Showroom</div>
        <nav>
          <NavLink to="/vehicles">Vehicles</NavLink>
          <NavLink to="/users">Users</NavLink>
        </nav>
        <div className="switcher">
          <select
            aria-label="Showroom"
            value={sid ?? ''}
            onChange={(e) => { setShowroomId(e.target.value); setSid(e.target.value); }}
          >
            {showrooms.map((s) => <option key={s.id} value={s.id}>{s.name}</option>)}
          </select>
          <button className="btn ghost" onClick={addShowroom}>New showroom</button>
        </div>
      </header>

      {sid ? (
        // key remounts the pages when the showroom changes, so every list reloads
        <main key={sid} className="page">
          <Routes>
            <Route path="/" element={<Navigate to="/vehicles" replace />} />
            <Route path="/vehicles" element={<VehiclesPage />} />
            <Route path="/vehicles/new" element={<VehicleFormPage />} />
            <Route path="/vehicles/:id/edit" element={<VehicleFormPage />} />
            <Route path="/users" element={<UsersPage />} />
          </Routes>
        </main>
      ) : (
        <main className="page">
          <p>Create your first showroom to get started.</p>
          <button className="btn" onClick={addShowroom}>Create showroom</button>
        </main>
      )}
    </>
  );
}
