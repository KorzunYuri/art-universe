package yurykorzun.art.universe.music.data.raw.lastfm.collectable.repository.relationship;

import org.springframework.data.jpa.repository.JpaRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.relationship.LastfmArtistAlbum;

public interface BaseLastfmArtistAlbumRepository extends JpaRepository<LastfmArtistAlbum, Long> {
}
