package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.tag.toptracks.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.track.common.dto.TrackDto;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class TagTopTracksTrackDto extends TrackDto {

    private TagTopTracksTrackArtistDto artist;

}
