package yurykorzun.art.universe.music.data.master.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlbumTrackItemDTO {

    @NotNull(message = "Track ID is required")
    private Long trackId;

    @NotNull(message = "Track order is required")
    private Integer trackOrder;

    private Long relationTypeId;
}
