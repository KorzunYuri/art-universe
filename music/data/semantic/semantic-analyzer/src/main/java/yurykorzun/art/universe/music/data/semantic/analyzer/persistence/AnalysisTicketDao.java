package yurykorzun.art.universe.music.data.semantic.analyzer.persistence;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

@Repository
public class AnalysisTicketDao {

    private final JdbcTemplate jdbcTemplate;

    public AnalysisTicketDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<TicketRow> pollPendingTickets(int batchSize) {
        return jdbcTemplate.query(
            """
            SELECT id, data_source, subject_type, subject_id, subject_name,
                   text_samples::text, expected_proposal_types, expected_entity_types, analysis_mode
            FROM mu_semantic_analysis.analysis_ticket
            WHERE status = 1
            ORDER BY created_at
            LIMIT ?
            FOR UPDATE SKIP LOCKED
            """,
            (rs, rowNum) -> mapTicketRow(rs),
            batchSize
        );
    }

    public void updateStatus(UUID ticketId, int status, String analysisVersion, String errorMessage) {
        if (status == 2) {
            jdbcTemplate.update(
                "UPDATE mu_semantic_analysis.analysis_ticket SET status = ?, analysis_version = ?, picked_up_at = CURRENT_TIMESTAMP WHERE id = ?",
                status, analysisVersion, ticketId
            );
        } else {
            jdbcTemplate.update(
                "UPDATE mu_semantic_analysis.analysis_ticket SET status = ?, error_message = ?, completed_at = CURRENT_TIMESTAMP WHERE id = ?",
                status, errorMessage, ticketId
            );
        }
    }

    public void saveRawResponse(UUID requestId, UUID ticketId, String inputHash, String analysisVersion,
                                 int dataSource, int subjectType, Long subjectId, int analysisMode,
                                 String provider, String model, int promptTokens, int completionTokens,
                                 String rawResponse) {
        jdbcTemplate.update(
            """
            INSERT INTO mu_semantic_analysis.analysis_request
                (id, ticket_id, input_hash, analysis_version, data_source, subject_type, subject_id, analysis_mode,
                 status, llm_provider, llm_model, prompt_tokens, completion_tokens, raw_response, completed_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, 3, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)
            """,
            requestId, ticketId, inputHash, analysisVersion,
            dataSource, subjectType, subjectId, analysisMode,
            provider, model, promptTokens, completionTokens, rawResponse
        );
    }

    public void saveFailedRequest(UUID requestId, UUID ticketId, String inputHash, String analysisVersion,
                                   int dataSource, int subjectType, Long subjectId, int analysisMode,
                                   String errorMessage) {
        jdbcTemplate.update(
            """
            INSERT INTO mu_semantic_analysis.analysis_request
                (id, ticket_id, input_hash, analysis_version, data_source, subject_type, subject_id, analysis_mode,
                 status, error_message, completed_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, 4, ?, CURRENT_TIMESTAMP)
            """,
            requestId, ticketId, inputHash, analysisVersion,
            dataSource, subjectType, subjectId, analysisMode, errorMessage
        );
    }

    private TicketRow mapTicketRow(ResultSet rs) throws SQLException {
        TicketRow row = new TicketRow();
        row.setId(UUID.fromString(rs.getString("id")));
        row.setDataSource(rs.getInt("data_source"));
        row.setSubjectType(rs.getInt("subject_type"));
        long subjectId = rs.getLong("subject_id");
        row.setSubjectId(rs.wasNull() ? null : subjectId);
        row.setSubjectName(rs.getString("subject_name"));
        row.setTextSamplesJson(rs.getString("text_samples"));
        row.setExpectedProposalTypes(toIntArray(rs.getArray("expected_proposal_types")));
        row.setExpectedEntityTypes(toIntArray(rs.getArray("expected_entity_types")));
        row.setAnalysisMode(rs.getInt("analysis_mode"));
        return row;
    }

    private int[] toIntArray(Array sqlArray) throws SQLException {
        if (sqlArray == null) return null;
        Integer[] arr = (Integer[]) sqlArray.getArray();
        int[] result = new int[arr.length];
        for (int i = 0; i < arr.length; i++) {
            result[i] = arr[i];
        }
        return result;
    }

}
