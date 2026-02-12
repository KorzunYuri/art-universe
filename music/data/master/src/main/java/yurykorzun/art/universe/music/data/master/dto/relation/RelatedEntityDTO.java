package yurykorzun.art.universe.music.data.master.dto.relation;

import lombok.*;
import lombok.experimental.SuperBuilder;
import yurykorzun.art.universe.music.data.master.dto.BaseEntityDto;
import yurykorzun.art.universe.music.data.master.entity.MasterEntityType;

/**
 * DTO for entity information
 */
@Data
@EqualsAndHashCode(callSuper=false)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class RelatedEntityDTO extends BaseEntityDto {
    private MasterEntityType entityType;
    private Long relationTypeId;
    private String relationTypeName;
}
