package yurykorzun.art.universe.music.data.raw.lastfm.api.client.repository;

import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.attribute.LastfmDataSnapshot;

@Repository
@Profile("test")
public interface TestLastfmDataSnapshotRepository extends JpaRepository<LastfmDataSnapshot, Long> {
}
