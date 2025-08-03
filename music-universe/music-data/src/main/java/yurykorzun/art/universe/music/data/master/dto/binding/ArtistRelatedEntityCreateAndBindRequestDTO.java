package yurykorzun.art.universe.music.data.master.dto.binding;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Extended DTO for creating and binding entities that are related to an artist (tracks, albums)
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ArtistRelatedEntityCreateAndBindRequestDTO extends EntityCreateAndBindRequestDTO {
    
    @NotNull(message = "Primary artist ID is required")
    private Long primaryArtistId;
}
