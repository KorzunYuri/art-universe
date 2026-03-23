package yurykorzun.art.universe.common.auth.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import yurykorzun.art.universe.common.auth.config.AuthProperties;
import yurykorzun.art.universe.common.auth.dto.AuthResponse;
import yurykorzun.art.universe.common.auth.entity.AuthUser;
import yurykorzun.art.universe.common.auth.entity.RefreshToken;
import yurykorzun.art.universe.common.auth.repository.AuthUserRepository;
import yurykorzun.art.universe.common.auth.repository.RefreshTokenRepository;
import yurykorzun.art.universe.common.security.jwt.JwtProperties;
import yurykorzun.art.universe.common.security.jwt.JwtTokenProvider;
import yurykorzun.art.universe.common.security.model.Role;

import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthUserRepository userRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private GoogleTokenVerifier googleTokenVerifier;

    @InjectMocks
    private AuthService authService;

    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        JwtProperties jwtProperties = new JwtProperties();
        jwtProperties.setSecret("test-secret-key-that-is-at-least-32-characters-long-for-hs256");
        jwtProperties.setExpirationMinutes(15);
        jwtProperties.setIssuer("art-universe");
        jwtTokenProvider = new JwtTokenProvider(jwtProperties);

        AuthProperties authProperties = new AuthProperties();
        authProperties.getRefreshToken().setExpirationDays(7);

        authService = new AuthService(
            userRepository, refreshTokenRepository, googleTokenVerifier,
            jwtTokenProvider, authProperties);
    }

    @Test
    void login_newUser_createsAccountWithViewerRole() {
        GoogleIdToken.Payload payload = new GoogleIdToken.Payload();
        payload.setSubject("google-sub-123");
        payload.setEmail("new@example.com");
        payload.set("name", "New User");
        payload.set("picture", "https://photo.url");

        when(googleTokenVerifier.verify("valid-token")).thenReturn(payload);
        when(userRepository.findByGoogleSub("google-sub-123")).thenReturn(Optional.empty());
        when(userRepository.save(any(AuthUser.class))).thenAnswer(inv -> {
            AuthUser user = inv.getArgument(0);
            user.setId(1L);
            return user;
        });
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(inv -> inv.getArgument(0));

        AuthResponse response = authService.login("valid-token");

        assertNotNull(response.getAccessToken());
        assertNotNull(response.getRefreshToken());
        assertEquals("new@example.com", response.getUser().getEmail());
        assertEquals("New User", response.getUser().getName());
        assertTrue(response.getUser().getRoles().contains("VIEWER"));

        ArgumentCaptor<AuthUser> userCaptor = ArgumentCaptor.forClass(AuthUser.class);
        verify(userRepository).save(userCaptor.capture());
        AuthUser savedUser = userCaptor.getValue();
        assertEquals(EnumSet.of(Role.VIEWER), savedUser.getRoles());
    }

    @Test
    void login_existingUser_updatesProfileAndReturnsTokens() {
        AuthUser existingUser = new AuthUser();
        existingUser.setId(42L);
        existingUser.setGoogleSub("google-sub-456");
        existingUser.setEmail("existing@example.com");
        existingUser.setName("Old Name");
        existingUser.setRoles(EnumSet.of(Role.VIEWER, Role.MASTER_CURATOR));
        existingUser.setEnabled(true);

        GoogleIdToken.Payload payload = new GoogleIdToken.Payload();
        payload.setSubject("google-sub-456");
        payload.setEmail("existing@example.com");
        payload.set("name", "Updated Name");
        payload.set("picture", "https://new-photo.url");

        when(googleTokenVerifier.verify("valid-token")).thenReturn(payload);
        when(userRepository.findByGoogleSub("google-sub-456")).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(AuthUser.class))).thenAnswer(inv -> inv.getArgument(0));
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(inv -> inv.getArgument(0));

        AuthResponse response = authService.login("valid-token");

        assertEquals(42L, response.getUser().getId());
        assertTrue(response.getUser().getRoles().contains("MASTER_CURATOR"));
        assertEquals("Updated Name", response.getUser().getName());
    }

    @Test
    void login_invalidGoogleToken_throws() {
        when(googleTokenVerifier.verify("bad-token")).thenReturn(null);

        assertThrows(IllegalArgumentException.class,
            () -> authService.login("bad-token"));
    }

    @Test
    void login_disabledUser_throws() {
        AuthUser disabledUser = new AuthUser();
        disabledUser.setId(1L);
        disabledUser.setGoogleSub("google-sub-disabled");
        disabledUser.setEmail("disabled@example.com");
        disabledUser.setName("Disabled");
        disabledUser.setRoles(EnumSet.of(Role.VIEWER));
        disabledUser.setEnabled(false);

        GoogleIdToken.Payload payload = new GoogleIdToken.Payload();
        payload.setSubject("google-sub-disabled");
        payload.setEmail("disabled@example.com");
        payload.set("name", "Disabled");

        when(googleTokenVerifier.verify("token")).thenReturn(payload);
        when(userRepository.findByGoogleSub("google-sub-disabled")).thenReturn(Optional.of(disabledUser));
        when(userRepository.save(any(AuthUser.class))).thenReturn(disabledUser);

        assertThrows(IllegalStateException.class,
            () -> authService.login("token"));
    }

    @Test
    void refresh_validToken_returnsNewTokens() {
        AuthUser user = new AuthUser();
        user.setId(1L);
        user.setEmail("user@test.com");
        user.setName("User");
        user.setRoles(EnumSet.of(Role.VIEWER));
        user.setEnabled(true);

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setId(10L);
        refreshToken.setUser(user);
        refreshToken.setToken("old-refresh-token");
        refreshToken.setExpiresAt(LocalDateTime.now().plusDays(5));

        when(refreshTokenRepository.findByToken("old-refresh-token")).thenReturn(Optional.of(refreshToken));
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(inv -> inv.getArgument(0));

        AuthResponse response = authService.refresh("old-refresh-token");

        assertNotNull(response.getAccessToken());
        assertNotNull(response.getRefreshToken());
        assertNotEquals("old-refresh-token", response.getRefreshToken());
        verify(refreshTokenRepository).delete(refreshToken);
    }

    @Test
    void refresh_expiredToken_throws() {
        AuthUser user = new AuthUser();
        user.setId(1L);

        RefreshToken expired = new RefreshToken();
        expired.setUser(user);
        expired.setToken("expired-token");
        expired.setExpiresAt(LocalDateTime.now().minusDays(1));

        when(refreshTokenRepository.findByToken("expired-token")).thenReturn(Optional.of(expired));

        assertThrows(IllegalArgumentException.class,
            () -> authService.refresh("expired-token"));
    }

    @Test
    void refresh_invalidToken_throws() {
        when(refreshTokenRepository.findByToken("nonexistent")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
            () -> authService.refresh("nonexistent"));
    }

    @Test
    void logout_deletesRefreshToken() {
        authService.logout("some-refresh-token");
        verify(refreshTokenRepository).deleteByToken("some-refresh-token");
    }
}
