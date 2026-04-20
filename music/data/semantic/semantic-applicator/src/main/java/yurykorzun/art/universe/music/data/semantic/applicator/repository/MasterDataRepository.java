package yurykorzun.art.universe.music.data.semantic.applicator.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import yurykorzun.art.universe.common.domain.entity.MasterEntityType;
import yurykorzun.art.universe.music.data.master.model.MasterApprovalStatus;
import yurykorzun.art.universe.music.data.master.model.Origin;

@Repository
public class MasterDataRepository {

    private static final int ORIGIN_AI = Origin.AI.getCode();
    private static final int APPROVAL_NEW = MasterApprovalStatus.NEW.getCode();

    private final JdbcTemplate jdbcTemplate;

    public MasterDataRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Long createEntity(MasterEntityType type, String name) {
        String table = tableFor(type);
        Long id = jdbcTemplate.queryForObject("SELECT nextval('mu." + table + "_seq')", Long.class);
        jdbcTemplate.update(
            "INSERT INTO mu." + table + " (id, name, origin, approval_status, created_at, updated_at) "
                + "VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)",
            id, name, ORIGIN_AI, APPROVAL_NEW
        );
        return id;
    }

    public Long createCategory(String name) {
        Long id = jdbcTemplate.queryForObject("SELECT nextval('mu.category_seq')", Long.class);
        jdbcTemplate.update(
            "INSERT INTO mu.category (id, name, origin, approval_status, created_at, updated_at) "
                + "VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)",
            id, name, ORIGIN_AI, APPROVAL_NEW
        );
        return id;
    }

    public Long bindEntityToCategory(MasterEntityType entityType, Long entityId, Long categoryId) {
        String bindingTable = tableFor(entityType) + "_category";
        String entityColumn = tableFor(entityType) + "_id";
        Long id = jdbcTemplate.queryForObject("SELECT nextval('mu." + bindingTable + "_seq')", Long.class);
        jdbcTemplate.update(
            "INSERT INTO mu." + bindingTable + " (id, " + entityColumn + ", category_id, origin, approval_status, created_at, updated_at) "
                + "VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)",
            id, entityId, categoryId, ORIGIN_AI, APPROVAL_NEW
        );
        return id;
    }

    public Long findCategoryByName(String name) {
        return jdbcTemplate.query(
            "SELECT id FROM mu.category WHERE lower(name) = lower(?) LIMIT 1",
            rs -> rs.next() ? rs.getLong("id") : null,
            name
        );
    }

    private String tableFor(MasterEntityType type) {
        return switch (type) {
            case ARTIST -> "artist";
            case ALBUM -> "album";
            case TRACK -> "track";
            case CATEGORY -> "category";
            case PERSON -> "person";
        };
    }
}
