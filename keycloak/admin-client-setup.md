# Keycloak Admin Client Setup — Pulse User Lookup

The Pulse Settings "User-Area Mapping" page lists real users in a dropdown by
calling Keycloak's Admin REST API. That requires a dedicated confidential
client with a service account allowed to read the realm's users.

A realm export now exists at `keycloak/realm-export.json` (see
[Importing the realm export](#importing-the-realm-export) below) — importing
it into a fresh environment already creates `netrics-pulse-admin` with the
`view-users` role granted, so steps 1-3 below only apply if you're setting
this client up by hand instead (e.g. adding it after the fact, or the export
is out of date).

## Importing the realm export

`keycloak/realm-export.json` is a partial export (clients, roles, groups,
client scopes — **no real users**, just the auto-generated service-account
users tied to each client's `serviceAccountsEnabled`) pulled from the prod
`netrics` realm. Use it to provision a new environment instead of recreating
clients/roles/groups by hand.

Both `docker-compose.yml` and `docker-compose.dev.yml` now mount the export
into `/opt/keycloak/data/import/` and pass `--import-realm` on the `keycloak`
service's `command`. Keycloak imports on every container start but **skips
realms that already exist** (default `IGNORE_EXISTING` strategy) — so on an
environment where `netrics` is already provisioned (including prod), this is
a no-op; it only actually imports the first time a fresh Postgres DB is used
(e.g. `docker compose down -v` then back up, or a brand-new environment).

To force a re-import that overwrites the realm with what's in
`realm-export.json` (e.g. to push an updated export to an environment), add
`--override=true` to the same `command` list temporarily, restart the
container once, then remove it again.

**Existing running environment, without restarting the container:**

```bash
docker compose cp ./keycloak/realm-export.json keycloak:/tmp/realm-export.json
docker compose exec keycloak /opt/keycloak/bin/kc.sh import --file /tmp/realm-export.json
```

If the `netrics` realm already exists there, import overwrites matching
entities by ID — `pg_dump` the `keycloak` Postgres DB first if it's a real
environment.

If auto-import fails, or you'd rather not rely on it, see
[`manual-realm-setup.md`](./manual-realm-setup.md) for step-by-step manual
creation of every role, group, and client in the export.

**Required fixups after every import** (client secrets in the export are
masked as `**********`, not real credentials, and some URLs are prod-specific):

1. Admin console → realm `netrics` → **Clients** → for `netrics-beam`,
   `netrics-gateway`, `netrics-pulse`, `netrics-pulse-admin`, `postman-client`:
   **Credentials** tab → regenerate the secret, then update the matching
   `.env` values (`KEYCLOAK_CLIENT_SECRET`, `KEYCLOAK_ADMIN_CLIENT_SECRET`).
2. For `netrics-frontend` and `netrics-gateway`: update **Root URL** /
   **Redirect URIs** / **Web Origins** — the export has them hardcoded to the
   prod host (`172.22.41.77:8000`); point them at this environment's
   `${DOMAIN_IP}:${FRONTEND_PORT}`.
3. Restart `config-server`, `gateway`, `pulse`, `beam` so they pick up the
   new secrets, then verify login end-to-end.

## 1. Create the client

Admin console → your realm (`netrics` by default) → **Clients** → **Create client**:

1. **Client ID**: `netrics-pulse-admin` (matches the default in
   `config-server/.../pulse-service.yml` → `keycloak.admin.client-id`; use a
   different value only if you also override `KEYCLOAK_ADMIN_CLIENT_ID`).
2. **Client authentication**: On (confidential client).
3. **Authentication flow**: enable **Service accounts roles** only — leave
   "Standard flow" and "Direct access grants" off, this client is never used
   for interactive login.
4. Save.

## 2. Grant it permission to read users

On the new client → **Service account roles** tab → **Assign role**:

1. Filter by clients → select **realm-management**.
2. Assign the **view-users** role (query-users is enough too, but view-users
   is sufficient for listing).
3. Save.

## 3. Get the client secret

Client → **Credentials** tab → copy the **Client secret**.

## 4. Configure the app

Set these in your `.env` (see `.env.example`):

```
KEYCLOAK_ADMIN_CLIENT_ID=netrics-pulse-admin
KEYCLOAK_ADMIN_CLIENT_SECRET=<the secret from step 3>
```

These feed `keycloak.admin.client-id` / `keycloak.admin.client-secret` in
`backend/spring/config-server/src/main/resources/configurations/pulse-service.yml`,
consumed by `KeycloakAdminConfig` in the `pulse` service to build the admin
`Keycloak` client bean used by `UserService.getAllRealmUsers()`.

## 5. Verify

Restart `pulse` (and `config-server` if it caches config), then log in and
open Pulse Settings → User-Area Mapping. The "User" dropdown should list
every user in the realm. If it's empty or the page errors, check the pulse
service logs for a 401/403 from Keycloak — that means the client secret is
wrong or the `view-users` role wasn't granted.
