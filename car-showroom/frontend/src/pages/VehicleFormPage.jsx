import { useEffect, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { api, money, errorText } from '../api';

const today = () => new Date().toISOString().slice(0, 10);

const EMPTY = {
  wheels: '4',
  modelNo: '',
  modelYear: String(new Date().getFullYear()),
  numberPlate: '',
  purchasePrice: '',
  purchaseDate: today(),
  notes: '',
};

export default function VehicleFormPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [f, setF] = useState(EMPTY);
  const [investors, setInvestors] = useState([]);
  const [users, setUsers] = useState([]);
  const [error, setError] = useState('');
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    api.get('/users').then(setUsers).catch((e) => setError(errorText(e)));
    if (id) {
      api.get('/vehicles/' + id)
        .then((v) => {
          setF({
            wheels: String(v.wheels),
            modelNo: v.modelNo,
            modelYear: String(v.modelYear),
            numberPlate: v.numberPlate,
            purchasePrice: String(v.purchasePrice),
            purchaseDate: v.purchaseDate,
            notes: v.notes ?? '',
          });
          setInvestors(v.investors.map((i) => ({ userId: String(i.userId), amount: String(i.amountInvested) })));
        })
        .catch((e) => setError(errorText(e)));
    }
  }, [id]);

  const set = (key) => (e) => setF({ ...f, [key]: e.target.value });

  const price = parseFloat(f.purchasePrice) || 0;
  const total = investors.reduce((sum, i) => sum + (parseFloat(i.amount) || 0), 0);
  const over = total > price;
  const share = (amount) => (price > 0 ? (((parseFloat(amount) || 0) * 100) / price).toFixed(2) : '0.00');

  const updateInvestor = (index, patch) =>
    setInvestors(investors.map((row, i) => (i === index ? { ...row, ...patch } : row)));

  const submit = async (e) => {
    e.preventDefault();
    setSaving(true);
    setError('');
    const payload = {
      wheels: Number(f.wheels),
      modelNo: f.modelNo,
      modelYear: Number(f.modelYear),
      numberPlate: f.numberPlate,
      purchasePrice: Number(f.purchasePrice),
      purchaseDate: f.purchaseDate,
      notes: f.notes,
      investors: investors
        .filter((i) => i.userId && i.amount)
        .map((i) => ({ userId: Number(i.userId), amountInvested: Number(i.amount) })),
    };
    try {
      if (id) await api.put('/vehicles/' + id, payload);
      else await api.post('/vehicles', payload);
      navigate('/vehicles');
    } catch (err) {
      setError(errorText(err));
      setSaving(false);
    }
  };

  return (
    <>
      <div className="page-head">
        <h1>{id ? 'Edit vehicle' : 'Add vehicle'}</h1>
        <Link className="link" to="/vehicles">Back to vehicles</Link>
      </div>

      <form className="form" onSubmit={submit}>
        {error && <p className="alert">{error}</p>}

        <div className="grid">
          <label>Wheels
            <select value={f.wheels} onChange={set('wheels')}>
              <option value="2">2</option>
              <option value="4">4</option>
              <option value="6">6</option>
            </select>
          </label>
          <label>Model no
            <input required maxLength={60} value={f.modelNo} onChange={set('modelNo')} />
          </label>
          <label>Model year
            <input required type="number" min="1900" max="2100" value={f.modelYear} onChange={set('modelYear')} />
          </label>
          <label>Number plate
            <input required maxLength={30} value={f.numberPlate} onChange={set('numberPlate')} />
          </label>
          <label>Purchase price
            <input required type="number" min="0.01" step="0.01" value={f.purchasePrice} onChange={set('purchasePrice')} />
          </label>
          <label>Purchase date
            <input required type="date" max={today()} value={f.purchaseDate} onChange={set('purchaseDate')} />
          </label>
        </div>

        <label>Notes
          <textarea rows={3} value={f.notes} onChange={set('notes')} />
        </label>

        <fieldset>
          <legend>Investors</legend>
          {investors.length === 0 && <p className="muted">No investors yet.</p>}
          {investors.map((row, index) => (
            <div className="inv-row" key={index}>
              <select value={row.userId} onChange={(e) => updateInvestor(index, { userId: e.target.value })}
                      aria-label="Investor">
                <option value="">Select user</option>
                {users.map((u) => (
                  <option key={u.id} value={u.id}>
                    {u.name} ({u.userTypeLabel}{u.active ? '' : ', inactive'})
                  </option>
                ))}
              </select>
              <input type="number" min="0.01" step="0.01" placeholder="Amount" value={row.amount}
                     onChange={(e) => updateInvestor(index, { amount: e.target.value })} aria-label="Amount invested" />
              <span className="share">{share(row.amount)}%</span>
              <button type="button" className="link danger"
                      onClick={() => setInvestors(investors.filter((_, i) => i !== index))}>Remove</button>
            </div>
          ))}
          <div className="inv-foot">
            <button type="button" className="btn ghost"
                    onClick={() => setInvestors([...investors, { userId: '', amount: '' }])}>Add investor</button>
            <span className={over ? 'over' : 'muted'}>
              Invested {money(total)} of {money(price)}
              {over ? ' — more than the purchase price' : ` — ${money(price - total)} left`}
            </span>
          </div>
        </fieldset>

        <div className="form-actions">
          <button className="btn" disabled={saving}>{saving ? 'Saving…' : 'Save vehicle'}</button>
          <Link className="btn ghost" to="/vehicles">Cancel</Link>
        </div>
      </form>
    </>
  );
}
