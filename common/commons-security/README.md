# Commons Security

Shared Spring Boot auto-configuration providing JWT authentication and Spring Security for all services in the Art Universe platform. Services opt-in by having a non-blank `AUTH_JWT_SECRET` environment variable.

## Key Components

### JwtSecretCondition

**Purpose**: Controls whether security is activated. The entire `CommonSecurityConfig` is conditional on this — if `auth.jwt.secret` is empty or not set, Spring Security does not activate for the service.

**Location**: `config/JwtSecretCondition.java`

**Details**: Uses `Condition` (not `@ConditionalOnProperty`) because `@ConditionalOnProperty` treats an empty string as "present". This allows services to run unsecured in test and local environments by simply omitting `AUTH_JWT_SECRET`.

---

### CommonSecurityConfig

**Purpose**: Auto-configuration that wires up the full JWT security stack: filter chain, JWT components, role hierarchy, method security, and CORS.

**Location**: `config/CommonSecurityConfig.java`

**What it configures**:
- Stateless session management
- `JwtAuthenticationFilter` before `UsernamePasswordAuthenticationFilter`
- Public paths from `auth.security.public-paths` (always includes `/health`, `/actuator/**`)
- CORS for allowed origins from `auth.security.allowed-origins`
- `@EnableMethodSecurity` for `@PreAuthorize` / `@PostAuthorize` annotations
- Role hierarchy (see below)

**Role Hierarchy**:
```
ADMIN > MASTER_CURATOR, LASTFM_CURATOR, SPOTIFY_CURATOR, QUIZ_MASTER, CONFIG_MANAGER, MAINTAINER
MAINTAINER > SURVEYOR
MASTER_CURATOR, LASTFM_CURATOR, SPOTIFY_CURATOR, QUIZ_MASTER, CONFIG_MANAGER, SURVEYOR > VIEWER
```

---

### JwtTokenProvider

**Purpose**: Creates and parses JWT tokens signed with HMAC-SHA256.

**Location**: `jwt/JwtTokenProvider.java`

**Token claims**: `sub` (user ID as string), `email`, `name`, `roles` (list of role name strings).

**Configuration properties** (`auth.jwt.*`):

| Property | Env Var | Default | Description |
|----------|---------|---------|-------------|
| `auth.jwt.secret` | `AUTH_JWT_SECRET` | — | HMAC-SHA256 signing key (min 32 chars) |
| `auth.jwt.expiration-minutes` | `AUTH_JWT_EXPIRATION_MINUTES` | 15 | Access token lifetime |
| `auth.jwt.issuer` | `AUTH_JWT_ISSUER` | `art-universe` | JWT `iss` claim |

---

### JwtAuthenticationFilter

**Purpose**: Servlet filter that extracts the `Authorization: Bearer <token>` header, validates the token via `JwtTokenProvider`, and populates the Spring Security context with a `UsernamePasswordAuthenticationToken`.

**Location**: `jwt/JwtAuthenticationFilter.java`

**Behavior**: On invalid or missing token, the filter passes through without setting the security context — downstream security rules then reject the request.

---

### UserPrincipal

**Purpose**: Spring `UserDetails` implementation representing an authenticated user. Carries `id`, `email`, `name`, and `Set<Role>` converted to Spring `GrantedAuthority` objects (`ROLE_<NAME>` prefix).

**Location**: `jwt/UserPrincipal.java`

---

### Role

**Purpose**: Enum of all application roles.

**Location**: `jwt/Role.java`

| Role | Description |
|------|-------------|
| `VIEWER` | Read-only access |
| `LASTFM_CURATOR` | Manages LastFM raw data |
| `SPOTIFY_CURATOR` | Manages Spotify raw data |
| `MASTER_CURATOR` | Manages master entities |
| `QUIZ_MASTER` | Manages quiz content |
| `CONFIG_MANAGER` | Manages system configuration |
| `MAINTAINER` | System maintenance operations |
| `SURVEYOR` | Monitoring and inspection |
| `ADMIN` | Full access (supersedes all roles) |

---

## Configuration Reference

Services using this module must include `env/docker/common/au-auth.env` and the environment-specific `au-auth.secrets.env` in their Docker configuration:

```yaml
# Required env vars for security to activate
AUTH_JWT_SECRET=<at-least-32-char-hmac-key>

# Optional (defaults shown)
AUTH_JWT_EXPIRATION_MINUTES=15
AUTH_JWT_ISSUER=art-universe

# Required for CORS
AU_AUTH_CORS_ALLOWED_ORIGINS=http://localhost:4000
# auth.security.public-paths — configured per-service in application.yml
```

When `AUTH_JWT_SECRET` is empty or absent, the module is a no-op — no security filter, no Spring Security beans.

---

## Usage in Consumer Services

Add the dependency:
```groovy
implementation project(':common:commons-security')
```

Configure public paths and CORS in `application.yml`:
```yaml
auth:
  security:
    public-paths: /health, /actuator/**, /api/v1/my-public-endpoint
    allowed-origins: ${AU_AUTH_CORS_ALLOWED_ORIGINS}
```

Protect individual endpoints with method security:
```java
@PreAuthorize("hasRole('MASTER_CURATOR')")
public ResponseEntity<Void> updateEntity(...) { ... }
```

Access the current user:
```java
@GetMapping("/me")
public UserInfo me(@AuthenticationPrincipal UserPrincipal principal) {
    return new UserInfo(principal.getId(), principal.getEmail(), ...);
}
```

---

## Testing

Services using this module in `@WebMvcTest` must exclude Spring Security auto-configuration from the test slice. This is handled by `BaseMvcTest`:

```java
@ImportAutoConfiguration(exclude = {
    SecurityAutoConfiguration.class,
    SecurityFilterAutoConfiguration.class,
    UserDetailsServiceAutoConfiguration.class
})
public abstract class BaseMvcTest extends BaseTest { ... }
```

See [Controller Testing Pattern](../docs/kb/patterns/backend/testing/testing-controllers.md).

---

## See Also

- [Auth Service README](auth/auth-service/README.md) — service that issues tokens consumed by this module
- [JWT Auth Pattern](../docs/kb/patterns/backend/security.md) — implementation pattern for adding auth to a new service
