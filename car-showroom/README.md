# Car Showroom Management

Spring Boot 3 (Java 21) REST API + React (Vite) frontend + MySQL 8.
Built from the project spec: showrooms are the customers, each showroom has users
(partners / investors), and vehicles can have many investors.

```
car-showroom/
├── docker-compose.yml     MySQL 8.0 for local development
├── backend/               Spring Boot API (Maven)
└── frontend/              React app (Vite)
```

## Run it

You need JDK 21, Maven 3.9+, Node 18+ and Docker (or your own MySQL 8).

```bash
# 1. database (creates schema "car_showroom", user root / password root)
docker compose up -d

# 2. backend on http://localhost:8080 (Flyway creates tables and demo data on first start)
cd backend
mvn spring-boot:run

# 3. frontend on http://localhost:5173 (proxies /api to the backend)
cd frontend
npm install
npm run dev
```

Use your own MySQL: `DB_USER=... DB_PASSWORD=... mvn spring-boot:run`, and edit the
URL in `backend/src/main/resources/application.yml`.

## Screens

| Route | Screen |
|-------|--------|
| `/vehicles` | Vehicle list: wheels, model no, year, number plate, price, date, investors (with % share). Search, wheels filter, sortable columns, paging. |
| `/vehicles/new`, `/vehicles/:id/edit` | Add / edit vehicle with an investors section (user + amount, share % and remaining amount shown live). |
| `/users` | Users with type (Partner / Investor), add / edit / delete, filter by type, and "Vehicles" to see a user's investments. |

The showroom switcher in the top bar picks which showroom's data you see.

## API

All endpoints except `/api/showrooms` and `/api/user-types` need the header `X-Showroom-Id`.

| Method | Path | Notes |
|--------|------|-------|
| GET / POST | `/api/showrooms` | list / create showroom |
| GET | `/api/user-types` | partner, investor, ... |
| GET / POST | `/api/users` | `?type=investor` to filter |
| PUT / DELETE | `/api/users/{id}` | delete is a soft delete, blocked while the user has investments |
| GET | `/api/users/{id}/vehicles` | vehicles the user is invested in |
| GET / POST | `/api/vehicles` | `?q=&wheels=&sortBy=&dir=&page=&size=` |
| GET / PUT / DELETE | `/api/vehicles/{id}` | delete is a soft delete |

Business rules enforced by the API: wheels must be 2, 4 or 6; number plate is unique per
showroom (stored upper-case); total invested cannot exceed the purchase price; an investor
can appear only once per vehicle; investors must belong to the same showroom.

## Changing the database later

The schema lives in `backend/src/main/resources/db/migration`. Never edit an applied
migration: add `V3__your_change.sql` and restart. Hibernate is set to `ddl-auto: none`,
so update the matching entity in `com.showroom.domain` and the DTOs in `Dtos.java` as well.

- New user type: `INSERT INTO user_types (code, label) VALUES ('manager', 'Manager');` in a new migration. The UI picks it up automatically.
- `V2__demo_data.sql` is sample data: remove it before a real deployment.

## Not included yet

- **Login / authentication.** The showroom is chosen with the `X-Showroom-Id` header, so anyone who can reach the API can read every showroom. Add Spring Security + JWT (users already have a `password_hash` column) before exposing it.
- Soft-deleted vehicles and users still hold their number plate / email, so those values cannot be reused in the same showroom.
