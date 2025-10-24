package yurykorzun.art.universe.music.quiz.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import yurykorzun.art.universe.music.quiz.entity.PipelineStep;

import java.util.List;

@Repository
public interface PipelineStepRepository extends JpaRepository<PipelineStep, Long> {
    
    List<PipelineStep> findByPipelineIdOrderByOrd(Long pipelineId);
    
    @Modifying
    @Query("UPDATE pipeline_step ps SET ps.ord = ps.ord - 1 WHERE ps.pipelineId = :pipelineId AND ps.ord > :deletedOrd")
    void decrementOrderAfter(Long pipelineId, Integer deletedOrd);

    @Modifying
    @Query("UPDATE pipeline_step ps SET ps.ord = ps.ord + 1 WHERE ps.pipelineId = :pipelineId AND ps.ord >= :insertedOrd")
    void incrementOrderAfter(Long pipelineId, Integer insertedOrd);
    
    @Modifying
    @Query("DELETE FROM pipeline_step ps WHERE ps.pipelineId = :pipelineId AND ps.stepId = :stepId")
    void deleteByPipelineIdAndStepId(Long pipelineId, Long stepId);
}
