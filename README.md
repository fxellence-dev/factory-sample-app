# Sample App SpringBoot

Simple Spring Boot REST app backed by PostgreSQL. CRUD on `Item` at `/api/items`.

## Run local (profile: local)

Needs Postgres reachable at `localhost:5432` (db `sampledb`, user `sampleuser`, pass `samplepass`).

```bash
docker compose up db -d          # just the db
mvn spring-boot:run              # uses application.yml default profile = local
```

## Run in Docker (profile: docker)

```bash
docker compose up --build
```

App on http://localhost:8080, db host inside compose network is `db`.

## Endpoints

- `GET    /api/items`
- `GET    /api/items/{id}`
- `POST   /api/items`      body: `{"name": "...", "description": "..."}`
- `PUT    /api/items/{id}`
- `DELETE /api/items/{id}`
- `GET    /actuator/health`
- `GET    /health/details`
