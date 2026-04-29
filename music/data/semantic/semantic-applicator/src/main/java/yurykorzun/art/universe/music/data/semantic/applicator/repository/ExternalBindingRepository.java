package yurykorzun.art.universe.music.data.semantic.applicator.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import yurykorzun.art.universe.common.domain.entity.MasterEntityType;
import yurykorzun.art.universe.music.data.master.model.MasterApprovalStatus;
import yurykorzun.art.universe.music.data.master.model.Origin;

@Repository
public class ExternalBindingRepository {

    private static final int ORIGIN_AI = Origin.AI.getCode();
    private static final int APPROVAL_NEW = MasterApprovalStatus.NEW.getCode();

    private final JdbcTemplate jdbcTemplate;

    public ExternalBindingRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Long createBinding(MasterEntityType entityType, Long masterId, int dataSourceCode, long externalId) {
        String table = bindingTableFor(entityType);
        Long id = jdbcTemplate.queryForObject("SELECT nextval('mu." + table + "_seq')", Long.class);
        jdbcTemplate.update(
            "INSERT INTO mu." + table
                + " (id, master_id, data_source_id, external_id, origin, approval_status, created_at, updated_at) "
                + "VALUES (?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)",
            id, masterId, dataSourceCode, externalId, ORIGIN_AI, APPROVAL_NEW
        );
        return id;
    }

    private String bindingTableFor(MasterEntityType entityType) {
        return switch (entityType) {
            case ARTIST -> "artist_binding";
            case ALBUM -> "album_binding";
            case TRACK -> "track_binding";
            case CATEGORY -> "category_binding";
            default -> throw new IllegalArgumentException("No external binding table for " + entityType);
        };
    }
}
