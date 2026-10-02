// In development Vite proxies /api to the backend. For a deployed build set VITE_API_URL
// (e.g. https://api.example.com/api) when running `npm run build`.
const BASE = import.meta.env.VITE_API_URL ?? '/api';
const TOKEN_KEY = 'token';

export const getToken = () => localStorage.getItem(TOKEN_KEY);
export const setToken = (t) => (t ? localStorage.setItem(TOKEN_KEY, t) : localStorage.removeItem(TOKEN_KEY));

async function request(path, { method = 'GET', body } = {}) {
  const headers = { 'Content-Type': 'application/json' };
  const token = getToken();
  if (token) headers.Authorization = `Bearer ${token}`;

  const res = await fetch(BASE + path, {
    method,
    headers,
    body: body === undefined ? undefined : JSON.stringify(body),
  });

  if (!res.ok) {
    let payload = null;
    try {
      payload = await res.json();
    } catch {
      /* no JSON body */
    }
    const err = new Error(payload?.message || `Request failed (${res.status})`);
    err.status = res.status;
    err.fieldErrors = payload?.fieldErrors || {};
    // The session is handled centrally (see AuthProvider): 401 -> /login, 403 -> /forbidden.
    if (res.status === 401 && path !== '/auth/login') window.dispatchEvent(new Event('auth:expired'));
    if (res.status === 403) window.dispatchEvent(new Event('auth:forbidden'));
    throw err;
  }
  return res.status === 204 ? null : res.json();
}

export const api = {
  get: (p) => request(p),
  post: (p, body) => request(p, { method: 'POST', body }),
  put: (p, body) => request(p, { method: 'PUT', body }),
  patch: (p, body) => request(p, { method: 'PATCH', body }),
  del: (p) => request(p, { method: 'DELETE' }),
};

const nf = new Intl.NumberFormat('en-US', { maximumFractionDigits: 2 });
export const money = (n) => nf.format(Number(n ?? 0));

/** "name: message" lines from a backend validation error. */
export const errorText = (e) => {
  const fields = Object.entries(e.fieldErrors || {}).map(([k, v]) => `${k}: ${v}`);
  return fields.length ? `${e.message} ${fields.join('; ')}` : e.message;
};
