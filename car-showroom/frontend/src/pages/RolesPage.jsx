import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { api, errorText } from '../api';
import { useAuth } from '../auth.jsx';

export default function RolesPage() {
  const { hasPermission } = useAuth();
  const canManage = hasPermission('ROLE_MANAGE');
  const [roles, setRoles] = useState(null);
  const [error, setError] = useState('');

  const load = () =>
    api.get('/roles').then((d) => { setRoles(d); setError(''); }).catch((e) => setError(errorText(e)));

  useEffect(() => { load(); }, []);

  const remove = async (r) => {
    if (!window.confirm(`Delete the role ${r.name}?`)) return;
    try {
      await api.del('/roles/' + r.id);
      load();
    } catch (e) {
      setError(errorText(e));
    }
  };

  return (
    <>
      <div className="page-head">
        <h1>Roles and permissions</h1>
        {canManage && <Link className="btn" to="/roles/new">Add role</Link>}
      </div>

      {error && <p className="alert">{error}</p>}

      <div className="table-wrap">
        <table>
          <thead>
            <tr><th>Role name</th><th>Description</th><th className="num">Permissions</th><th className="num">Users</th><th>Type</th><th /></tr>
          </thead>
          <tbody>
            {roles?.map((r) => (
              <tr key={r.id}>
                <td>{r.name}</td>
                <td>{r.description ?? <span className="muted">-</span>}</td>
                <td className="num">{r.permissionCount}</td>
                <td className="num">{r.userCount}</td>
                <td><span className={`tag ${r.system ? 'partner' : ''}`}>{r.system ? 'System' : 'Custom'}</span></td>
                <td className="actions">
                  {canManage && !r.system && <Link className="link" to={`/roles/${r.id}/edit`}>Edit</Link>}
                  {canManage && !r.system && <button className="link danger" onClick={() => remove(r)}>Delete</button>}
                  {canManage && r.system && <Link className="link" to={`/roles/${r.id}/edit`}>View</Link>}
                </td>
              </tr>
            ))}
            {roles && roles.length === 0 && <tr><td colSpan={6} className="empty">No roles yet.</td></tr>}
          </tbody>
        </table>
      </div>
    </>
  );
}
