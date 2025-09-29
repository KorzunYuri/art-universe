package yurykorzun.art.universe.music.data.raw.lastfm.collectable.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.LastfmTrack;

@NoRepositoryBean
public interface BaseLastfmTrackRepository extends JpaRepository<LastfmTrack, Long> {
}
