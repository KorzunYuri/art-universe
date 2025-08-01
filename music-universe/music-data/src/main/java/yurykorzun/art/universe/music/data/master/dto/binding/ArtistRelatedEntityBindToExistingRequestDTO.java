package yurykorzun.art.universe.music.data.master.dto.binding;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Extended DTO for binding entities that are related to an artist (tracks, albums) to existing entities
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ArtistRelatedEntityBindToExistingRequestDTO extends EntityBindToExistingRequestDTO {
    
    @NotNull(message = "Primary artist ID is required")
    private Long primaryArtistId;
}
