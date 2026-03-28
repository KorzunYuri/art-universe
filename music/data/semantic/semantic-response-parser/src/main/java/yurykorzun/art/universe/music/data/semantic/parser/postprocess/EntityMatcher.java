package yurykorzun.art.universe.music.data.semantic.parser.postprocess;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class EntityMatcher {

    private static final Logger log = LoggerFactory.getLogger(EntityMatcher.class);
    private static final double SIMILARITY_THRESHOLD = 0.6;

    private final JdbcTemplate jdbcTemplate;

    public EntityMatcher(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<MatchResult> findMatches(String entityType, String candidateName) {
        String tableName = resolveTableName(entityType);
        if (tableName == null) {
            return List.of();
        }

        return jdbcTemplate.query(
            String.format(
                "SELECT id, name, similarity(lower(name), lower(?)) as sim " +
                "FROM mu.%s WHERE similarity(lower(name), lower(?)) > ? " +
                "ORDER BY sim DESC LIMIT 5",
                tableName
            ),
            (rs, rowNum) -> new MatchResult(
                rs.getLong("id"),
                rs.getString("name"),
                rs.getDouble("sim")
            ),
            candidateName, candidateName, SIMILARITY_THRESHOLD
        );
    }

    private String resolveTableName(String entityType) {
        return switch (entityType.toLowerCase()) {
            case "artist" -> "artist";
            case "album" -> "album";
            case "track" -> "track";
            case "category" -> "category";
            default -> null;
        };
    }

    public record MatchResult(long id, String name, double similarity) {
    }
}
