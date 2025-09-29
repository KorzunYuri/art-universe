package yurykorzun.art.universe.music.data.raw.lastfm.collectable.repository.attribute;

import org.springframework.data.jpa.repository.JpaRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.attribute.LastfmDataSnapshot;

public interface BaseLastfmDataSnapshotRepository extends JpaRepository<LastfmDataSnapshot, Long> {
}
