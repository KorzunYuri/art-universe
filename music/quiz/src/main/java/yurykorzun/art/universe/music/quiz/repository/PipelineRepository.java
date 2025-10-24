package yurykorzun.art.universe.music.quiz.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import yurykorzun.art.universe.music.quiz.entity.Pipeline;

import java.util.Optional;

@Repository
public interface PipelineRepository extends JpaRepository<Pipeline, Long> {
    
    Optional<Pipeline> findByGameId(Long gameId);
}
