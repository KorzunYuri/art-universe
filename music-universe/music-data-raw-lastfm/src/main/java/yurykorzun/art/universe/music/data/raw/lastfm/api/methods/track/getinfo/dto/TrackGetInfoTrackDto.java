package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.track.getinfo.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.track.common.dto.TrackDto;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class TrackGetInfoTrackDto extends TrackDto {

    @JsonProperty("duration")
    private int duration;

    @JsonProperty("listeners")
    private int listeners;

    @JsonProperty("playcount")
    private long playcount;

    @JsonProperty("artist")
    private TrackGetInfoArtistDto artist;

    @JsonProperty("album")
    private TrackGetInfoAlbumDto album;

    @JsonProperty("toptags")
    private TrackGetInfoTagsDto topTags;
}
