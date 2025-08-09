package yurykorzun.art.universe.music.data.master.dto.relation;

import jakarta.annotation.Nullable;
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
    private boolean isSourceEntityBound;
    @Nullable private Long sourceInternalId;
    
    // Information about target entities
    private EntityType targetEntityType;
    private List<TargetEntityBindingDTO> targetBindings;
}
