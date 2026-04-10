package yurykorzun.art.universe.music.data.semantic.analyzer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import yurykorzun.art.universe.music.data.semantic.analyzer.entity.AnalysisRequest;

import java.util.UUID;

@Repository
public interface AnalysisRequestRepository extends JpaRepository<AnalysisRequest, UUID> {

    /**
     * Marks all pending proposals for a specific subject and version as SUPERSEDED.
     * Used by reprocessing when a subject is re-queued under a new analysis version.
     *
     * @return the number of proposals updated
     */
    @Modifying
    @Query(
        value = """
            UPDATE mu_semantic_analysis.proposal
            SET resolution = 4,
                resolved_at = CURRENT_TIMESTAMP,
                resolved_by = 'reprocessing'
            WHERE resolution = 1
              AND request_id IN (
                SELECT id FROM mu_semantic_analysis.analysis_request
                WHERE analysis_version = :fromVersion
                  AND subject_type = :subjectType
                  AND subject_id = :subjectId
              )
            """,
        nativeQuery = true
    )
    int supersedePendingProposals(
        @Param("fromVersion") String fromVersion,
        @Param("subjectType") int subjectType,
        @Param("subjectId") long subjectId
    );
}
