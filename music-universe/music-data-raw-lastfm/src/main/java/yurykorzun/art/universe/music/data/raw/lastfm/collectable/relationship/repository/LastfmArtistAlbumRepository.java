package yurykorzun.art.universe.music.data.raw.lastfm.collectable.relationship.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.relationship.entity.LastfmArtistAlbum;

public interface LastfmArtistAlbumRepository extends JpaRepository<LastfmArtistAlbum, Long> {
}
