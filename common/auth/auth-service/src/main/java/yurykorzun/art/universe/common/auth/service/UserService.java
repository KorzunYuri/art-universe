package yurykorzun.art.universe.common.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import yurykorzun.art.universe.common.auth.dto.UserDto;
import yurykorzun.art.universe.common.auth.entity.AuthUser;
import yurykorzun.art.universe.common.auth.repository.AuthUserRepository;
import yurykorzun.art.universe.common.auth.repository.RefreshTokenRepository;
import yurykorzun.art.universe.common.exception.CustomEntityNotFoundException;
import yurykorzun.art.universe.common.security.model.Role;

import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final AuthUserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional(readOnly = true)
    public Page<UserDto> getUsers(Pageable pageable) {
        return userRepository.findAll(pageable).map(this::toDto);
    }

    @Transactional(readOnly = true)
    public UserDto getUser(Long id) {
        return userRepository.findById(id)
            .map(this::toDto)
            .orElseThrow(() -> new CustomEntityNotFoundException("User", id));
    }

    @Transactional
    public UserDto updateRoles(Long userId, Set<String> roleNames) {
        AuthUser user = userRepository.findById(userId)
            .orElseThrow(() -> new CustomEntityNotFoundException("User", userId));

        Set<Role> roles = roleNames.stream()
            .map(Role::valueOf)
            .collect(Collectors.toCollection(() -> EnumSet.noneOf(Role.class)));

        log.info("Updating roles for user {} ({}): {} -> {}", userId, user.getEmail(), user.getRoles(), roles);
        user.setRoles(roles);
        userRepository.save(user);

        // Invalidate all refresh tokens so user must re-login with new roles
        refreshTokenRepository.deleteAllByUserId(userId);

        return toDto(user);
    }

    @Transactional
    public UserDto setEnabled(Long userId, boolean enabled) {
        AuthUser user = userRepository.findById(userId)
            .orElseThrow(() -> new CustomEntityNotFoundException("User", userId));

        log.info("Setting enabled={} for user {} ({})", enabled, userId, user.getEmail());
        user.setEnabled(enabled);
        userRepository.save(user);

        if (!enabled) {
            refreshTokenRepository.deleteAllByUserId(userId);
        }

        return toDto(user);
    }

    private UserDto toDto(AuthUser user) {
        return UserDto.builder()
            .id(user.getId())
            .email(user.getEmail())
            .name(user.getName())
            .pictureUrl(user.getPictureUrl())
            .roles(user.getRoles().stream().map(Enum::name).collect(Collectors.toSet()))
            .enabled(user.isEnabled())
            .createdAt(user.getCreatedAt())
            .lastLoginAt(user.getLastLoginAt())
            .build();
    }
}
