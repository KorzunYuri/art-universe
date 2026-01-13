package yurykorzun.art.universe.music.data.raw.lastfm.etl.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.entity.LastfmArtistSearchRequest;

@NoRepositoryBean
public interface BaseLastfmArtistSearchRequestRepository extends JpaRepository<LastfmArtistSearchRequest, Long> {
}
