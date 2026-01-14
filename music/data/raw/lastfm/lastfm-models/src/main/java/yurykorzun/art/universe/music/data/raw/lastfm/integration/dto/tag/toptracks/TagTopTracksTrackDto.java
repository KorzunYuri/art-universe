package yurykorzun.art.universe.music.data.raw.lastfm.integration.dto.tag.toptracks;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import yurykorzun.art.universe.music.data.raw.lastfm.integration.dto.RankInfo;
import yurykorzun.art.universe.music.data.raw.lastfm.integration.dto.track.TrackDto;

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
