package yurykorzun.art.universe.music.data.master.dto;

import lombok.*;
import yurykorzun.art.universe.music.data.master.entity.MasterEntityType;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RelationTypeApplicabilityDTO {
    private Long id;
    private Long relationTypeId;
    private MasterEntityType sourceEntityType;
    private MasterEntityType targetEntityType;
    private boolean isDefault;
}
