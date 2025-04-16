package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.artist.toptracks.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.track.common.dto.TrackDto;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true) // ignore artist, among others
public class ArtistTopTracksTrackDto extends TrackDto {

    @JsonProperty("duration")
    private int duration;

    @JsonProperty("streamable")
    private int streamable;

    @JsonProperty("playcount")
    private int playCount;

    @JsonProperty("listeners")
    private int listenersCount;

}
