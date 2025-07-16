package yurykorzun.art.universe.music.data.approved.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for binding an external track to an existing internal track
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrackBindToExistingRequestDTO {
    
    @NotNull(message = "Track ID is required")
    private Long trackId;
    
    @NotNull(message = "Artist external ID is required")
    private Long artistExternalId;
}
