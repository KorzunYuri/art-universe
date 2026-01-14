package yurykorzun.art.universe.music.data.raw.lastfm.integration.dto.tag.toptracks;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import yurykorzun.art.universe.music.data.raw.lastfm.integration.dto.artist.ArtistDto;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class TagTopTracksTrackArtistDto extends ArtistDto {

    private int streamable;

}
