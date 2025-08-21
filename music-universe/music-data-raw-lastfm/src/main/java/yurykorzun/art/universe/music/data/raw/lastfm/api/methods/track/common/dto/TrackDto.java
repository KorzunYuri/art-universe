package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.track.common.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.dto.EntityDto;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.dto.ArtistScoped;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.entity.LastfmEntityType;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.track.entity.LastfmTrack;

import java.beans.Transient;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class TrackDto implements EntityDto<LastfmTrack>, ArtistScoped {

    @JsonProperty("name")
    private String name;

    @JsonProperty("mbid")
    private String mbid;

    @JsonProperty("url")
    private String url;

    @Transient
    @Override
    public LastfmEntityType getEntityType() {
        return LastfmEntityType.TRACK;
    }
}
