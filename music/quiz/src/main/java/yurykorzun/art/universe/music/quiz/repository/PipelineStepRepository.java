package yurykorzun.art.universe.music.quiz.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import yurykorzun.art.universe.music.quiz.entity.PipelineStep;
import yurykorzun.art.universe.music.quiz.entity.Step;
import yurykorzun.art.universe.music.quiz.entity.StepRun;

import java.util.List;
import java.util.Optional;

@Repository
public interface PipelineStepRepository extends JpaRepository<PipelineStep, Long> {
    
    List<PipelineStep> findByPipelineIdOrderByOrd(Long pipelineId);
    
    @Query("""
        SELECT ps.ord as ord, s as step, sr as stepRun
        FROM pipeline_step ps
        JOIN step s ON ps.stepId = s.id
        LEFT JOIN step_run sr ON s.lastStepRunId = sr.id
        WHERE ps.pipelineId = :pipelineId
        ORDER BY ps.ord
    """)
    List<PipelineStepWithDetails> findPipelineStepsWithDetails(@Param("pipelineId") Long pipelineId);
    
    @Query("""
        SELECT MIN(ps.ord)
        FROM pipeline_step ps
        JOIN step s ON ps.stepId = s.id
        WHERE ps.pipelineId = :pipelineId AND s.lastStepRunId IS NULL
    """)
    Optional<Integer> findEarliestStepWithoutResult(@Param("pipelineId") Long pipelineId);
    
    @Modifying
    @Query("UPDATE pipeline_step ps SET ps.ord = ps.ord - 1 WHERE ps.pipelineId = :pipelineId AND ps.ord > :deletedOrd")
    void decrementOrderAfter(Long pipelineId, Integer deletedOrd);

    @Modifying
    @Query("UPDATE pipeline_step ps SET ps.ord = ps.ord + 1 WHERE ps.pipelineId = :pipelineId AND ps.ord >= :insertedOrd")
    void incrementOrderAfter(Long pipelineId, Integer insertedOrd);
    
    @Modifying
    @Query("UPDATE pipeline_step ps SET ps.ord = ps.ord - 1 WHERE ps.pipelineId = :pipelineId AND ps.ord > :fromOrd AND ps.ord <= :toOrd")
    void decrementOrderBetween(@Param("pipelineId") Long pipelineId, @Param("fromOrd") Integer fromOrd, @Param("toOrd") Integer toOrd);
    
    @Modifying
    @Query("UPDATE pipeline_step ps SET ps.ord = ps.ord + 1 WHERE ps.pipelineId = :pipelineId AND ps.ord >= :fromOrd AND ps.ord < :toOrd")
    void incrementOrderBetween(@Param("pipelineId") Long pipelineId, @Param("fromOrd") Integer fromOrd, @Param("toOrd") Integer toOrd);
    
    @Modifying
    @Query("UPDATE pipeline_step ps SET ps.ord = :newOrd WHERE ps.pipelineId = :pipelineId AND ps.stepId = :stepId")
    void updateStepOrder(@Param("pipelineId") Long pipelineId, @Param("stepId") Long stepId, @Param("newOrd") Integer newOrd);
    
    @Modifying
    @Query("DELETE FROM pipeline_step ps WHERE ps.pipelineId = :pipelineId AND ps.stepId = :stepId")
    void deleteByPipelineIdAndStepId(Long pipelineId, Long stepId);
    
    interface PipelineStepWithDetails {
        Integer getOrd();
        Step getStep();
        StepRun getStepRun();
    }
}
