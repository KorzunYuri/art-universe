package yurykorzun.art.universe.music.data.raw.lastfm.collectable.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.LastfmArtist;

public interface BaseLastfmArtistRepository extends JpaRepository<LastfmArtist, Long> {
}
