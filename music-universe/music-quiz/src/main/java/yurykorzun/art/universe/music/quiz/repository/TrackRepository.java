package yurykorzun.art.universe.music.quiz.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import yurykorzun.art.universe.music.quiz.entity.Track;

@Repository
public interface TrackRepository extends JpaRepository<Track, Long> {
}
