package yurykorzun.art.universe.common.auth.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class UpdateRolesRequest {

    @NotEmpty
    private Set<String> roles;
}
