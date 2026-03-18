package yurykorzun.art.universe.common.security.jwt;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import yurykorzun.art.universe.common.security.model.Role;
import yurykorzun.art.universe.common.security.model.UserPrincipal;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenProviderTest {

    private JwtTokenProvider tokenProvider;

    @BeforeEach
    void setUp() {
        JwtProperties properties = new JwtProperties();
        properties.setSecret("test-secret-key-that-is-at-least-32-characters-long-for-hs256");
        properties.setExpirationMinutes(15);
        properties.setIssuer("art-universe");
        tokenProvider = new JwtTokenProvider(properties);
    }

    @Test
    void generateAndParseToken() {
        UserPrincipal principal = new UserPrincipal(
            42L, "test@example.com", "Test User", Set.of(Role.VIEWER, Role.MASTER_CURATOR));

        String token = tokenProvider.generateToken(principal);
        assertNotNull(token);

        UserPrincipal parsed = tokenProvider.parseToken(token);
        assertEquals(42L, parsed.getId());
        assertEquals("test@example.com", parsed.getEmail());
        assertEquals("Test User", parsed.getName());
        assertTrue(parsed.getRoles().contains(Role.VIEWER));
        assertTrue(parsed.getRoles().contains(Role.MASTER_CURATOR));
    }

    @Test
    void validateToken_valid() {
        UserPrincipal principal = new UserPrincipal(
            1L, "user@test.com", "User", Set.of(Role.VIEWER));

        String token = tokenProvider.generateToken(principal);
        assertTrue(tokenProvider.validateToken(token));
    }

    @Test
    void validateToken_invalidSignature() {
        JwtProperties otherProps = new JwtProperties();
        otherProps.setSecret("different-secret-key-that-is-at-least-32-characters-long-too");
        otherProps.setExpirationMinutes(15);
        otherProps.setIssuer("art-universe");
        JwtTokenProvider otherProvider = new JwtTokenProvider(otherProps);

        UserPrincipal principal = new UserPrincipal(
            1L, "user@test.com", "User", Set.of(Role.VIEWER));

        String token = otherProvider.generateToken(principal);
        assertFalse(tokenProvider.validateToken(token));
    }

    @Test
    void validateToken_garbage() {
        assertFalse(tokenProvider.validateToken("not-a-jwt-token"));
    }

    @Test
    void validateToken_wrongIssuer() {
        JwtProperties otherProps = new JwtProperties();
        otherProps.setSecret("test-secret-key-that-is-at-least-32-characters-long-for-hs256");
        otherProps.setExpirationMinutes(15);
        otherProps.setIssuer("wrong-issuer");
        JwtTokenProvider otherProvider = new JwtTokenProvider(otherProps);

        UserPrincipal principal = new UserPrincipal(
            1L, "user@test.com", "User", Set.of(Role.VIEWER));

        String token = otherProvider.generateToken(principal);
        assertFalse(tokenProvider.validateToken(token));
    }

    @Test
    void parseToken_preservesAllRoles() {
        Set<Role> roles = Set.of(Role.VIEWER, Role.ADMIN, Role.QUIZ_MASTER, Role.LASTFM_CURATOR);
        UserPrincipal principal = new UserPrincipal(99L, "admin@test.com", "Admin", roles);

        String token = tokenProvider.generateToken(principal);
        UserPrincipal parsed = tokenProvider.parseToken(token);

        assertEquals(roles, parsed.getRoles());
    }

    @Test
    void userPrincipal_hasRole_checksAdmin() {
        UserPrincipal admin = new UserPrincipal(
            1L, "admin@test.com", "Admin", Set.of(Role.ADMIN));

        assertTrue(admin.hasRole(Role.ADMIN));
        assertTrue(admin.hasRole(Role.MASTER_CURATOR));
        assertTrue(admin.hasRole(Role.VIEWER));
    }

    @Test
    void userPrincipal_hasRole_withoutAdmin() {
        UserPrincipal curator = new UserPrincipal(
            2L, "curator@test.com", "Curator", Set.of(Role.MASTER_CURATOR));

        assertTrue(curator.hasRole(Role.MASTER_CURATOR));
        assertFalse(curator.hasRole(Role.ADMIN));
        assertFalse(curator.hasRole(Role.QUIZ_MASTER));
    }

    @Test
    void userPrincipal_authorities_haveRolePrefix() {
        UserPrincipal principal = new UserPrincipal(
            1L, "user@test.com", "User", Set.of(Role.VIEWER, Role.MASTER_CURATOR));

        var authorityNames = principal.getAuthorities().stream()
            .map(Object::toString)
            .toList();

        assertTrue(authorityNames.contains("ROLE_VIEWER"));
        assertTrue(authorityNames.contains("ROLE_MASTER_CURATOR"));
    }
}
