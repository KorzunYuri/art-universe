package yurykorzun.art.universe.music.data.raw.lastfm.integration.dto.artist.toptracks;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import yurykorzun.art.universe.music.data.raw.lastfm.integration.dto.artist.ArtistDto;

/**
 * DTO for artist information within a track from artist.getTopTracks response
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ArtistTopTracksTrackArtistDto extends ArtistDto {
}
