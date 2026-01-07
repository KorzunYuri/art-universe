package yurykorzun.art.universe.music.quiz.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import yurykorzun.art.universe.music.quiz.entity.GenerationTrack;

import java.util.List;

@Repository
public interface GenerationTrackRepository extends JpaRepository<GenerationTrack, Long> {
    
    List<GenerationTrack> findByGenerationIdOrderByOrderIndex(Long generationId);
    void deleteByGenerationIdAndTrackId(Long generationId, Long trackId);
}
