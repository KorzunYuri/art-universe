package yurykorzun.art.universe.music.data.master.dto.relation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import yurykorzun.art.universe.music.data.master.entity.EntityType;

import java.util.List;

/**
 * DTO for relation binding status information
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RelationBindingStatusDTO {
    // Information about source entity
    private Long sourceExternalId;
    private EntityType sourceEntityType;
    private String sourceEntityName;
    private Long sourceInternalId; // Internal entity ID if bound
    private boolean sourceEntityBound; // Whether source entity is bound
    
    // Information about target entities
    private EntityType targetEntityType;
    private List<TargetEntityBindingDTO> targetBindings;
}
