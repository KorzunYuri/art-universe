package yurykorzun.art.universe.music.data.raw.lastfm.collectable.relationship.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.relationship.entity.LastfmTrackTag;

public interface LastfmTrackTagRepository extends JpaRepository<LastfmTrackTag, Long> {
}
