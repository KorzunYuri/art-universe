package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.tag.toptracks.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.dto.RankInfo;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.track.common.dto.TrackDto;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class TagTopTracksTrackDto extends TrackDto {

    @JsonProperty("artist")
    private TagTopTracksTrackArtistDto artist;

    @JsonProperty("duration")
    private int duration;

    @JsonProperty("@attr")
    private RankInfo rankInfo;
    
    @Override
    public String getArtistName() {
        return artist != null ? artist.getName() : null;
    }
}
