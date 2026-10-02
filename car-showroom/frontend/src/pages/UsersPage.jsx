import { useEffect, useState } from 'react';
import { api, money, errorText } from '../api';
import Modal from '../components/Modal.jsx';
import Plate from '../components/Plate.jsx';

const BLANK = { name: '', userTypeCode: 'investor', email: '', phone: '', active: true };

export default function UsersPage() {
  const [users, setUsers] = useState(null);
  const [types, setTypes] = useState([]);
  const [filter, setFilter] = useState('');
  const [error, setError] = useState('');
  const [editing, setEditing] = useState(null); // { id?, ...fields }
  const [formError, setFormError] = useState('');
  const [viewing, setViewing] = useState(null); // { user, vehicles }

  const load = () =>
    api.get('/users' + (filter ? `?type=${filter}` : ''))
      .then((d) => { setUsers(d); setError(''); })
      .catch((e) => setError(errorText(e)));

  useEffect(() => { api.get('/user-types').then(setTypes).catch((e) => setError(errorText(e))); }, []);
  useEffect(() => { load(); /* eslint-disable-next-line react-hooks/exhaustive-deps */ }, [filter]);

  const openAdd = () => { setFormError(''); setEditing({ ...BLANK }); };
  const openEdit = (u) => {
    setFormError('');
    setEditing({ id: u.id, name: u.name, userTypeCode: u.userTypeCode, email: u.email ?? '', phone: u.phone ?? '', active: u.active });
  };

  const save = async (e) => {
    e.preventDefault();
    const { id, ...body } = editing;
    try {
      if (id) await api.put('/users/' + id, body);
      else await api.post('/users', body);
      setEditing(null);
      load();
    } catch (err) {
      setFormError(errorText(err));
    }
  };

  const remove = async (u) => {
    if (!window.confirm(`Delete ${u.name}?`)) return;
    try {
      await api.del('/users/' + u.id);
      load();
    } catch (err) {
      setError(errorText(err));
    }
  };

  const showVehicles = async (u) => {
    try {
      setViewing({ user: u, vehicles: await api.get(`/users/${u.id}/vehicles`) });
    } catch (err) {
      setError(errorText(err));
    }
  };

  const set = (key) => (e) => setEditing({ ...editing, [key]: e.target.value });

  return (
    <>
      <div className="page-head">
        <h1>Users</h1>
        <button className="btn" onClick={openAdd}>Add user</button>
      </div>

      <div className="toolbar">
        <select value={filter} onChange={(e) => setFilter(e.target.value)} aria-label="Filter by type">
          <option value="">All types</option>
          {types.map((t) => <option key={t.code} value={t.code}>{t.label}</option>)}
        </select>
      </div>

      {error && <p className="alert">{error}</p>}

      <div className="table-wrap">
        <table>
          <thead>
            <tr><th>Name</th><th>Type</th><th>Email</th><th>Phone</th><th>Status</th><th /></tr>
          </thead>
          <tbody>
            {users?.map((u) => (
              <tr key={u.id}>
                <td>{u.name}</td>
                <td><span className={`tag ${u.userTypeCode}`}>{u.userTypeLabel}</span></td>
                <td>{u.email ?? <span className="muted">-</span>}</td>
                <td>{u.phone ?? <span className="muted">-</span>}</td>
                <td>{u.active ? 'Active' : <span className="muted">Inactive</span>}</td>
                <td className="actions">
                  <button className="link" onClick={() => showVehicles(u)}>Vehicles</button>
                  <button className="link" onClick={() => openEdit(u)}>Edit</button>
                  <button className="link danger" onClick={() => remove(u)}>Delete</button>
                </td>
              </tr>
            ))}
            {users && users.length === 0 && (
              <tr><td colSpan={6} className="empty">No users yet. Add a partner or an investor to begin.</td></tr>
            )}
          </tbody>
        </table>
      </div>

      {editing && (
        <Modal title={editing.id ? 'Edit user' : 'Add user'} onClose={() => setEditing(null)}>
          <form className="form" onSubmit={save}>
            {formError && <p className="alert">{formError}</p>}
            <label>Name
              <input required maxLength={150} value={editing.name} onChange={set('name')} autoFocus />
            </label>
            <label>Type
              <select value={editing.userTypeCode} onChange={set('userTypeCode')}>
                {types.map((t) => <option key={t.code} value={t.code}>{t.label}</option>)}
              </select>
            </label>
            <label>Email
              <input type="email" maxLength={150} value={editing.email} onChange={set('email')} />
            </label>
            <label>Phone
              <input maxLength={30} value={editing.phone} onChange={set('phone')} />
            </label>
            <label className="check">
              <input type="checkbox" checked={editing.active}
                     onChange={(e) => setEditing({ ...editing, active: e.target.checked })} />
              Active
            </label>
            <div className="form-actions">
              <button className="btn">Save user</button>
              <button type="button" className="btn ghost" onClick={() => setEditing(null)}>Cancel</button>
            </div>
          </form>
        </Modal>
      )}

      {viewing && (
        <Modal title={`Vehicles of ${viewing.user.name}`} onClose={() => setViewing(null)}>
          {viewing.vehicles.length === 0 ? (
            <p className="muted">{viewing.user.name} has no investments yet.</p>
          ) : (
            <ul className="veh-list">
              {viewing.vehicles.map((v) => {
                const mine = v.investors.find((i) => i.userId === viewing.user.id);
                return (
                  <li key={v.id}>
                    <Plate value={v.numberPlate} />
                    <span>{v.modelNo} ({v.modelYear})</span>
                    <span className="muted">{money(mine.amountInvested)} · {mine.sharePercent}%</span>
                  </li>
                );
              })}
            </ul>
          )}
        </Modal>
      )}
    </>
  );
}
