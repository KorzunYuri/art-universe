package yurykorzun.art.universe.common.auth.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import yurykorzun.art.universe.common.auth.dto.AuthResponse;
import yurykorzun.art.universe.common.auth.dto.LoginRequest;
import yurykorzun.art.universe.common.auth.dto.RefreshRequest;
import yurykorzun.art.universe.common.auth.service.AuthService;
import yurykorzun.art.universe.common.security.model.UserPrincipal;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request.getGoogleIdToken());
    }

    @PostMapping("/refresh")
    public AuthResponse refresh(@Valid @RequestBody RefreshRequest request) {
        return authService.refresh(request.getRefreshToken());
    }

    @PostMapping("/logout")
    public void logout(@Valid @RequestBody RefreshRequest request) {
        authService.logout(request.getRefreshToken());
    }

    @GetMapping("/me")
    public AuthResponse.UserInfo me(@AuthenticationPrincipal UserPrincipal principal) {
        return AuthResponse.UserInfo.builder()
            .id(principal.getId())
            .email(principal.getEmail())
            .name(principal.getName())
            .roles(principal.getRoles().stream()
                .map(Enum::name)
                .collect(java.util.stream.Collectors.toSet()))
            .build();
    }
}
