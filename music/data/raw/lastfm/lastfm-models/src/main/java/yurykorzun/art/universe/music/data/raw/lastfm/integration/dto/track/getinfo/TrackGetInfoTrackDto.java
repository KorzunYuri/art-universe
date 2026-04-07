package yurykorzun.art.universe.music.data.raw.lastfm.integration.dto.track.getinfo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import yurykorzun.art.universe.music.data.raw.lastfm.integration.dto.WikiDto;
import yurykorzun.art.universe.music.data.raw.lastfm.integration.dto.track.TrackDto;
import yurykorzun.art.universe.music.data.raw.lastfm.integration.dto.track.TrackDtoWithMetrics;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class TrackGetInfoTrackDto extends TrackDto implements TrackDtoWithMetrics {

    @JsonProperty("duration")
    private Integer duration;

    @JsonProperty("listeners")
    private Integer listenersCount;

    @JsonProperty("playcount")
    private Long playCount;

    @JsonProperty("artist")
    private TrackGetInfoArtistDto artist;

    @JsonProperty("album")
    private TrackGetInfoAlbumDto album;

    @JsonProperty("toptags")
    private TrackGetInfoTagsDto topTags;

    @JsonProperty("wiki")
    private WikiDto wiki;

    @Override
    public String getArtistName() {
        return artist != null ? artist.getName() : null;
    }
}
