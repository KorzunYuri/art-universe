package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.artist.topalbums.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ArtistTopAlbumsTopAlbumsDto {

    @JsonProperty("album")
    private List<ArtistTopAlbumAlbumDto> albums;

}
