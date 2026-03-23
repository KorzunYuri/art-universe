package yurykorzun.art.universe.common.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import yurykorzun.art.universe.common.auth.entity.AuthUser;

import java.util.Optional;

public interface AuthUserRepository extends JpaRepository<AuthUser, Long> {

    Optional<AuthUser> findByGoogleSub(String googleSub);

    Optional<AuthUser> findByEmail(String email);
}
