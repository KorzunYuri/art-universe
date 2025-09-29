package yurykorzun.art.universe.music.data.raw.lastfm.collectable.repository.attribute;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.attribute.LastfmDataSnapshot;

@NoRepositoryBean
public interface BaseLastfmDataSnapshotRepository extends JpaRepository<LastfmDataSnapshot, Long> {
}
