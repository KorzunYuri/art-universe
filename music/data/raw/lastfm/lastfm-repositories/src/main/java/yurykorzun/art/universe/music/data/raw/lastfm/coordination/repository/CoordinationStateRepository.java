package yurykorzun.art.universe.music.data.raw.lastfm.coordination.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import yurykorzun.art.universe.music.data.raw.lastfm.coordination.entity.CoordinationStateEntity;

/**
 * Repository for coordination state (singleton table with id=1).
 */
@Repository
public interface CoordinationStateRepository extends JpaRepository<CoordinationStateEntity, Integer> {
    // Uses default JpaRepository methods only
}
