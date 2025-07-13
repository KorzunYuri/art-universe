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
    private boolean internalRelationBound; // Whether internal relation between source and target entities exists
    private boolean externalRelationBound; // Whether external relation is bound to the internal relation
    private Long internalRelationId; // Relation ID if bound
}
