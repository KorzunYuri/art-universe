package yurykorzun.art.universe.music.data.raw.lastfm.integration.dto.album.getinfo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import yurykorzun.art.universe.music.data.raw.lastfm.integration.dto.DtoRoot;

@Data
@NoArgsConstructor
public class AlbumGetInfoDtoRoot implements DtoRoot {

    @JsonProperty("album")
    private AlbumGetInfoAlbumDto album;
}
