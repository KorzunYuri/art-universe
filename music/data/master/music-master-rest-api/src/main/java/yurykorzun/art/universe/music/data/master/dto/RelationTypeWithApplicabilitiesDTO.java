package yurykorzun.art.universe.music.data.master.dto;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class RelationTypeWithApplicabilitiesDTO extends RelationTypeDTO {
    private List<RelationTypeApplicabilityDTO> applicabilities;
}
