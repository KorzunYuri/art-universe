package yurykorzun.art.universe.music.data.raw.lastfm.collectable.repository.relationship;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.relationship.LastfmArtistTag;

@NoRepositoryBean
public interface BaseLastfmArtistTagRepository extends JpaRepository<LastfmArtistTag, Long> {
}
