import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { api, money, errorText } from '../api';
import { useAuth } from '../auth.jsx';
import Modal from '../components/Modal.jsx';
import Plate from '../components/Plate.jsx';

export default function UsersPage() {
  const { hasPermission } = useAuth();
  const [users, setUsers] = useState(null);
  const [types, setTypes] = useState([]);
  const [roles, setRoles] = useState([]);
  const [typeFilter, setTypeFilter] = useState('');
  const [roleFilter, setRoleFilter] = useState('');
  const [error, setError] = useState('');
  const [viewing, setViewing] = useState(null); // { user, vehicles }

  const load = () => {
    const params = new URLSearchParams();
    if (typeFilter) params.set('type', typeFilter);
    if (roleFilter) params.set('roleId', roleFilter);
    return api.get('/users?' + params)
      .then((d) => { setUsers(d); setError(''); })
      .catch((e) => setError(errorText(e)));
  };

  useEffect(() => {
    api.get('/user-types').then(setTypes).catch((e) => setError(errorText(e)));
    api.get('/roles/options').then(setRoles).catch((e) => setError(errorText(e)));
  }, []);
  useEffect(() => { load(); /* eslint-disable-next-line react-hooks/exhaustive-deps */ }, [typeFilter, roleFilter]);

  const toggleActive = async (u) => {
    const verb = u.active ? 'Deactivate' : 'Activate';
    if (!window.confirm(`${verb} ${u.name}?`)) return;
    try {
      await api.patch(`/users/${u.id}/status`, { active: !u.active });
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

  return (
    <>
      <div className="page-head">
        <h1>Users</h1>
        {hasPermission('USER_CREATE') && <Link className="btn" to="/users/new">Add user</Link>}
      </div>

      <div className="toolbar">
        <select value={typeFilter} onChange={(e) => setTypeFilter(e.target.value)} aria-label="Filter by type">
          <option value="">All types</option>
          {types.map((t) => <option key={t.code} value={t.code}>{t.label}</option>)}
        </select>
        <select value={roleFilter} onChange={(e) => setRoleFilter(e.target.value)} aria-label="Filter by role">
          <option value="">All roles</option>
          {roles.map((r) => <option key={r.id} value={r.id}>{r.name}</option>)}
        </select>
      </div>

      {error && <p className="alert">{error}</p>}

      <div className="table-wrap">
        <table>
          <thead>
            <tr><th>Name</th><th>Type</th><th>Roles</th><th>Email</th><th>Phone</th><th>Status</th><th /></tr>
          </thead>
          <tbody>
            {users?.map((u) => (
              <tr key={u.id}>
                <td>{u.name}</td>
                <td><span className={`tag ${u.userTypeCode}`}>{u.userTypeLabel}</span></td>
                <td>
                  {u.roles.length === 0 && <span className="muted">None</span>}
                  <div className="chips">{u.roles.map((r) => <span className="chip" key={r.id}>{r.name}</span>)}</div>
                </td>
                <td>{u.email ?? <span className="muted">-</span>}</td>
                <td>{u.phone ?? <span className="muted">-</span>}</td>
                <td>{u.active ? 'Active' : <span className="muted">Inactive</span>}</td>
                <td className="actions">
                  <button className="link" onClick={() => showVehicles(u)}>Vehicles</button>
                  {hasPermission('USER_EDIT') && <Link className="link" to={`/users/${u.id}/edit`}>Edit</Link>}
                  {hasPermission('USER_DEACTIVATE') && (
                    <button className={`link ${u.active ? 'danger' : ''}`} onClick={() => toggleActive(u)}>
                      {u.active ? 'Deactivate' : 'Activate'}
                    </button>
                  )}
                </td>
              </tr>
            ))}
            {users && users.length === 0 && (
              <tr><td colSpan={7} className="empty">No users found.</td></tr>
            )}
          </tbody>
        </table>
      </div>

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
