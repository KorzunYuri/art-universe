package yurykorzun.art.universe.music.data.approved.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrackBindingRequestDTO {
    
    @NotBlank(message = "Track name is required")
    private String name;
    
    @NotNull(message = "Artist external ID is required")
    private Long artistExternalId;
}
