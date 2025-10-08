package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.album.getinfo.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.artist.common.dto.ArtistDto;

@Data
@EqualsAndHashCode(callSuper=true)
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AlbumGetInfoTrackArtistDto extends ArtistDto {
    // inherits name, url & mbid from base dto
}
