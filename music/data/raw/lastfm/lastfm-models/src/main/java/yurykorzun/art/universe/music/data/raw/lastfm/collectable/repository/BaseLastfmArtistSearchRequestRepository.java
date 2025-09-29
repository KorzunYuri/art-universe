package yurykorzun.art.universe.music.data.raw.lastfm.collectable.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.LastfmArtistSearchRequest;

public interface BaseLastfmArtistSearchRequestRepository extends JpaRepository<LastfmArtistSearchRequest, Long> {
}
