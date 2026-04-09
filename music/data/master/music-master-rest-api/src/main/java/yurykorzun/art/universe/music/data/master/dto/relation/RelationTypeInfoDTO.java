package yurykorzun.art.universe.music.data.master.dto.relation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RelationTypeInfoDTO {
    private Long relationId;
    private Long relationTypeId;
    private String relationTypeName;
}
