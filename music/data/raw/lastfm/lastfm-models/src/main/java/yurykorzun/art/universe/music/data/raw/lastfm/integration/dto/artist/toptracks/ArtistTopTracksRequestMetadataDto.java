package yurykorzun.art.universe.music.data.raw.lastfm.integration.dto.artist.toptracks;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ArtistTopTracksRequestMetadataDto {

    @JsonProperty("artist")
    private String artistName;

    @JsonProperty("page")
    private int pageNumber;

    @JsonProperty("perPage")
    private int pageSize;

    @JsonProperty("totalPages")
    private int pagesTotal;

    @JsonProperty("total")
    private int tracksTotal;
}
