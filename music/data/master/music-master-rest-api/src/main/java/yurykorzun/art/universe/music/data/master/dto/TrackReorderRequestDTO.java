package yurykorzun.art.universe.music.data.master.dto;

import jakarta.validation.Valid;
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
public class TrackReorderRequestDTO {

    @NotEmpty(message = "At least one item is required")
    @Valid
    private List<TrackReorderItemDTO> items;
}
