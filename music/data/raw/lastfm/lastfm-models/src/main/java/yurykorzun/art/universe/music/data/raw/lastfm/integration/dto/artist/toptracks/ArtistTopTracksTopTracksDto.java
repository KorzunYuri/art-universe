package yurykorzun.art.universe.music.data.raw.lastfm.integration.dto.artist.toptracks;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class ArtistTopTracksTopTracksDto {

    @JsonProperty("track")
    List<ArtistTopTracksTrackDto> tracks;

    @JsonProperty("@attr")
    ArtistTopTracksRequestMetadataDto artistMetadata;

}
