package yurykorzun.art.universe.music.data.raw.lastfm.domain.service.relationship;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.function.Function;

/**
 * Metadata for entity relation upsert operations
 */
@Data
@Builder
public class EntityRelationMetadata<T> {
    private String tableName;
    private List<String> insertColumns;
    private List<String> conflictColumns;
    private Function<T, Object[]> parameterMapper;
}
