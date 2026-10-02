# Car Showroom Management - Frontend

React 18 + Vite + React Router. It talks to the REST API in the repository's `backend/` service.

## Run it

You need Node 18+ and the backend running on http://localhost:8080.

```bash
npm install
npm run dev        # http://localhost:5173, /api is proxied to the backend
```

Backend somewhere else? `BACKEND_URL=http://host:8080 npm run dev`.

## Build for deployment

```bash
VITE_API_URL=https://api.example.com/api npm run build   # output in dist/
```

The Docker image (`Dockerfile`, `nginx.conf`) serves `dist/` and proxies `/api` to the `backend` service, so it needs no `VITE_API_URL`. If you host `dist/` elsewhere, set the backend's `CORS_ORIGINS` to the origin that serves `dist/`.

## Screens

| Route | Screen | Permission |
|-------|--------|------------|
| `/login` | Login (showroom code, email, password) | public |
| `/forbidden` | 403 screen | public |
| `/vehicles`, `/vehicles/new`, `/vehicles/:id/edit` | Vehicles list and form | `VEHICLE_VIEW` / `VEHICLE_CREATE` / `VEHICLE_EDIT` |
| `/users`, `/users/new`, `/users/:id/edit` | Users list and form (Roles field needs `USER_ASSIGN_ROLE`) | `USER_VIEW` / `USER_CREATE` / `USER_EDIT` |
| `/roles`, `/roles/new`, `/roles/:id/edit` | Roles and permissions | `ROLE_VIEW` / `ROLE_MANAGE` |

Routes use `<ProtectedRoute permission="...">`; buttons use `hasPermission(code)` from `src/auth.jsx`.
The backend repeats every check, so hiding a screen is only a convenience. No token redirects to `/login`;
a 401 clears the session, a 403 opens `/forbidden`.

Demo logins (showroom code `DEMO`): `ali@example.com` / `Admin@123` (Admin), `sara@example.com` / `Viewer@123` (Viewer).
