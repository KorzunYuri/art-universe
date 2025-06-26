package yurykorzun.art.universe.music.data.approved.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArtistSearchResultDTO {
    private Long id;
    private String name;
}
