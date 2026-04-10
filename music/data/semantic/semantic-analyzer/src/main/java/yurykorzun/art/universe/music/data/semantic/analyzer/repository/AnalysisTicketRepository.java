package yurykorzun.art.universe.music.data.semantic.analyzer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import yurykorzun.art.universe.music.data.semantic.analyzer.entity.AnalysisTicket;

import java.util.List;
import java.util.UUID;

@Repository
public interface AnalysisTicketRepository extends JpaRepository<AnalysisTicket, UUID> {

    /**
     * Polls pending tickets with row-level pessimistic locking.
     * Uses FOR UPDATE SKIP LOCKED so multiple analyzer instances can consume
     * the queue concurrently without blocking each other.
     */
    @Query(
        value = """
            SELECT * FROM mu_semantic_analysis.analysis_ticket
            WHERE status = 1
            ORDER BY created_at
            LIMIT :batchSize
            FOR UPDATE SKIP LOCKED
            """,
        nativeQuery = true
    )
    List<AnalysisTicket> pollPendingTickets(@Param("batchSize") int batchSize);

    @Query(
        value = "SELECT status, COUNT(*) FROM mu_semantic_analysis.analysis_ticket GROUP BY status",
        nativeQuery = true
    )
    List<Object[]> countByStatus();

    /**
     * Finds source tickets whose subjects were processed under {@code fromVersion}
     * but have not yet been processed under {@code toVersion}. Used by reprocessing
     * to clone fresh tickets for the new analysis version.
     *
     * Returns at most one ticket per (subject_type, subject_id) via {@code DISTINCT ON}.
     * Tickets with null subject_id are skipped.
     */
    @Query(
        value = """
            SELECT DISTINCT ON (at.subject_type, at.subject_id) at.*
            FROM mu_semantic_analysis.analysis_ticket at
            JOIN mu_semantic_analysis.analysis_request ar ON ar.ticket_id = at.id
            WHERE ar.status = 3
              AND ar.analysis_version = :fromVersion
              AND at.subject_id IS NOT NULL
              AND NOT EXISTS (
                SELECT 1 FROM mu_semantic_analysis.analysis_request ar2
                WHERE ar2.subject_type = at.subject_type
                  AND ar2.subject_id = at.subject_id
                  AND ar2.analysis_version = :toVersion
              )
            ORDER BY at.subject_type, at.subject_id, at.created_at DESC
            LIMIT :batchSize
            """,
        nativeQuery = true
    )
    List<AnalysisTicket> findEligibleForReprocessing(
        @Param("fromVersion") String fromVersion,
        @Param("toVersion") String toVersion,
        @Param("batchSize") int batchSize
    );
}
