# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this is

Netrics is a telecom network-monitoring platform (RAN KPIs, alarms, anomaly detection, coverage mapping) built as Spring Cloud microservices behind a gateway, with an Angular frontend and standalone Python data-ingestion scripts. Everything is orchestrated via Docker Compose and secured with Keycloak.

## Repository layout

```
backend/
  spring/
    discovery/      Eureka service registry
    config-server/   Spring Cloud Config server; per-service YAML lives in
                      config-server/src/main/resources/configurations/*.yml
    gateway/          Spring Cloud Gateway (WebFlux) — single entry point, OAuth2/Keycloak login,
                      routes /api/v1/pulse/** -> pulse-service, /api/v1/beam/** -> beam-service
    pulse/            Main domain service: KPIs, cells/sites/sectors, alarms, anomaly detection,
                      worst-cell dashboards, area/district mapping
    beam/             Coverage/topology domain service (sites, sectors, bands, geometry)
  python/pulse/       Standalone KPI ingestion & processing scripts (not a packaged app), plus
                      alarms/ (alarm-puller + alarm ingest), day-average / hour KPI processors
frontend/netrics/      Angular 20 SPA
keycloak/               Realm themes/providers mounted into the Keycloak container
nginx/                  Reverse proxy config
postgres/, init.sql/    DB init
docker-compose.yml, docker-compose-local.yml   Full-stack orchestration (prod-ish vs local)
```

Each Spring service uses Java package root `dev.thilanka.netrics` (pulse) / `dev.thilanka.beam` (beam) / `dev.thilanka.gateway` / `dev.thilanka.discovery` / `dev.thilankaw.configserver`.

## Architecture

- **Service discovery & config**: `discovery` (Eureka) and `config-server` (Spring Cloud Config) must be up before other Spring services register/fetch config. Each service's `application.yml` just points at the config server (`spring.config.import: optional:configserver:...`); actual routes, CORS, OAuth2 client registration, DB pools, etc. live centrally in `config-server/src/main/resources/configurations/<service>-service.yml`.
- **Gateway**: single ingress on port 8001 (actuator on 9001). Routes by path prefix to `pulse-service` / `beam-service` via Eureka (`lb://...`). Handles the OAuth2 login flow (authorization_code) against Keycloak; downstream services validate JWTs as resource servers (same Keycloak realm `netrics`).
- **pulse** (port 8012 / actuator 9012) and **beam** (port 8013 / actuator 9013): standard layered Spring Boot services — `controller` → `service` (+ `service/impl` in beam) → `repository` (Spring Data JPA) → `entity`, with `dto` + `mapper` for the controller-facing shape. Endpoints are versioned under `/api/v1/pulse/...` and `/api/v1/beam/...`, secured per-endpoint with `@PreAuthorize("hasAuthority('ROLE_PULSE_*')")`-style checks. DB access is PostgreSQL (TimescaleDB + PostGIS extensions) with Flyway migrations in `src/main/resources/db/migration` (`V<n>__description.sql`, strictly sequential per service). One-off/backfill SQL that isn't a migration lives in `src/main/resources/sql-files/`.
- **Redis** is used for caching (`CacheWarmup`/`CacheWarmupAsyncService` in pulse) and **Kafka** for cross-service events (`PulseEventPublisher` / `BeamSyncConsumer` — beam-side changes sync into pulse, e.g. site/sector/band migrations reflected via `sql-files/*_migration_to_beam.sql`).
- **Python pulse pipeline** (`backend/python/pulse`): independent scripts/containers that read KPI export files (per RAT: gsm/ltefdd/ltetdd/nr/umts, under `files/day-average`, `files/hour`, `files/busy-hour`) and write into the pulse Postgres DB — these are the `kpip-day` / `kpip-hour` containers in compose. `alarms/` is a separate alarm ingestion pipeline (`alarm-puller` pulls OSS alarm files, `alarm_ingest.py` loads them).
- **Frontend** (`frontend/netrics`, Angular 20 + standalone components): `app/layout` holds the shell (navbar/sidebar/content), `app/layout/content/pages/{pulse,beam,surge,home,unauthorized}` are the routed feature areas. `app/service/pulse/*-service.ts` are thin HTTP clients (one per backend controller) that build URLs off `UrlService` (`environment.baseUrl` + `/api/v1/pulse` or `/api/v1/beam`), matching `app/models/pulse/*Dto.ts` to the backend DTOs 1:1. Auth is Keycloak via `keycloak-angular`/`keycloak-js` (`app/auth/`: config, guards, interceptor). All requests go through the gateway, not directly to pulse/beam.
- **Networking**: nginx terminates on `FRONTEND_PORT` (compose: 8000) and fronts both the Angular app and Keycloak (`/auth`); the gateway and internal services sit on a `private_net` Docker network not exposed to the host.

## Running locally

Full stack (recommended for integration work) via Docker Compose — copy `.env.example` to `.env` and fill in secrets first:

```bash
docker compose -f docker-compose-local.yml up -d
```

Bring-up order matters and is encoded in `depends_on`/healthchecks: postgres/redis/kafka/keycloak → config-server → discovery → pulse/beam → gateway/nginx → frontend.

Individual Spring service (from its directory, e.g. `backend/spring/pulse`):

```bash
./mvnw spring-boot:run                # run
./mvnw clean package -DskipTests      # build jar (matches Dockerfile build stage)
./mvnw test                           # run tests for this service
./mvnw test -Dtest=ClassName#method   # run a single test
```

Frontend (from `frontend/netrics`):

```bash
npm install
npm start
npm run build
npm test         # Karma/Jasmine
```

Note: when hitting the API from `ng serve` directly (bypassing nginx), the allowed CORS origins are configured centrally in `config-server/.../gateway-service.yml`, not in the gateway service itself.

## Conventions to follow when adding code

- New pulse/beam REST endpoints: add DTO → mapper → service (+ impl for beam) → controller, following the existing `BandController`/`BandService` pattern; guard endpoints with `@PreAuthorize` using the existing `ROLE_PULSE_*`/`ROLE_BEAM_*` authority naming.
- New pulse/beam frontend service: one `*-service.ts` per backend controller under `app/service/pulse/`, built on `UrlService`, paired with a `*Dto.ts` model matching the backend DTO field-for-field.
- Schema changes: add a new sequential Flyway migration (`V<next>__...sql`) in the relevant service's `db/migration`; never edit an already-applied migration.
- Config for a service (routes, CORS, client secrets, pool sizes, etc.) belongs in `config-server/src/main/resources/configurations/<service>-service.yml`, not hardcoded in the service.
