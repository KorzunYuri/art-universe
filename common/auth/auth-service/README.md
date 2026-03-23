# Auth Service (`au-auth-service`)

Standalone authentication and user management service for the Art Universe platform. Authenticates users via Google OAuth 2.0, issues short-lived JWT access tokens and long-lived rotating refresh tokens, and manages user roles.

## Responsibilities

- Verify Google ID tokens and create/update user accounts on first login
- Issue and rotate JWT access tokens and refresh tokens
- Expose admin API for user management (roles, enable/disable)
- Serve as the single source of truth for user identities

---

## API Endpoints

Base path: `/api/v1/auth`

### Public Endpoints (no JWT required)

| Method | Path | Purpose |
|--------|------|---------|
| `POST` | `/login` | Exchange Google ID token for JWT access + refresh tokens |
| `POST` | `/refresh` | Rotate refresh token and issue new access token |
| `POST` | `/logout` | Invalidate a refresh token |

#### POST `/login`
```json
// Request
{ "googleIdToken": "<token-from-google>" }

// Response
{
  "accessToken": "<jwt>",
  "refreshToken": "<uuid>",
  "user": { "id": 1, "email": "...", "name": "...", "pictureUrl": "...", "roles": ["VIEWER"] }
}
```

**Login flow**: Verify Google ID token → find user by `google_sub` → create with `VIEWER` role if first login → update profile (name, picture) → check `enabled=true` → issue tokens.

#### POST `/refresh`
```json
// Request
{ "refreshToken": "<uuid>" }
// Response: same shape as /login
```
Refresh token is **rotated** on every call — old token deleted, new one issued. Expired tokens return 401.

#### POST `/logout`
```json
{ "refreshToken": "<uuid>" }
```

---

### Authenticated Endpoints (JWT required)

| Method | Path | Role required | Purpose |
|--------|------|---------------|---------|
| `GET` | `/me` | any | Current user info from JWT principal |
| `GET` | `/users` | `ADMIN` | List all users (paginated) |
| `GET` | `/users/{id}` | `ADMIN` | Get user by ID |
| `PUT` | `/users/{id}/roles` | `ADMIN` | Update roles; invalidates all user's refresh tokens |
| `PUT` | `/users/{id}/enabled` | `ADMIN` | Enable/disable user; disabling invalidates refresh tokens |

---

## Key Classes

| Class | Location | Responsibility |
|-------|----------|----------------|
| `AuthController` | `controller/` | Public auth endpoints |
| `UserController` | `controller/` | Admin user management endpoints |
| `AuthService` | `service/` | Login, refresh, logout, token cleanup |
| `UserService` | `service/` | User listing, role/enabled updates |
| `GoogleTokenVerifier` | `service/` | Wraps Google API Client for ID token verification |
| `AuthUser` | `entity/` | JPA entity for `au_auth.users` table |
| `RefreshToken` | `entity/` | JPA entity for `au_auth.refresh_tokens` table |
| `RoleArrayConverter` | `entity/` | Converts `Set<Role>` ↔ PostgreSQL `text[]` |

---

## Database Schema

Schema: `au_auth` — owned by `au_auth_dm` user.

```sql
users (
  id              BIGSERIAL PRIMARY KEY,
  email           VARCHAR(255) NOT NULL UNIQUE,
  name            VARCHAR(255) NOT NULL,
  picture_url     VARCHAR(1024),
  google_sub      VARCHAR(255) NOT NULL UNIQUE,  -- Google subject identifier
  roles           TEXT[] NOT NULL DEFAULT '{VIEWER}',
  enabled         BOOLEAN NOT NULL DEFAULT TRUE,
  created_at      TIMESTAMP NOT NULL,
  updated_at      TIMESTAMP NOT NULL,
  last_login_at   TIMESTAMP
)

refresh_tokens (
  id          BIGSERIAL PRIMARY KEY,
  user_id     BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  token       VARCHAR(512) NOT NULL UNIQUE,       -- UUID
  expires_at  TIMESTAMP NOT NULL,
  created_at  TIMESTAMP NOT NULL
)
-- Indexes on: token, user_id, expires_at
```

Migrations live in `common:auth:auth-service-liquibase-resources` and are applied by `common:auth:auth-liquibase-service` on startup.

---

## Configuration

```yaml
# application.yml
auth:
  jwt:
    secret: ${AUTH_JWT_SECRET:}          # Empty = security disabled (dev/test)
    expiration-minutes: ${AUTH_JWT_EXPIRATION_MINUTES:15}
    issuer: ${AUTH_JWT_ISSUER:art-universe}
  security:
    public-paths: /health, /actuator/**, /api/v1/auth/login, /api/v1/auth/refresh
    allowed-origins: ${AU_AUTH_CORS_ALLOWED_ORIGINS}
  google:
    client-id: ${AUTH_GOOGLE_CLIENT_ID:}
  refresh-token:
    expiration-days: ${AUTH_REFRESH_TOKEN_EXPIRATION_DAYS:7}
```

### Environment Variables

| Variable | Source file | Description |
|----------|-------------|-------------|
| `AU_DB_MASTER_HOST` / `AU_DB_MASTER_PORT` / `AU_DB_NAME` | `common.env` | Database connection |
| `AU_AUTH_DB_SCHEMA` | `au-auth.env` | PostgreSQL schema (`au_auth`) |
| `AU_AUTH_DB_USERNAME` | `au-auth.env` | DB user (`au_auth_dm`) |
| `AU_AUTH_DB_PASSWORD_DM` | `database.secrets.env` | DB password |
| `AUTH_JWT_SECRET` | `au-auth.secrets.env` | HMAC-SHA256 signing key (min 32 chars) |
| `AUTH_GOOGLE_CLIENT_ID` | `au-auth.secrets.env` | Google OAuth Client ID |
| `AU_AUTH_CORS_ALLOWED_ORIGINS` | `au-auth.env` | Comma-separated allowed origins |
| `AUTH_JWT_EXPIRATION_MINUTES` | `au-auth.env` | Access token lifetime (default: 15) |
| `AUTH_REFRESH_TOKEN_EXPIRATION_DAYS` | `au-auth.env` | Refresh token lifetime (default: 7) |
| `AU_AUTH_APP_INTERNAL_PORT` | `au-auth.env` | Service port |

---

## Dependencies

- `common:commons-security` — JWT filter, `UserPrincipal`, role hierarchy
- `common:commons-web` — Exception handling, Jackson config
- `com.google.api-client:google-api-client:2.7.2` — Google ID token verification

---

## Docker

**Image**: `au-auth-service:latest`
**Liquibase image**: `au-auth-liquibase:latest` (runs migrations, exits)

The service must start after `au-auth-liquibase` completes successfully:
```yaml
depends_on:
  au-auth-liquibase:
    condition: service_completed_successfully
```

---

## See Also

- [Commons Security README](../../commons-security/README.md) — JWT filter used by all consumer services
- [JWT Auth Pattern](../../../docs/kb/patterns/backend/security.md) — KB pattern for adding security to a new service
- [Auth DB Changeset](../../../env/docker/common/db/initdb/changesets/0013-au-auth.sql) — Schema and user initialization
