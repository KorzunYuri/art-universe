package yurykorzun.art.universe.common.security.model;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
public class UserPrincipal implements UserDetails {

    private final Long id;
    private final String email;
    private final String name;
    private final Set<Role> roles;
    private final Collection<? extends GrantedAuthority> authorities;

    public UserPrincipal(Long id, String email, String name, Set<Role> roles) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.roles = roles;
        this.authorities = roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.name()))
                .collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public String getUsername() {
        return email;
    }

    public boolean hasRole(Role role) {
        return roles.contains(Role.ADMIN) || roles.contains(role);
    }

    public boolean hasAnyRole(Role... checkRoles) {
        if (roles.contains(Role.ADMIN)) {
            return true;
        }
        for (Role r : checkRoles) {
            if (roles.contains(r)) {
                return true;
            }
        }
        return false;
    }
}
