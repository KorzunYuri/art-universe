package yurykorzun.art.universe.music.data.raw.lastfm.etl.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.BlacklistedEntityUrl;

@NoRepositoryBean
public interface BaseBlacklistedEntityUrlRepository extends JpaRepository<BlacklistedEntityUrl, Long> {
}
