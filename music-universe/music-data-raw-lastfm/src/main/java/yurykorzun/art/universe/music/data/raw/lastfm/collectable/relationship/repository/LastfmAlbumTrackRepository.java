package yurykorzun.art.universe.music.data.raw.lastfm.collectable.relationship.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.relationship.entity.LastfmAlbumTrack;

public interface LastfmAlbumTrackRepository extends JpaRepository<LastfmAlbumTrack, Long> {
}
