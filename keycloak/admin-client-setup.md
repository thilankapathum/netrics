# Keycloak Admin Client Setup — Pulse User Lookup

The Pulse Settings "User-Area Mapping" page lists real users in a dropdown by
calling Keycloak's Admin REST API. That requires a dedicated confidential
client with a service account allowed to read the realm's users. This isn't
scripted anywhere in the repo (no realm export exists), so it must be created
once per environment via the Keycloak admin console or `kcadm.sh`.

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
