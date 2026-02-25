package yurykorzun.art.universe.music.data.master.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrackReorderItemDTO {

    @NotNull(message = "Album track ID is required")
    private Long albumTrackId;

    @NotNull(message = "New order is required")
    @Min(value = 1, message = "New order must be at least 1")
    private Integer newOrder;
}
