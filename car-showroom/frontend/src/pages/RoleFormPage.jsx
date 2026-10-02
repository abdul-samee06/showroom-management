import { useEffect, useMemo, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { api, errorText } from '../api';

export default function RoleFormPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [name, setName] = useState('');
  const [description, setDescription] = useState('');
  const [selected, setSelected] = useState(new Set());
  const [permissions, setPermissions] = useState([]);
  const [holders, setHolders] = useState([]);
  const [system, setSystem] = useState(false);
  const [error, setError] = useState('');
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    api.get('/permissions').then(setPermissions).catch((e) => setError(errorText(e)));
    if (id) {
      api.get('/roles/' + id)
        .then((r) => {
          setName(r.name);
          setDescription(r.description ?? '');
          setSelected(new Set(r.permissionCodes));
          setHolders(r.users);
          setSystem(r.system);
        })
        .catch((e) => setError(errorText(e)));
    }
  }, [id]);

  // Checkboxes grouped by module (Vehicles, Users, Roles)
  const groups = useMemo(() => {
    const map = new Map();
    permissions.forEach((p) => map.set(p.module, [...(map.get(p.module) ?? []), p]));
    return [...map.entries()];
  }, [permissions]);

  const toggle = (code) => {
    const next = new Set(selected);
    if (next.has(code)) next.delete(code); else next.add(code);
    setSelected(next);
  };

  const toggleGroup = (items) => {
    const next = new Set(selected);
    const all = items.every((p) => next.has(p.code));
    items.forEach((p) => (all ? next.delete(p.code) : next.add(p.code)));
    setSelected(next);
  };

  const submit = async (e) => {
    e.preventDefault();
    setSaving(true);
    setError('');
    const payload = { name, description, permissionCodes: [...selected] };
    try {
      if (id) await api.put('/roles/' + id, payload);
      else await api.post('/roles', payload);
      navigate('/roles');
    } catch (err) {
      setError(errorText(err));
      setSaving(false);
    }
  };

  return (
    <>
      <div className="page-head">
        <h1>{id ? (system ? 'Admin role' : 'Edit role') : 'Add role'}</h1>
        <Link className="link" to="/roles">Back to roles</Link>
      </div>

      <form className="form" onSubmit={submit}>
        {error && <p className="alert">{error}</p>}
        {system && <p className="muted">The built-in Admin role always has every permission and cannot be changed.</p>}

        <div className="grid">
          <label>Role name
            <input required maxLength={60} value={name} disabled={system} onChange={(e) => setName(e.target.value)} />
          </label>
          <label>Description
            <input maxLength={255} value={description} disabled={system} onChange={(e) => setDescription(e.target.value)} />
          </label>
        </div>

        {groups.map(([module, items]) => (
          <fieldset key={module}>
            <legend>{module}</legend>
            <label className="check">
              <input type="checkbox" disabled={system}
                     checked={items.every((p) => selected.has(p.code))}
                     onChange={() => toggleGroup(items)} />
              <b>Select all</b>
            </label>
            <div className="perm-list">
              {items.map((p) => (
                <label className="check perm" key={p.code}>
                  <input type="checkbox" disabled={system} checked={selected.has(p.code)} onChange={() => toggle(p.code)} />
                  <span>{p.label}{p.urlPattern && <span className="muted"> · opens <code>{p.urlPattern}</code></span>}</span>
                </label>
              ))}
            </div>
          </fieldset>
        ))}

        {id && (
          <fieldset>
            <legend>Users with this role</legend>
            {holders.length === 0
              ? <p className="muted">Nobody has this role yet.</p>
              : <div className="chips">{holders.map((u) => <span className="chip" key={u.id}>{u.name}</span>)}</div>}
          </fieldset>
        )}

        <div className="form-actions">
          {!system && <button className="btn" disabled={saving}>{saving ? 'Saving…' : 'Save role'}</button>}
          <Link className="btn ghost" to="/roles">{system ? 'Back' : 'Cancel'}</Link>
        </div>
      </form>
    </>
  );
}
