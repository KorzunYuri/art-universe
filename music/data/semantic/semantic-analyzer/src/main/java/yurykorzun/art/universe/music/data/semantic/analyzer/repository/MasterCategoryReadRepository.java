package yurykorzun.art.universe.music.data.semantic.analyzer.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

/**
 * Read-only access to the {@code mu.category} schema owned by the music-data-master service.
 * Uses JDBC because this module does not own JPA mappings for master-data tables.
 */
@Repository
public class MasterCategoryReadRepository {

    private final JdbcTemplate jdbcTemplate;

    public MasterCategoryReadRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<CategoryRow> findAll() {
        List<CategoryRow> rows = new ArrayList<>();
        jdbcTemplate.query(
            """
            SELECT c.id, c.name, cc.source_category_id AS parent_id
            FROM mu.category c
            LEFT JOIN mu.category_category cc ON c.id = cc.target_category_id
            """,
            rs -> {
                long id = rs.getLong("id");
                String name = rs.getString("name");
                long parentId = rs.getLong("parent_id");
                Long parent = rs.wasNull() ? null : parentId;
                rows.add(new CategoryRow(id, name, parent));
            }
        );
        return rows;
    }

    public record CategoryRow(long id, String name, Long parentId) {}
}
