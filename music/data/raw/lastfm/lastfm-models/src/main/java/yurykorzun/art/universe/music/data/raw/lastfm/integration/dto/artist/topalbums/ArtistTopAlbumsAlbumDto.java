package yurykorzun.art.universe.music.data.raw.lastfm.integration.dto.artist.topalbums;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import yurykorzun.art.universe.music.data.raw.lastfm.integration.dto.album.AlbumDto;
import yurykorzun.art.universe.music.data.raw.lastfm.integration.dto.album.AlbumDtoWithMetrics;
import yurykorzun.art.universe.music.data.raw.lastfm.integration.dto.artist.ArtistDto;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ArtistTopAlbumsAlbumDto extends AlbumDto implements AlbumDtoWithMetrics {

    @JsonProperty("playcount")
    private Long playCount;

    @JsonProperty("artist")
    private ArtistDto artist;

    @Override
    public String getArtistName() {
        return artist != null ? artist.getName() : null;
    }
}
