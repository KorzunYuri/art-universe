package yurykorzun.art.universe.music.data.master.dto.relation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import yurykorzun.art.universe.music.data.master.entity.DataSource;
import yurykorzun.art.universe.common.domain.entity.MasterEntityType;

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
    private MasterEntityType sourceEntityType;
    private MasterEntityType targetEntityType;
}
