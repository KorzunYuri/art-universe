package yurykorzun.art.universe.music.data.semantic.parser.persistence;

import lombok.Data;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public class AnalysisRequestDao {

    private final JdbcTemplate jdbcTemplate;

    public AnalysisRequestDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<CompletedRequest> pollCompletedRequests(int batchSize) {
        return jdbcTemplate.query(
            """
            SELECT id, ticket_id, data_source, subject_type, subject_id, raw_response
            FROM mu_semantic_analysis.analysis_request
            WHERE status = 3
              AND id NOT IN (SELECT DISTINCT request_id FROM mu_semantic_analysis.proposal)
            ORDER BY completed_at
            LIMIT ?
            FOR UPDATE SKIP LOCKED
            """,
            (rs, rowNum) -> {
                CompletedRequest req = new CompletedRequest();
                req.setId(UUID.fromString(rs.getString("id")));
                req.setTicketId(UUID.fromString(rs.getString("ticket_id")));
                req.setDataSource(rs.getInt("data_source"));
                req.setSubjectType(rs.getInt("subject_type"));
                long subjId = rs.getLong("subject_id");
                req.setSubjectId(rs.wasNull() ? null : subjId);
                req.setRawResponse(rs.getString("raw_response"));
                return req;
            },
            batchSize
        );
    }

    @Data
    public static class CompletedRequest {
        private UUID id;
        private UUID ticketId;
        private int dataSource;
        private int subjectType;
        private Long subjectId;
        private String rawResponse;
    }
}
