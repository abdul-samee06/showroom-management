# Car Showroom Management

One repository, two services: a Spring Boot 3 (Java 21) REST API in `backend/` and a React (Vite)
frontend in `frontend/`, with MySQL 8. Built from the project spec (`Car-Showroom-Management.md`):
showrooms are the customers, each has users (partners / investors), vehicles can have many investors,
and every screen and API call is protected by login + role-based permissions.

```
car-showroom/
├── docker-compose.yml   mysql + backend + frontend services
├── backend/             Spring Boot API (Maven), own README and Dockerfile
└── frontend/            React app (Vite), own README and Dockerfile
```

The services only talk over the REST API and can be built, run and deployed independently.

## Run it

**Everything in Docker** (needs Docker only):

```bash
docker compose up --build        # app on http://localhost:5173, API on http://localhost:8080
```

**Development** (needs JDK 21, Maven 3.9+, Node 18+, Docker for MySQL):

```bash
docker compose up -d mysql                  # 1. database
cd backend && mvn spring-boot:run           # 2. API on :8080 (Flyway creates tables and demo data)
cd frontend && npm install && npm run dev   # 3. UI on :5173 (proxies /api to :8080)
```

Set `JWT_SECRET` (32+ characters) outside development. Details: [backend/README.md](backend/README.md),
[frontend/README.md](frontend/README.md).

## Demo logins (showroom code `DEMO`)

| Email | Password | Role |
|-------|----------|------|
| `ali@example.com` | `Admin@123` | Admin (every permission) |
| `sara@example.com` | `Viewer@123` | Viewer (vehicles + users, read only) |

The demo data comes from migrations `V2` and `V4`: remove them before a real deployment.
