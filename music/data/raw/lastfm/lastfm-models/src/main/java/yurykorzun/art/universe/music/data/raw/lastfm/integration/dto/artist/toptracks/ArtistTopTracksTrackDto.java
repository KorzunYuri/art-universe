package yurykorzun.art.universe.music.data.raw.lastfm.integration.dto.artist.toptracks;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import yurykorzun.art.universe.music.data.raw.lastfm.integration.dto.track.TrackDto;
import yurykorzun.art.universe.music.data.raw.lastfm.integration.dto.track.TrackDtoWithMetrics;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ArtistTopTracksTrackDto extends TrackDto implements TrackDtoWithMetrics {

    @JsonProperty("artist")
    private ArtistTopTracksTrackArtistDto artist;

    @JsonProperty("streamable")
    private Integer streamable;

    @JsonProperty("playcount")
    private Long playCount;

    @JsonProperty("listeners")
    private Integer listenersCount;
    
    @Override
    public String getArtistName() {
        return artist != null ? artist.getName() : null;
    }
}
