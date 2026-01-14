package yurykorzun.art.universe.music.data.raw.lastfm.task.response.process.mapping.factory.tag.toptracks;

import org.springframework.stereotype.Component;
import yurykorzun.art.universe.music.data.raw.lastfm.task.response.process.mapping.factory.artist.LastfmArtistEntityFactory;
import yurykorzun.art.universe.music.data.raw.lastfm.integration.dto.tag.toptracks.TagTopTracksTrackArtistDto;

@Component
public class LastfmTagTopTracksArtistFactory extends LastfmArtistEntityFactory<TagTopTracksTrackArtistDto> {
}
