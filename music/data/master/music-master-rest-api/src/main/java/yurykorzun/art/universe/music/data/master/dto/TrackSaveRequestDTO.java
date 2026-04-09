package yurykorzun.art.universe.music.data.master.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrackSaveRequestDTO {

    private Long id;

    @NotBlank(message = "Track name is required")
    private String name;

    private Long primaryArtistId;
}
