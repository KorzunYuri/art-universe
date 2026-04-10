package yurykorzun.art.universe.music.data.semantic.analyzer.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Array;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Read-only access to the {@code mu.attribute_def} schema owned by the music-data-master service.
 * Returns only semantic-origin attribute definitions (computation_type = SEMANTIC).
 */
@Repository
public class MasterAttributeDefReadRepository {

    /** Matches AttributeComputationType.SEMANTIC code in music-data-master. */
    private static final int COMPUTATION_TYPE_SEMANTIC = 4;

    private final JdbcTemplate jdbcTemplate;

    public MasterAttributeDefReadRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<AttributeDefRow> findSemanticAttributeDefs() {
        List<AttributeDefRow> rows = new ArrayList<>();
        jdbcTemplate.query(
            """
            SELECT d.code, d.name, d.description, d.data_type, d.temporal_type,
                   d.is_multi_value,
                   array_agg(a.target_type ORDER BY a.target_type) AS target_types
            FROM mu.attribute_def d
            JOIN mu.attribute_def_applicability a ON d.id = a.attribute_def_id
            WHERE d.computation_type = ?
            GROUP BY d.id, d.code, d.name, d.description, d.data_type, d.temporal_type, d.is_multi_value
            ORDER BY d.id
            """,
            rs -> {
                String code = rs.getString("code");
                String name = rs.getString("name");
                String description = rs.getString("description");
                int dataType = rs.getInt("data_type");
                int temporalType = rs.getInt("temporal_type");
                boolean multiValue = rs.getBoolean("is_multi_value");
                int[] targetTypeCodes = toIntArray(rs.getArray("target_types"));
                rows.add(new AttributeDefRow(code, name, description, dataType, temporalType, multiValue, targetTypeCodes));
            },
            COMPUTATION_TYPE_SEMANTIC
        );
        return rows;
    }

    private int[] toIntArray(Array sqlArray) throws SQLException {
        if (sqlArray == null) {
            return new int[0];
        }
        Integer[] arr = (Integer[]) sqlArray.getArray();
        int[] result = new int[arr.length];
        for (int i = 0; i < arr.length; i++) {
            result[i] = arr[i];
        }
        return result;
    }

    public record AttributeDefRow(
        String code,
        String name,
        String description,
        int dataTypeCode,
        int temporalTypeCode,
        boolean multiValue,
        int[] targetTypeCodes
    ) {}
}
