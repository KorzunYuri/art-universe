package yurykorzun.art.universe.common.auth.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import yurykorzun.art.universe.common.auth.dto.UpdateRolesRequest;
import yurykorzun.art.universe.common.auth.dto.UserDto;
import yurykorzun.art.universe.common.auth.service.UserService;

@RestController
@RequestMapping("/api/v1/auth/users")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public Page<UserDto> getUsers(Pageable pageable) {
        return userService.getUsers(pageable);
    }

    @GetMapping("/{id}")
    public UserDto getUser(@PathVariable Long id) {
        return userService.getUser(id);
    }

    @PutMapping("/{id}/roles")
    public UserDto updateRoles(
        @PathVariable Long id,
        @Valid @RequestBody UpdateRolesRequest request
    ) {
        return userService.updateRoles(id, request.getRoles());
    }

    @PutMapping("/{id}/enabled")
    public UserDto setEnabled(
        @PathVariable Long id,
        @RequestParam boolean enabled
    ) {
        return userService.setEnabled(id, enabled);
    }
}
