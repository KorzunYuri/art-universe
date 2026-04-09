package yurykorzun.art.universe.music.data.master.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import yurykorzun.art.universe.common.domain.entity.MasterEntityType;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RelationTypeApplicabilityDTO {

    private Long id;
    private Long relationTypeId;
    private MasterEntityType sourceEntityType;
    private MasterEntityType targetEntityType;

    @JsonProperty("isDefault")
    private boolean isDefault;
}
