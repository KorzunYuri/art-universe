package yurykorzun.art.universe.common.auth.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import yurykorzun.art.universe.common.auth.config.AuthProperties;
import yurykorzun.art.universe.common.auth.dto.AuthResponse;
import yurykorzun.art.universe.common.auth.entity.AuthUser;
import yurykorzun.art.universe.common.auth.entity.RefreshToken;
import yurykorzun.art.universe.common.auth.repository.AuthUserRepository;
import yurykorzun.art.universe.common.auth.repository.RefreshTokenRepository;
import yurykorzun.art.universe.common.security.jwt.JwtTokenProvider;
import yurykorzun.art.universe.common.security.model.Role;
import yurykorzun.art.universe.common.security.model.UserPrincipal;

import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final AuthUserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final GoogleTokenVerifier googleTokenVerifier;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthProperties authProperties;

    @Transactional
    public AuthResponse login(String googleIdToken) {
        GoogleIdToken.Payload payload = googleTokenVerifier.verify(googleIdToken);
        if (payload == null) {
            throw new IllegalArgumentException("Invalid Google ID token");
        }

        String googleSub = payload.getSubject();
        String email = payload.getEmail();
        String name = (String) payload.get("name");
        String pictureUrl = (String) payload.get("picture");

        AuthUser user = userRepository.findByGoogleSub(googleSub)
            .map(existing -> {
                existing.setEmail(email);
                existing.setName(name);
                existing.setPictureUrl(pictureUrl);
                existing.setLastLoginAt(LocalDateTime.now());
                return userRepository.save(existing);
            })
            .orElseGet(() -> {
                log.info("Creating new user for email: {}", email);
                AuthUser newUser = new AuthUser();
                newUser.setGoogleSub(googleSub);
                newUser.setEmail(email);
                newUser.setName(name);
                newUser.setPictureUrl(pictureUrl);
                newUser.setRoles(EnumSet.of(Role.VIEWER));
                newUser.setLastLoginAt(LocalDateTime.now());
                return userRepository.save(newUser);
            });

        if (!user.isEnabled()) {
            throw new IllegalStateException("User account is disabled");
        }

        return buildAuthResponse(user);
    }

    @Transactional
    public AuthResponse refresh(String refreshTokenValue) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(refreshTokenValue)
            .orElseThrow(() -> new IllegalArgumentException("Invalid refresh token"));

        if (refreshToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            refreshTokenRepository.delete(refreshToken);
            throw new IllegalArgumentException("Refresh token expired");
        }

        AuthUser user = refreshToken.getUser();
        if (!user.isEnabled()) {
            throw new IllegalStateException("User account is disabled");
        }

        // Rotate: delete old, create new
        refreshTokenRepository.delete(refreshToken);

        return buildAuthResponse(user);
    }

    @Transactional
    public void logout(String refreshTokenValue) {
        refreshTokenRepository.deleteByToken(refreshTokenValue);
    }

    @Transactional
    public int cleanupExpiredTokens() {
        int deleted = refreshTokenRepository.deleteExpired(LocalDateTime.now());
        if (deleted > 0) {
            log.info("Cleaned up {} expired refresh tokens", deleted);
        }
        return deleted;
    }

    private AuthResponse buildAuthResponse(AuthUser user) {
        UserPrincipal principal = new UserPrincipal(
            user.getId(), user.getEmail(), user.getName(), user.getRoles());

        String accessToken = jwtTokenProvider.generateToken(principal);
        String refreshTokenValue = createRefreshToken(user);

        return AuthResponse.builder()
            .accessToken(accessToken)
            .refreshToken(refreshTokenValue)
            .user(AuthResponse.UserInfo.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .pictureUrl(user.getPictureUrl())
                .roles(user.getRoles().stream().map(Enum::name).collect(Collectors.toSet()))
                .build())
            .build();
    }

    private String createRefreshToken(AuthUser user) {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setExpiresAt(LocalDateTime.now()
            .plusDays(authProperties.getRefreshToken().getExpirationDays()));
        refreshTokenRepository.save(refreshToken);
        return refreshToken.getToken();
    }
}
