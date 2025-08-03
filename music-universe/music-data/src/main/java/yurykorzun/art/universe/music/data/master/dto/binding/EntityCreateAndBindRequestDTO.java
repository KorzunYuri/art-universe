package yurykorzun.art.universe.music.data.master.dto.binding;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Unified DTO for creating a new internal entity and binding it to an external entity
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class EntityCreateAndBindRequestDTO {
    
    @NotBlank(message = "Entity name is required")
    private String entityName;
}
