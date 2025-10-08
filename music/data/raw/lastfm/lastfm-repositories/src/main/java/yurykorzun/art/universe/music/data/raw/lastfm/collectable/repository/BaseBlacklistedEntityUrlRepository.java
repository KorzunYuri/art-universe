package yurykorzun.art.universe.music.data.raw.lastfm.collectable.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.Param;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.BlacklistedEntityUrl;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.common.LastfmEntityType;

import java.util.List;

@NoRepositoryBean
public interface BaseBlacklistedEntityUrlRepository extends JpaRepository<BlacklistedEntityUrl, Long> {
}
