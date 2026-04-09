package yurykorzun.art.universe.music.data.master.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlbumWithTracksSaveRequestDTO {

    private Long id;

    @NotBlank(message = "Album name is required")
    private String name;

    private Long primaryArtistId;

    @NotEmpty(message = "Tracks list must not be empty")
    @Valid
    private List<AlbumTrackItemDTO> tracks;
}
