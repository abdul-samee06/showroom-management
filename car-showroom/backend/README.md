# Car Showroom Management - Backend

Spring Boot 3 (Java 21) REST API with JWT login, role-based permissions and MySQL 8.
The React UI is the `frontend/` service of the same repository.

## Run it

You need JDK 21, Maven 3.9+ and Docker (or your own MySQL 8).

```bash
docker compose up -d mysql  # run from the repo root: MySQL 8 (schema car_showroom, user root / password root)
mvn spring-boot:run         # API on http://localhost:8080, Flyway creates tables and demo data
```

## Configuration (environment variables)

| Variable | Default | Purpose |
|----------|---------|---------|
| `DB_URL`, `DB_USER`, `DB_PASSWORD` | local MySQL, `root` / `root` | MySQL connection |
| `JWT_SECRET` | dev value | Token signing key, 32+ characters. **Set it outside development.** |
| `CORS_ORIGINS` | `http://localhost:5173` | Frontend origin(s), comma separated (not needed behind the nginx proxy) |

Tokens last 8 hours (`app.jwt.expiry-minutes`).

## Demo logins (showroom code `DEMO`)

| Email | Password | Role |
|-------|----------|------|
| `ali@example.com` | `Admin@123` | Admin (every permission) |
| `sara@example.com` | `Viewer@123` | Viewer (vehicles + users, read only) |

## API

Everything except `POST /api/auth/login` and `POST /api/showrooms` needs `Authorization: Bearer <token>`.
The showroom comes from the token, never from the client.

| Method | Path | Permission |
|--------|------|------------|
| POST | `/api/auth/login`, GET `/api/auth/me`, POST `/api/auth/logout` | public / logged in |
| POST | `/api/showrooms` | public onboarding: creates showroom + Admin role + first admin user |
| GET | `/api/user-types` | logged in |
| GET | `/api/vehicles` (`?q=&wheels=&year=&investorId=&sortBy=&dir=&page=&size=`), `/api/vehicles/{id}` | `VEHICLE_VIEW` (`{id}` also `VEHICLE_EDIT`) |
| GET | `/api/vehicles/investor-options` | `VEHICLE_VIEW`, `VEHICLE_CREATE` or `VEHICLE_EDIT` |
| POST / PUT / DELETE | `/api/vehicles`, `/api/vehicles/{id}` | `VEHICLE_CREATE` / `VEHICLE_EDIT` / `VEHICLE_DELETE` |
| GET | `/api/users` (`?type=&roleId=`), `/api/users/{id}`, `/api/users/{id}/vehicles` | `USER_VIEW` (`{id}` also `USER_EDIT`) |
| POST / PUT | `/api/users`, `/api/users/{id}` | `USER_CREATE` / `USER_EDIT` (`roleIds` in the body also needs `USER_ASSIGN_ROLE`) |
| PATCH | `/api/users/{id}/status` | `USER_DEACTIVATE` |
| GET | `/api/roles`, `/api/roles/{id}`, `/api/permissions` | `ROLE_VIEW` |
| GET | `/api/roles/options` | `ROLE_VIEW`, `USER_VIEW` or `USER_ASSIGN_ROLE` |
| POST / PUT / DELETE | `/api/roles`, `/api/roles/{id}` | `ROLE_MANAGE` |

Business rules: wheels 2/4/6; number plate unique per showroom (upper-case); total invested cannot exceed
the purchase price; an investor appears once per vehicle and must belong to the same showroom; role names are
unique per showroom; the Admin role cannot be edited or deleted; a role in use cannot be deleted; the last
active admin cannot be deactivated or lose the Admin role.

## Code layout

`com.showroom.dto` holds one class per request/response. Other packages: `security` (JWT, filter, `AuthUser`),
`config`, `domain`, `repo`, `service`, `web`.

## Changing the database

Schema lives in `src/main/resources/db/migration`. Never edit an applied migration: add `V5__your_change.sql`.
Update the matching entity in `domain` and the DTOs in `dto` too. New screen with its own rule: insert a row in
`permissions`, add `@PreAuthorize` to its endpoints, and add it to every showroom's Admin role. `V2` and `V4`
are demo data: remove them before a real deployment.

## Known limits

- Permissions are read when the token is issued, so role changes apply on next login; a deactivated user's token stays valid until it expires.
- `POST /api/showrooms` is public for onboarding: protect or remove it before exposing the API.
- Soft-deleted vehicles, users and roles still hold their plate / email / name.
- No default role for new users, password reset or login lockout yet.
