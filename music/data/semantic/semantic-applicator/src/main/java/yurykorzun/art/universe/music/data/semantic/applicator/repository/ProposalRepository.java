package yurykorzun.art.universe.music.data.semantic.applicator.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import yurykorzun.art.universe.common.CodedRegistry;
import yurykorzun.art.universe.common.domain.entity.MasterEntityType;
import yurykorzun.art.universe.music.data.semantic.applicator.applier.ProposalRow;
import yurykorzun.art.universe.music.data.semantic.model.ProposalResolution;
import yurykorzun.art.universe.music.data.semantic.model.ProposalType;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

@Repository
public class ProposalRepository {

    private static final String SELECT_COLUMNS =
        "id, request_id, synth_id, proposal_type, subject_type, subject_id, subject_ref, confidence, reasoning, payload, resolution";

    private static final RowMapper<ProposalRow> ROW_MAPPER = ProposalRepository::mapRow;

    private final JdbcTemplate jdbcTemplate;

    public ProposalRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<ProposalRow> fetchPendingForUpdate(int batchSize) {
        return jdbcTemplate.query(
            "SELECT " + SELECT_COLUMNS + " FROM mu_semantic_analysis.proposal "
                + "WHERE resolution = ? ORDER BY created_at LIMIT ? FOR UPDATE SKIP LOCKED",
            ROW_MAPPER,
            ProposalResolution.PENDING.getCode(), batchSize
        );
    }

    public List<ProposalRow> fetchApprovedForUpdate(int batchSize) {
        return jdbcTemplate.query(
            "SELECT " + SELECT_COLUMNS + " FROM mu_semantic_analysis.proposal "
                + "WHERE resolution IN (?, ?) AND applied_ref IS NULL "
                + "ORDER BY created_at LIMIT ? FOR UPDATE SKIP LOCKED",
            ROW_MAPPER,
            ProposalResolution.APPROVED.getCode(), ProposalResolution.AUTO_APPROVED.getCode(), batchSize
        );
    }

    public void markAutoResolution(Long proposalId, ProposalResolution resolution) {
        jdbcTemplate.update(
            "UPDATE mu_semantic_analysis.proposal "
                + "SET resolution = ?, resolved_by = 'auto', resolved_at = CURRENT_TIMESTAMP "
                + "WHERE id = ?",
            resolution.getCode(), proposalId
        );
    }

    public void markApplied(Long proposalId, String appliedRef) {
        jdbcTemplate.update(
            """
            UPDATE mu_semantic_analysis.proposal
            SET resolution = ?, applied_ref = ?, resolved_at = CURRENT_TIMESTAMP, resolved_by = 'applicator'
            WHERE id = ?
            """,
            ProposalResolution.APPROVED.getCode(), appliedRef, proposalId
        );
    }

    private static ProposalRow mapRow(ResultSet rs, int rowNum) throws SQLException {
        ProposalRow row = new ProposalRow();
        row.setId(rs.getLong("id"));
        row.setRequestId(UUID.fromString(rs.getString("request_id")));
        row.setSynthId(rs.getString("synth_id"));
        row.setProposalType(resolveEnum(rs.getInt("proposal_type"), ProposalType.class));
        row.setSubjectType(resolveEnum(rs.getInt("subject_type"), MasterEntityType.class));
        long subjId = rs.getLong("subject_id");
        row.setSubjectId(rs.wasNull() ? null : subjId);
        row.setSubjectRef(rs.getString("subject_ref"));
        row.setConfidence(rs.getShort("confidence"));
        row.setReasoning(rs.getString("reasoning"));
        row.setPayload(rs.getString("payload"));
        row.setResolution(resolveEnum(rs.getInt("resolution"), ProposalResolution.class));
        return row;
    }

    private static <T extends yurykorzun.art.universe.common.Coded> T resolveEnum(int code, Class<T> clazz) {
        return CodedRegistry.getByCode(code, clazz)
            .orElseThrow(() -> new IllegalStateException("Unknown code " + code + " for " + clazz.getSimpleName()));
    }
}
