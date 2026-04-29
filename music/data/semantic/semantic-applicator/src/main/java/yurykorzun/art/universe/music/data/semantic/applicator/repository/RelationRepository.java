package yurykorzun.art.universe.music.data.semantic.applicator.repository;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import yurykorzun.art.universe.common.domain.entity.MasterEntityType;
import yurykorzun.art.universe.music.data.master.model.MasterApprovalStatus;
import yurykorzun.art.universe.music.data.master.model.Origin;

@Repository
public class RelationRepository {

    private static final int ORIGIN_AI = Origin.AI.getCode();
    private static final int APPROVAL_NEW = MasterApprovalStatus.NEW.getCode();

    private final JdbcTemplate jdbcTemplate;
    private final RelationTableResolver tableResolver;

    public RelationRepository(JdbcTemplate jdbcTemplate, RelationTableResolver tableResolver) {
        this.jdbcTemplate = jdbcTemplate;
        this.tableResolver = tableResolver;
    }

    public CreatedRelation findOrCreateRelation(
        MasterEntityType sourceType,
        Long sourceId,
        MasterEntityType targetType,
        Long targetId,
        Long relationTypeId
    ) {
        RelationTableResolver.Resolution resolution = tableResolver.resolve(sourceType, sourceId, targetType, targetId);

        Long existing = jdbcTemplate.query(
            "SELECT id FROM mu." + resolution.table()
                + " WHERE " + resolution.firstColumn() + " = ? "
                + "  AND " + resolution.secondColumn() + " = ? "
                + "  AND relation_type_id = ? LIMIT 1",
            rs -> rs.next() ? rs.getLong("id") : null,
            resolution.firstId(), resolution.secondId(), relationTypeId
        );
        if (existing != null) {
            return new CreatedRelation(resolution.table(), existing, true);
        }

        Long id = jdbcTemplate.queryForObject(
            "SELECT nextval('mu." + resolution.table() + "_seq')", Long.class
        );
        jdbcTemplate.update(
            "INSERT INTO mu." + resolution.table()
                + " (id, " + resolution.firstColumn() + ", " + resolution.secondColumn()
                + ", relation_type_id, origin, approval_status, created_at, updated_at) "
                + "VALUES (?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)",
            id, resolution.firstId(), resolution.secondId(), relationTypeId, ORIGIN_AI, APPROVAL_NEW
        );
        return new CreatedRelation(resolution.table(), id, false);
    }

    public RelationType findRelationType(Long relationTypeId) {
        try {
            return jdbcTemplate.queryForObject(
                "SELECT id, is_system FROM mu.relation_type WHERE id = ?",
                (rs, rowNum) -> new RelationType(rs.getLong("id"), rs.getBoolean("is_system")),
                relationTypeId
            );
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public record CreatedRelation(String table, Long id, boolean existed) {}

    public record RelationType(Long id, boolean isSystem) {}
}
