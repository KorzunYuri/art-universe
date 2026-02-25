package yurykorzun.art.universe.music.data.master.dto.relation;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.annotation.Nullable;
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
    @JsonProperty("isTargetEntityBound")
    private boolean isTargetEntityBound;
    @JsonProperty("isInternalRelationBound")
    private boolean isInternalRelationBound;
    @JsonProperty("isExternalRelationBound")
    private boolean isExternalRelationBound;
    @Nullable
    private Long targetInternalId; // if bound
    @Nullable
    private Long internalRelationId; // if bound
}
