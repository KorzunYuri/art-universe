package yurykorzun.art.universe.music.data.master.dto.binding;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Unified DTO for binding an external entity to an existing internal entity
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class EntityBindToExistingRequestDTO {
    
    @NotNull(message = "Master entity ID is required")
    private Long masterId;
}
