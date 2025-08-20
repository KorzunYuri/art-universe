package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.track.common.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.dto.EntityDto;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.dto.ArtistScoped;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.entity.LastfmEntityType;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.track.entity.LastfmTrack;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class TrackDto implements EntityDto<LastfmTrack>, ArtistScoped {

    private String name;

    private String mbid;

    private String url;

    @Override
    public LastfmEntityType getEntityType() {
        return LastfmEntityType.TRACK;
    }
}
