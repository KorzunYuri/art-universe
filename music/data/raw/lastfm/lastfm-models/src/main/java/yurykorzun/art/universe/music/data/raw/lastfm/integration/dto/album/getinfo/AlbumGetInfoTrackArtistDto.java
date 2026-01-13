package yurykorzun.art.universe.music.data.raw.lastfm.integration.dto.album.getinfo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import yurykorzun.art.universe.music.data.raw.lastfm.integration.dto.artist.ArtistDto;

@Data
@EqualsAndHashCode(callSuper=true)
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AlbumGetInfoTrackArtistDto extends ArtistDto {
    // inherits name, url & mbid from base dto
}
