package yurykorzun.art.universe.music.data.raw.lastfm.integration.dto.track.getinfo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import yurykorzun.art.universe.music.data.raw.lastfm.integration.dto.artist.ArtistDto;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class TrackGetInfoArtistDto extends ArtistDto {
    // Extends ArtistDto with name, mbid, url
}
