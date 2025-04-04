package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.track.common.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.artist.common.dto.ArtistDto;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.dto.EntityDto;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.dto.RankInfo;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class TrackDto implements EntityDto {

    private String name;

    private String mbid;

    private String url;

    private int duration;

    private ArtistDto artist;

    private TrackStreamableAttrDto streamable;

    @JsonProperty("@attr")
    private RankInfo rankInfo;

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getUniqueKey() {
        return url;
    }
}
