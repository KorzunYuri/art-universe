package yurykorzun.art.universe.music.quiz.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import yurykorzun.art.universe.music.quiz.entity.Step;

import java.util.List;

@Repository
public interface StepRepository extends JpaRepository<Step, Long> {

    /**
     * Using a step, extract metadata of related entities.
     * This method covers two cases:
     * - single execution (step.id -> pipeline_step.step_id -> pipeline_step.pipeline_id -> pipeline.id -> game.pipeline_id
     * - pipeline execution (step.id -> pipeline_step.step_id -> pipeline_step.pipeline_id -> pipeline.id -> generation.pipeline_id -> generation.game_id
     */
    @Query("""
        SELECT 
            COALESCE(g.id, gen.gameId) as gameId, 
            p.id as pipelineId,
            gen.id as generationId
        FROM pipeline_step ps
        JOIN pipeline p ON ps.pipelineId = p.id
        LEFT JOIN game g ON g.pipelineId = p.id
        LEFT JOIN generation gen ON gen.pipelineId = p.id
        WHERE ps.stepId = :stepId
    """)
    StepMetadataProjection getStepMetadata(@Param("stepId") Long stepId);

    @Modifying
    @Query("""
        UPDATE  step s
        SET     s.previewData = :preview
        WHERE   s.id = :stepId
    """)
    void updatePreview(@Param("stepId") Long stepId, @Param("preview") String preview);

    @Modifying
    @Query("""
        UPDATE  step s
        SET     s.deleted = true
        WHERE   s.id = :stepId
    """)
    void softDelete(@Param("stepId") Long stepId);

    @Modifying
    @Query("""
        UPDATE  step s
        SET     s.lastStepRunId = :stepRunId
        WHERE   s.id = :stepId
    """)
    void setLastRun(@Param("stepId") Long stepId, @Param("stepRunId") Long stepRunId);

    @Modifying
    @Query("""
        UPDATE  step s
        SET     s.lastStepRunId = null
        WHERE   s.id IN :stepIds
    """)
    void clearLastRun(@Param("stepIds") List<Long> stepIds);
}
