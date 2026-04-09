package yurykorzun.art.universe.music.data.master.dto.relation;

import lombok.*;
import lombok.experimental.SuperBuilder;
import yurykorzun.art.universe.common.domain.entity.EntityType;
import yurykorzun.art.universe.music.data.master.dto.BaseEntityDto;

import java.util.List;

/**
 * DTO for entity information
 */
@Data
@EqualsAndHashCode(callSuper=false)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class RelatedEntityDTO extends BaseEntityDto {
    private EntityType entityType;
    private List<RelationTypeInfoDTO> relationTypes;
    private Integer trackOrder;
}
