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
    
    @Query("""
        SELECT g.id as gameId, p.id as pipelineId
        FROM pipeline_step ps
        JOIN pipeline p ON ps.pipelineId = p.id
        JOIN game g ON g.pipelineId = p.id
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
        SET     s.previewData = null,
                s.lastStepRunId = null
        WHERE   s.id IN :stepIds
    """)
    void clearResults(@Param("stepIds") List<Long> stepIds);
}
