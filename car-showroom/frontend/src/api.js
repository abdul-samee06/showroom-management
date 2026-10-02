const BASE = '/api';

export const getShowroomId = () => localStorage.getItem('showroomId');
export const setShowroomId = (id) => localStorage.setItem('showroomId', String(id));

async function request(path, { method = 'GET', body } = {}) {
  const headers = { 'Content-Type': 'application/json' };
  const sid = getShowroomId();
  if (sid) headers['X-Showroom-Id'] = sid;

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
    err.fieldErrors = payload?.fieldErrors || {};
    throw err;
  }
  return res.status === 204 ? null : res.json();
}

export const api = {
  get: (p) => request(p),
  post: (p, body) => request(p, { method: 'POST', body }),
  put: (p, body) => request(p, { method: 'PUT', body }),
  del: (p) => request(p, { method: 'DELETE' }),
};

const nf = new Intl.NumberFormat('en-US', { maximumFractionDigits: 2 });
export const money = (n) => nf.format(Number(n ?? 0));

/** "name: message" lines from a backend validation error. */
export const errorText = (e) => {
  const fields = Object.entries(e.fieldErrors || {}).map(([k, v]) => `${k}: ${v}`);
  return fields.length ? `${e.message} ${fields.join('; ')}` : e.message;
};
