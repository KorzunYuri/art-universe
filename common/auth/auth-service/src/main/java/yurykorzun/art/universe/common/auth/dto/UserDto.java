package yurykorzun.art.universe.common.auth.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Builder
public class UserDto {

    private Long id;
    private String email;
    private String name;
    private String pictureUrl;
    private Set<String> roles;
    private boolean enabled;
    private LocalDateTime createdAt;
    private LocalDateTime lastLoginAt;
}
