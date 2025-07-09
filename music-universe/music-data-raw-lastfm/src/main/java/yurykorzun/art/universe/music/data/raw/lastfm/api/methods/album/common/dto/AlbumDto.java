package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.album.common.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.dto.EntityDto;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.album.entity.LastfmAlbum;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AlbumDto implements EntityDto<LastfmAlbum> {

    @JsonProperty("name")
    private String name;

    @JsonProperty("url")
    private String url;

    @JsonProperty("mbid")
    private String mbid;

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getUniqueKey() {
        return url;
    }
}
