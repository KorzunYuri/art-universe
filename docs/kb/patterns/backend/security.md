# JWT Authentication Pattern

## Purpose

Adds stateless JWT-based authentication and role-based authorization to a Spring Boot service using the shared `commons-security` library. Security activates only when `AUTH_JWT_SECRET` is set — allowing the service to run unsecured in test and local environments without code changes.

---

## When to Use

- Adding a new REST API service that should require authenticated requests
- Protecting specific endpoints or methods with role checks
- Any service whose endpoints must not be publicly accessible in production

---

## How It Works

```
User (browser)
  │
  ├─ POST /api/v1/auth/login {googleIdToken}
  │     au-auth-service verifies with Google, issues JWT + refresh token
  │
  ├─ GET /api/v1/resource
  │     Authorization: Bearer <jwt>
  │     ↓
  │   JwtAuthenticationFilter (in commons-security)
  │     validates JWT signature + expiration
  │     sets SecurityContext → UserPrincipal{id, email, roles}
  │     ↓
  │   Spring Security filter chain
  │     checks path rules + @PreAuthorize annotations
  │
  └─ 401 response → frontend auto-refreshes token via POST /api/v1/auth/refresh
```

Token lifetime:
- **Access token** (JWT): 15 minutes, stateless — no DB lookup per request
- **Refresh token** (UUID stored in DB): 7 days, rotated on every use

---

## Implementation Steps

### Step 1: Add dependency

```groovy
// build.gradle
implementation project(':common:commons-security')
```

### Step 2: Import shared config and add optional profile override

```yaml
# application.yml — spring.config.import section
spring:
  config:
    import:
      - classpath:commons-security.yml
      - optional:classpath:commons-security-${spring.profiles.active}.yml
```

`commons-security.yml` (shipped in the `commons-security` jar) provides:
- Exclusion of Spring Boot's default `SecurityAutoConfiguration` and `ManagementWebSecurityAutoConfiguration` (prevents conflicting filter chains)
- Common `auth.jwt.*` defaults: `secret` (from `AUTH_JWT_SECRET` env var, empty = security disabled), `expiration-minutes` (15), `issuer` (`art-universe`)

### Step 3: Configure service-specific public paths and CORS

```yaml
# application.yml — only service-specific security config
auth:
  security:
    public-paths: /health, /actuator/**, /api/v1/my-public-endpoint
    allowed-origins: ${MY_SERVICE_CORS_ALLOWED_ORIGINS}
```

`/health` and `/actuator/**` are always in the public paths list. Add any other paths that must be unauthenticated (e.g., OAuth callback, internal endpoints). Do NOT define `auth.jwt.*` here — those come from `commons-security.yml`.

### Step 4: Add env files to Docker Compose

```yaml
# docker-compose service entry
env_file:
  - ../common/au-auth.env       # JWT config (expiration, issuer, schema)
  - ./au-auth.secrets.env       # AUTH_JWT_SECRET, AUTH_GOOGLE_CLIENT_ID
```

When `AUTH_JWT_SECRET` is empty, `CommonSecurityConfig` does not load — all requests pass through without authentication.

### Step 5: Protect endpoints (optional)

Role-based method security via `@PreAuthorize`:

```java
// Requires the MASTER_CURATOR role (or ADMIN, which inherits all roles)
@PreAuthorize("hasRole('MASTER_CURATOR')")
@PutMapping("/{id}")
public ResponseEntity<Void> update(@PathVariable Long id, ...) { ... }

// Multiple roles — any one is sufficient
@PreAuthorize("hasAnyRole('LASTFM_CURATOR', 'SPOTIFY_CURATOR', 'MASTER_CURATOR')")
@GetMapping
public ResponseEntity<List<Dto>> list() { ... }
```

Access the current user in controller methods:

```java
@GetMapping("/me")
public UserInfo me(@AuthenticationPrincipal UserPrincipal principal) {
    Long userId = principal.getId();
    String email = principal.getEmail();
    Set<Role> roles = principal.getRoles();
    ...
}
```

---

## Role Hierarchy

Roles defined in `Role.java` and hierarchy wired in `CommonSecurityConfig`:

```
ADMIN
  ├─ MASTER_CURATOR → VIEWER
  ├─ LASTFM_CURATOR → VIEWER
  ├─ SPOTIFY_CURATOR → VIEWER
  ├─ QUIZ_MASTER → VIEWER
  ├─ CONFIG_MANAGER → VIEWER
  └─ MAINTAINER → SURVEYOR → VIEWER
```

