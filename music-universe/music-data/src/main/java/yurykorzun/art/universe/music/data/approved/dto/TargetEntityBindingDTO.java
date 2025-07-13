package yurykorzun.art.universe.music.data.approved.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for target entity binding information
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TargetEntityBindingDTO {
    private Long targetExternalId;
    private String targetEntityName;
    private Long targetInternalId; // Internal entity ID if bound
    private boolean targetEntityBound; // Whether target entity is bound
    private boolean relationBound; // Whether relation between source and target entities is bound
    private Long relationId; // Relation ID if bound
}
