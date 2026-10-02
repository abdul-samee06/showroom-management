import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { api, money, errorText } from '../api';
import Plate from '../components/Plate.jsx';

const COLUMNS = [
  { key: 'wheels', label: 'Wheels' },
  { key: 'modelNo', label: 'Model no' },
  { key: 'modelYear', label: 'Year' },
  { key: 'numberPlate', label: 'Number plate' },
  { key: 'purchasePrice', label: 'Purchase price', num: true },
  { key: 'purchaseDate', label: 'Purchase date' },
];

export default function VehiclesPage() {
  const [q, setQ] = useState('');
  const [wheels, setWheels] = useState('');
  const [sortBy, setSortBy] = useState('purchaseDate');
  const [dir, setDir] = useState('desc');
  const [page, setPage] = useState(0);
  const [data, setData] = useState(null);
  const [error, setError] = useState('');
  const [reload, setReload] = useState(0);

  useEffect(() => {
    const timer = setTimeout(() => {
      const params = new URLSearchParams({ q, sortBy, dir, page, size: 10 });
      if (wheels) params.set('wheels', wheels);
      api.get('/vehicles?' + params)
        .then((d) => { setData(d); setError(''); })
        .catch((e) => setError(errorText(e)));
    }, 250);
    return () => clearTimeout(timer);
  }, [q, wheels, sortBy, dir, page, reload]);

  const sort = (key) => {
    if (key === sortBy) setDir(dir === 'asc' ? 'desc' : 'asc');
    else { setSortBy(key); setDir('asc'); }
    setPage(0);
  };

  const remove = async (v) => {
    if (!window.confirm(`Delete ${v.modelNo} (${v.numberPlate})?`)) return;
    try {
      await api.del('/vehicles/' + v.id);
      setReload((n) => n + 1);
    } catch (e) {
      setError(errorText(e));
    }
  };

  return (
    <>
      <div className="page-head">
        <h1>Vehicles</h1>
        <Link className="btn" to="/vehicles/new">Add vehicle</Link>
      </div>

      <div className="toolbar">
        <input type="search" placeholder="Search model or number plate" value={q}
               onChange={(e) => { setQ(e.target.value); setPage(0); }} />
        <select value={wheels} onChange={(e) => { setWheels(e.target.value); setPage(0); }}
                aria-label="Filter by wheels">
          <option value="">All wheels</option>
          <option value="2">2 wheels</option>
          <option value="4">4 wheels</option>
          <option value="6">6 wheels</option>
        </select>
      </div>

      {error && <p className="alert">{error}</p>}

      <div className="table-wrap">
        <table>
          <thead>
            <tr>
              {COLUMNS.map((c) => (
                <th key={c.key} className={c.num ? 'num' : ''}>
                  <button className="sort" onClick={() => sort(c.key)}>
                    {c.label}{sortBy === c.key ? (dir === 'asc' ? ' ▲' : ' ▼') : ''}
                  </button>
                </th>
              ))}
              <th>Investors</th>
              <th />
            </tr>
          </thead>
          <tbody>
            {data?.items.map((v) => (
              <tr key={v.id}>
                <td>{v.wheels}</td>
                <td>{v.modelNo}</td>
                <td>{v.modelYear}</td>
                <td><Plate value={v.numberPlate} /></td>
                <td className="num">{money(v.purchasePrice)}</td>
                <td>{v.purchaseDate}</td>
                <td>
                  {v.investors.length === 0 && <span className="muted">None</span>}
                  <div className="chips">
                    {v.investors.map((i) => (
                      <span className="chip" key={i.userId}
                            title={`${money(i.amountInvested)} (${i.sharePercent}% of price)`}>
                        {i.userName} <b>{i.sharePercent}%</b>
                      </span>
                    ))}
                  </div>
                </td>
                <td className="actions">
                  <Link className="link" to={`/vehicles/${v.id}/edit`}>Edit</Link>
                  <button className="link danger" onClick={() => remove(v)}>Delete</button>
                </td>
              </tr>
            ))}
            {data && data.items.length === 0 && (
              <tr><td colSpan={8} className="empty">
                No vehicles found. <Link className="link" to="/vehicles/new">Add the first vehicle</Link>.
              </td></tr>
            )}
          </tbody>
        </table>
      </div>

      {data && data.totalPages > 1 && (
        <div className="pager">
          <button className="btn ghost" disabled={page === 0} onClick={() => setPage(page - 1)}>Previous</button>
          <span>Page {data.page + 1} of {data.totalPages} ({data.totalElements} vehicles)</span>
          <button className="btn ghost" disabled={page + 1 >= data.totalPages} onClick={() => setPage(page + 1)}>Next</button>
        </div>
      )}
    </>
  );
}