`hasRole('VIEWER')` is satisfied by any authenticated user with any role.

---

## Testing

Services using `@WebMvcTest` must exclude Spring Security auto-configuration. This is handled globally via `BaseMvcTest`:

```java
// commons-test-web: BaseMvcTest.java
@ImportAutoConfiguration(exclude = {
    SecurityAutoConfiguration.class,
    SecurityFilterAutoConfiguration.class,
    UserDetailsServiceAutoConfiguration.class
})
public abstract class BaseMvcTest extends BaseTest { ... }
```

All MVC test classes must extend `BaseMvcTest` (or a subclass of it) to inherit this exclusion. The exclusion propagates correctly through class hierarchy with `@ImportAutoConfiguration`, unlike `@AutoConfigureMockMvc(addFilters = false)` which is overridden by `@WebMvcTest`'s own meta-annotation.

See [Controller Testing Pattern](testing/testing-controllers.md) for the full testing approach.

---

## Environment Variable Reference

Provided by `commons-security.yml` (shared across all services):

| Variable | Required | Description |
|----------|----------|-------------|
| `AUTH_JWT_SECRET` | Yes (prod) | HMAC-SHA256 key, min 32 chars. Empty = security disabled |
| `AUTH_JWT_EXPIRATION_MINUTES` | No (default: 15) | Access token lifetime |
| `AUTH_JWT_ISSUER` | No (default: `art-universe`) | JWT `iss` claim |

Defined per service in `application.yml`:

| Variable | Required | Description |
|----------|----------|-------------|
| Service-specific CORS var | Yes (when security active) | Comma-separated allowed CORS origins, bound to `auth.security.allowed-origins` |

---

## Key Design Decisions

**Conditional activation via `JwtSecretCondition`**: Security is off by default (empty secret). This means test slices and dev environments work without any additional setup or mocking — no `@MockBean` for security components needed.

**Shared config via `commons-security.yml`**: All `auth.jwt.*` defaults and Spring Security auto-configuration exclusions live in `commons-security.yml` inside the `commons-security` jar. Services import it via `spring.config.import` and only define service-specific `auth.security.public-paths` and `auth.security.allowed-origins`. The `optional:classpath:commons-security-${spring.profiles.active}.yml` import allows per-profile overrides following the same convention as `commons-context` and `commons-observability`.

**Auto-configuration exclusion**: `commons-security.yml` excludes Spring Boot's `SecurityAutoConfiguration` and `ManagementWebSecurityAutoConfiguration`. Without this, the transitive `spring-boot-starter-security` dependency from `commons-security` activates default security in all consumer services — creating a catch-all filter chain that conflicts with `CommonSecurityConfig` (when active) or locks down all endpoints (when inactive).

**`@ImportAutoConfiguration(exclude)` for test isolation**: `@WebMvcTest` applies its own `@AutoConfigureMockMvc` via meta-annotation, which overrides the `addFilters = false` flag if set on a superclass. The `@ImportAutoConfiguration(exclude)` approach works through inheritance and is the correct pattern.

**Stateless tokens**: No server-side session, no Redis. The JWT carries all claims needed for authorization. The only DB state is the refresh token table in `au_auth`.

---

## Files

| File | Module | Purpose |
|------|--------|---------|
| `commons-security.yml` | `commons-security` | Shared config: auto-config exclusions + `auth.jwt.*` defaults |
| `CommonSecurityConfig.java` | `commons-security` | Auto-configuration wiring security stack |
| `JwtSecretCondition.java` | `commons-security` | Conditional: activates only when secret is non-blank |
| `JwtTokenProvider.java` | `commons-security` | Token generation and parsing |
| `JwtAuthenticationFilter.java` | `commons-security` | Servlet filter — validates Bearer token per request |
| `UserPrincipal.java` | `commons-security` | Spring UserDetails wrapping user ID, email, roles |
| `Role.java` | `commons-security` | Application roles enum |
| `BaseMvcTest.java` | `commons-test-web` | Excludes security auto-config in `@WebMvcTest` slices |
| `AuthController.java` | `auth-service` | Issues tokens (login, refresh, logout) |

---

## See Also

- [Commons Security README](../../../../common/commons-security/README.md) — library providing the JWT stack
- [Auth Service README](../../../../common/auth/auth-service/README.md) — service that issues tokens
- [Controller Testing Pattern](testing/testing-controllers.md) — full MVC test approach
