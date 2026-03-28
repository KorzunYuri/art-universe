package yurykorzun.art.universe.music.data.semantic.analyzer.reprocessing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class ReprocessingService {

    private static final Logger log = LoggerFactory.getLogger(ReprocessingService.class);

    private final JdbcTemplate jdbcTemplate;

    public ReprocessingService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional
    public ReprocessingResult triggerReprocessing(String fromVersion, String toVersion, int batchSize) {
        // Find entities processed under old version but not yet under new version
        List<Map<String, Object>> eligible = jdbcTemplate.queryForList(
            """
            SELECT DISTINCT ar.subject_type, ar.subject_id, ar.data_source,
                   at.subject_name, at.text_samples::text as text_samples,
                   at.expected_proposal_types, at.expected_entity_types, at.analysis_mode
            FROM mu_semantic_analysis.analysis_request ar
            JOIN mu_semantic_analysis.analysis_ticket at ON ar.ticket_id = at.id
            WHERE ar.status = 3
              AND ar.analysis_version = ?
              AND NOT EXISTS (
                SELECT 1 FROM mu_semantic_analysis.analysis_request ar2
                WHERE ar2.subject_type = ar.subject_type
                  AND ar2.subject_id = ar.subject_id
                  AND ar2.analysis_version = ?
              )
            LIMIT ?
            """,
            fromVersion, toVersion, batchSize
        );

        if (eligible.isEmpty()) {
            log.info("No entities eligible for reprocessing from {} to {}", fromVersion, toVersion);
            return new ReprocessingResult(0, 0);
        }

        // Supersede pending proposals from old version
        int superseded = jdbcTemplate.update(
            """
            UPDATE mu_semantic_analysis.proposal p
            SET resolution = 4, resolved_at = CURRENT_TIMESTAMP, resolved_by = 'reprocessing'
            WHERE p.resolution = 1
              AND p.request_id IN (
                SELECT ar.id FROM mu_semantic_analysis.analysis_request ar
                WHERE ar.analysis_version = ?
                  AND ar.subject_type || ':' || ar.subject_id IN (
                    SELECT DISTINCT ar2.subject_type || ':' || ar2.subject_id
                    FROM mu_semantic_analysis.analysis_request ar2
                    JOIN mu_semantic_analysis.analysis_ticket at2 ON ar2.ticket_id = at2.id
                    WHERE ar2.status = 3 AND ar2.analysis_version = ?
                      AND NOT EXISTS (
                        SELECT 1 FROM mu_semantic_analysis.analysis_request ar3
                        WHERE ar3.subject_type = ar2.subject_type
                          AND ar3.subject_id = ar2.subject_id
                          AND ar3.analysis_version = ?
                      )
                  )
              )
            """,
            fromVersion, fromVersion, toVersion
        );

        log.info("Superseded {} pending proposals from version {}", superseded, fromVersion);

        // Create new tickets for eligible entities
        int ticketsCreated = 0;
        for (Map<String, Object> row : eligible) {
            UUID ticketId = UUID.randomUUID();
            jdbcTemplate.update(
                """
                INSERT INTO mu_semantic_analysis.analysis_ticket
                    (id, data_source, subject_type, subject_id, subject_name,
                     text_samples, expected_proposal_types, expected_entity_types, analysis_mode, status)
                VALUES (?, ?, ?, ?, ?, ?::jsonb, ?, ?, ?, 1)
                """,
                ticketId,
                row.get("data_source"),
                row.get("subject_type"),
                row.get("subject_id"),
                row.get("subject_name"),
                row.get("text_samples"),
                row.get("expected_proposal_types"),
                row.get("expected_entity_types"),
                row.get("analysis_mode")
            );
            ticketsCreated++;
        }

        log.info("Created {} reprocessing tickets for version {}", ticketsCreated, toVersion);
        return new ReprocessingResult(ticketsCreated, superseded);
    }

    public record ReprocessingResult(int ticketsCreated, int proposalsSuperseded) {}
}
