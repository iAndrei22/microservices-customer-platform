# Microservices Customer Platform

This repo now includes one root `docker-compose.yml` for the full stack, plus one `docker-compose.yml` per service and one `docker-compose-db.yml` per service database, based on the IntelliJ Docker run configurations stored in `.idea/workspace.xml`.

## Quick start

Create a local `.env` from the template and set your Stripe key:

```powershell
Copy-Item .env.example .env
```

Then edit `.env` and set `STRIPE_SECRET_KEY`.

Create the shared Docker network once (only needed for service-level compose files):

```powershell
docker network create internal
```

## Run everything

From the repo root, start the full stack in one command:

```powershell
docker compose up --build -d
```

To stop the full stack:

```powershell
docker compose down
```

## Run a service pair

Start the database container first, then the service container.

### Billing service

```powershell
docker compose -f billing-service\docker-compose-db.yml up -d
docker compose -f billing-service\docker-compose.yml up --build -d
```

### Customer service

```powershell
docker compose -f customer-service\docker-compose-db.yml up -d
docker compose -f customer-service\docker-compose.yml up --build -d
```

### Auth service

```powershell
docker compose -f auth-service\docker-compose-db.yml up -d
docker compose -f auth-service\docker-compose.yml up --build -d
```

### Analytics service

```powershell
docker compose -f analytics-service\docker-compose-db.yml up -d
docker compose -f analytics-service\docker-compose.yml up --build -d
```

### API gateway

```powershell
docker compose -f api-gateway\docker-compose.yml up --build -d
```

## Restart one container

```powershell
docker compose -f billing-service\docker-compose.yml restart
```

To restart the DB too:

```powershell
docker compose -f billing-service\docker-compose-db.yml restart
```

## Stop a service pair

```powershell
docker compose -f billing-service\docker-compose.yml down
docker compose -f billing-service\docker-compose-db.yml down
```

## Notes

- The compose files use the existing `internal` Docker network.
- Root `docker-compose.yml` reads Stripe secret from `.env` via `STRIPE_SECRET_KEY`.
- Database host ports come from the saved IntelliJ Docker configs:
  - `customer-service-db` -> `5000:5432`
  - `auth-service-db` -> `5001:5432`
  - `billing-service-db` -> `5002:5432`
  - `analytics-service-db` -> `5003:5432`

## Files added

- `billing-service/docker-compose.yml`
- `billing-service/docker-compose-db.yml`
- `customer-service/docker-compose.yml`
- `customer-service/docker-compose-db.yml`
- `auth-service/docker-compose.yml`
- `auth-service/docker-compose-db.yml`
- `analytics-service/docker-compose.yml`
- `analytics-service/docker-compose-db.yml`
- `api-gateway/docker-compose.yml`
- `docker-compose.yml`

## Where the configurations came from

The settings were recovered from `.idea/workspace.xml` under the `RunManager` component, where IntelliJ stores the Docker run configurations.


