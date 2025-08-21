package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.album.common.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.dto.ArtistScoped;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.dto.EntityDto;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.album.entity.LastfmAlbum;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.entity.LastfmEntityType;

import java.beans.Transient;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class AlbumDto implements EntityDto<LastfmAlbum>, ArtistScoped {

    @JsonProperty("name")
    private String name;

    @JsonProperty("url")
    private String url;

    @JsonProperty("mbid")
    private String mbid;

    @Override
    @Transient
    public LastfmEntityType getEntityType() {
        return LastfmEntityType.ALBUM;
    }
}
