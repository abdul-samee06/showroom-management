import { useEffect, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { api, errorText } from '../api';
import { useAuth } from '../auth.jsx';

const BLANK = { name: '', userTypeCode: 'investor', email: '', phone: '', active: true, password: '' };

export default function UserFormPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { hasPermission } = useAuth();
  const canAssign = hasPermission('USER_ASSIGN_ROLE'); // the Roles field is hidden without it
  const canDeactivate = hasPermission('USER_DEACTIVATE');

  const [f, setF] = useState(BLANK);
  const [types, setTypes] = useState([]);
  const [roles, setRoles] = useState([]);
  const [roleIds, setRoleIds] = useState(new Set());
  const [error, setError] = useState('');
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    api.get('/user-types').then(setTypes).catch((e) => setError(errorText(e)));
    if (canAssign) api.get('/roles/options').then(setRoles).catch((e) => setError(errorText(e)));
    if (id) {
      api.get('/users/' + id)
        .then((u) => {
          setF({ name: u.name, userTypeCode: u.userTypeCode, email: u.email ?? '', phone: u.phone ?? '',
                 active: u.active, password: '' });
          setRoleIds(new Set(u.roles.map((r) => r.id)));
        })
        .catch((e) => setError(errorText(e)));
    }
  }, [id, canAssign]);

  const set = (key) => (e) => setF({ ...f, [key]: e.target.value });

  const toggleRole = (rid) => {
    const next = new Set(roleIds);
    if (next.has(rid)) next.delete(rid); else next.add(rid);
    setRoleIds(next);
  };

  const submit = async (e) => {
    e.preventDefault();
    setSaving(true);
    setError('');
    const payload = {
      name: f.name,
      userTypeCode: f.userTypeCode,
      email: f.email || undefined,
      phone: f.phone || undefined,
      password: f.password || undefined, // blank on edit keeps the current password
    };
    if (canDeactivate || !id) payload.active = f.active;
    if (canAssign) payload.roleIds = [...roleIds];
    try {
      if (id) await api.put('/users/' + id, payload);
      else await api.post('/users', payload);
      navigate('/users');
    } catch (err) {
      setError(errorText(err));
      setSaving(false);
    }
  };

  return (
    <>
      <div className="page-head">
        <h1>{id ? 'Edit user' : 'Add user'}</h1>
        <Link className="link" to="/users">Back to users</Link>
      </div>

      <form className="form" onSubmit={submit}>
        {error && <p className="alert">{error}</p>}
        <div className="grid">
          <label>Name
            <input required maxLength={150} value={f.name} onChange={set('name')} autoFocus />
          </label>
          <label>Type
            <select value={f.userTypeCode} onChange={set('userTypeCode')}>
              {types.map((t) => <option key={t.code} value={t.code}>{t.label}</option>)}
            </select>
          </label>
          <label>Email
            <input type="email" maxLength={150} value={f.email} onChange={set('email')} />
          </label>
          <label>Phone
            <input maxLength={30} value={f.phone} onChange={set('phone')} />
          </label>
          <label>Password
            <input type="password" minLength={8} maxLength={72} autoComplete="new-password"
                   value={f.password} onChange={set('password')}
                   placeholder={id ? 'Leave blank to keep the current one' : 'Leave blank if the user cannot log in'} />
          </label>
        </div>

        {(canDeactivate || !id) && (
          <label className="check">
            <input type="checkbox" checked={f.active} onChange={(e) => setF({ ...f, active: e.target.checked })} />
            Active
          </label>
        )}

        {canAssign && (
          <fieldset>
            <legend>Roles</legend>
            {roles.length === 0 && <p className="muted">No roles defined yet.</p>}
            <div className="perm-list">
              {roles.map((r) => (
                <label className="check perm" key={r.id}>
                  <input type="checkbox" checked={roleIds.has(r.id)} onChange={() => toggleRole(r.id)} />
                  <span>{r.name}{r.system && <span className="muted"> · built-in</span>}</span>
                </label>
              ))}
            </div>
          </fieldset>
        )}

        <div className="form-actions">
          <button className="btn" disabled={saving}>{saving ? 'Saving…' : 'Save user'}</button>
          <Link className="btn ghost" to="/users">Cancel</Link>
        </div>
      </form>
    </>
  );
}
