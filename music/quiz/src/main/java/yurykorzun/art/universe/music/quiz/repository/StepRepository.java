package yurykorzun.art.universe.music.quiz.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import yurykorzun.art.universe.music.quiz.entity.Step;

@Repository
public interface StepRepository extends JpaRepository<Step, Long> {
    
    @Modifying
    @Query("""
        UPDATE step s SET s.previewData = NULL, s.lastStepRunId = NULL
        WHERE s.id IN (
            SELECT ps.stepId FROM pipeline_step ps 
            WHERE ps.pipelineId = :pipelineId AND ps.ord >= :fromPosition
        )
    """)
    void clearSubsequentStepResults(@Param("pipelineId") Long pipelineId, @Param("fromPosition") Integer fromPosition);
}
