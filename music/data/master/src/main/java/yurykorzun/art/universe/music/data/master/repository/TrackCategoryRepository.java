package yurykorzun.art.universe.music.data.master.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import yurykorzun.art.universe.music.data.master.entity.TrackCategory;

import java.util.Optional;

@Repository
public interface TrackCategoryRepository extends JpaRepository<TrackCategory, Long> {

    boolean existsByTrackIdAndCategoryId(Long trackId, Long categoryId);

    Optional<TrackCategory> findByTrackIdAndCategoryId(Long trackId, Long categoryId);
}
