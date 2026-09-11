# Manual Keycloak Realm Setup (netrics)

Fallback guide for provisioning the `netrics` realm's roles, groups, and
clients by hand — use this if `keycloak/realm-export.json` auto-import
(see `admin-client-setup.md`) fails, or you'd rather build the realm up
manually. This mirrors exactly what's in the realm export as of the last
prod export.

Client scopes (`profile`, `roles`, `email`, `acr`, `web-origins`, etc.) and
the built-in clients (`account`, `account-console`, `admin-cli`, `broker`,
`realm-management`, `security-admin-console`) are **not** covered here —
Keycloak creates all of those automatically for every new realm, nothing to
do manually.

## 1. Create the realm

Admin console → realm dropdown → **Create realm** → name it `netrics`
(must match `REALM_NAME` in `.env`) → **Create**.

## 2. Create realm roles

Realm `netrics` → **Realm roles** → **Create role**, one at a time, name
only (leave description blank):

| Role name |
|---|
| `BEAM_CREATE` |
| `BEAM_READ` |
| `BEAM_UPDATE` |
| `BEAM_DELETE` |
| `PULSE_CREATE` |
| `PULSE_READ` |
| `PULSE_UPDATE` |
| `PULSE_DELETE` |

These map directly to the `@PreAuthorize("hasAuthority('ROLE_PULSE_*')")` /
`ROLE_BEAM_*` checks in the pulse/beam controllers (Spring's JWT converter
prefixes realm roles with `ROLE_`).

`default-roles-netrics`, `offline_access`, `uma_authorization` already exist
by default — don't recreate them.

## 3. Create groups

Realm `netrics` → **Groups** → **Create group**, then for each group open
it → **Role mapping** → **Assign role** → filter "Filter by realm roles" →
select the roles listed:

| Group | Realm roles assigned |
|---|---|
| `Mobitel_Admin` | `BEAM_CREATE`, `BEAM_READ`, `BEAM_UPDATE`, `BEAM_DELETE`, `PULSE_CREATE`, `PULSE_READ`, `PULSE_UPDATE`, `PULSE_DELETE` (all 8) |
| `Mobitel_Manager` | `PULSE_CREATE`, `PULSE_READ`, `PULSE_UPDATE` |
| `Mobitel_Engineer` | `PULSE_READ`, `PULSE_UPDATE` |
| `Mobitel_TO` | `PULSE_READ` |

No sub-groups. Add real users to these groups afterward (Users → select
user → **Groups** tab → **Join group**) — user membership isn't part of
this export/import by design.

## 4. Create clients

For every client below: **Clients** → **Create client**, set **Client ID**
+ **Name** on the General Settings step, then the flags on Capability
config, then the URLs/origins on Login settings step. Values not listed
(e.g. `Client SAML endpoint`) are left at Keycloak defaults. After saving a
confidential client, go to its **Credentials** tab to generate/copy the
secret and put it in `.env`.

Everywhere below, `<DOMAIN_IP>` / `<FRONTEND_PORT>` mean the values from
this environment's `.env` (`DOMAIN_IP`, `FRONTEND_PORT`), not the literal
strings.

### netrics-frontend (public, Angular SPA login)
- **Client authentication**: Off (public)
- **Authentication flow**: Standard flow only
- **Root URL / Home URL**: `http://<DOMAIN_IP>:<FRONTEND_PORT>`
- **Valid redirect URIs**: `http://<DOMAIN_IP>:<FRONTEND_PORT>/*`
- **Valid post logout redirect URIs**: same as redirect URIs
- **Web origins**: `http://<DOMAIN_IP>:<FRONTEND_PORT>`
- **Advanced** tab → Proof Key for Code Exchange Code Challenge Method: `S256`

### netrics-gateway (confidential, gateway's own OAuth2 login + service account)
- **Client authentication**: On
- **Authentication flow**: Standard flow + Direct access grants + Service accounts roles, all on
- **Root URL**: `http://<DOMAIN_IP>:<FRONTEND_PORT>`
- **Valid redirect URIs**: `http://<DOMAIN_IP>:<FRONTEND_PORT>/*`
- **Valid post logout redirect URIs**: same
- **Web origins**: `http://<DOMAIN_IP>:<FRONTEND_PORT>`
- Copy secret → `.env` `KEYCLOAK_CLIENT_SECRET` (consumed by gateway's OAuth2 client registration, `config-server/.../gateway-service.yml`)

### netrics-pulse (confidential, resource server + service account)
- **Client authentication**: On
- **Authentication flow**: Direct access grants + Service accounts roles on; Standard flow **off**
- **Valid redirect URIs**: `/*`
- **Web origins**: `/*`
- Copy secret → used as the pulse service's own client secret if configured (check `pulse-service.yml`)

### netrics-beam (confidential, resource server + service account)
- Same capability config as `netrics-pulse`: Direct access grants + Service accounts on, Standard flow off
- **Valid redirect URIs**: `/*`
- **Web origins**: `/*`

### netrics-pulse-admin (confidential, admin-API service account only — no login)
- **Client authentication**: On
- **Authentication flow**: **Only** Service accounts roles on; Standard flow and Direct access grants off
- **Valid redirect URIs**: `/*`
- After saving, go to **Service accounts roles** tab → **Assign role** → filter by clients → `realm-management` → assign `view-users`
- Copy secret → `.env` `KEYCLOAK_ADMIN_CLIENT_ID=netrics-pulse-admin`, `KEYCLOAK_ADMIN_CLIENT_SECRET=<secret>` (see steps 1-5 in this same directory's `admin-client-setup.md` for what consumes this)

### postman-client (confidential, for manual/Postman OAuth2 testing — optional)
- **Client authentication**: On
- **Authentication flow**: Standard flow + Direct access grants on
- **Valid redirect URIs**: `https://oauth.pstmn.io/v1/browser-callback`, `https://oauth.pstmn.io/v1/callback`
- **Web origins**: `https://oauth.pstmn.io`
- Skip this client entirely if you don't use Postman against this environment.

## 5. Protocol mappers (usually automatic — verify only if tokens look wrong)

`netrics-beam`, `netrics-gateway`, and `netrics-pulse` each carry three
extra `oidc-usersessionmodel-note-mapper` protocol mappers (**Client ID**,
**Client IP Address**, **Client Host** — session-note → token claim). Modern
Keycloak versions add these automatically once a client has service accounts
enabled. If a token is missing `client_id`/`clientAddress`/`clientHost`
claims, add them manually: client → **Client scopes** → `<clientId>-dedicated`
→ **Add mapper** → **By configuration** → **User Session Note**, with
`User Session Note` = `client_id` / `clientAddress` / `clientHost` and
`Token Claim Name` matching, all three token-claim toggles on.

## 6. Wire the app up

Once clients exist and secrets are copied into `.env`, restart
`config-server`, `gateway`, `pulse`, `beam` so they pick up the new client
IDs/secrets, then verify:

1. Log in through the frontend (`netrics-frontend` public client + gateway's
   `netrics-gateway` OAuth2 login) — should reach the app past Keycloak's
   login page.
2. A logged-in user in `Mobitel_Admin`/etc. sees pulse/beam data — confirms
   realm role → group → `@PreAuthorize` chain works.
3. Pulse Settings → User-Area Mapping dropdown populates — confirms
   `netrics-pulse-admin`'s `view-users` service account role works (see
   `admin-client-setup.md`).
