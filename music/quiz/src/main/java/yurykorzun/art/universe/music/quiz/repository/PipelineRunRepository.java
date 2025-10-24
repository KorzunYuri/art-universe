package yurykorzun.art.universe.music.quiz.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import yurykorzun.art.universe.music.quiz.entity.PipelineRun;

@Repository
public interface PipelineRunRepository extends JpaRepository<PipelineRun, Long> {
}
