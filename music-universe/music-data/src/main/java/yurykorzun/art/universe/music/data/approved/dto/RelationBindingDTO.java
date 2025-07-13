package yurykorzun.art.universe.music.data.approved.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import yurykorzun.art.universe.music.data.approved.entity.DataSource;
import yurykorzun.art.universe.music.data.approved.entity.EntityType;

/**
 * DTO for relation binding information
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RelationBindingDTO {
    private Long sourceExternalId;
    private Long targetExternalId;
    private DataSource dataSource;
    private Long relationId;
    private String sourceEntityName;
    private String targetEntityName;
    private EntityType sourceEntityType;
    private EntityType targetEntityType;
}
