package yurykorzun.art.universe.music.data.raw.lastfm.integration.dto.artist.search;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import yurykorzun.art.universe.music.data.raw.lastfm.integration.dto.artist.ArtistDto;
import yurykorzun.art.universe.music.data.raw.lastfm.integration.dto.artist.ArtistDtoWithMetrics;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ArtistSearchArtistDto extends ArtistDto implements ArtistDtoWithMetrics {

    @JsonProperty("listeners")
    private Integer listenersCount;

    @JsonProperty("streamable")
    private Integer streamable;

}
