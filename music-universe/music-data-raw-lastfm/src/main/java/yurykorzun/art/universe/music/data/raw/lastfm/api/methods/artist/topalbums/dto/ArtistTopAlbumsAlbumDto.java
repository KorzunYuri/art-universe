package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.artist.topalbums.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.album.common.dto.AlbumDto;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ArtistTopAlbumsAlbumDto extends AlbumDto {

    @JsonProperty("playcount")
    private int playCount;

}
