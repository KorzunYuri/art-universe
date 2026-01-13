package yurykorzun.art.universe.music.data.raw.lastfm.task.response.process.mapping.factory.artist.toptracks;

import org.springframework.stereotype.Component;
import yurykorzun.art.universe.music.data.raw.lastfm.task.response.process.mapping.factory.artist.LastfmArtistEntityFactory;
import yurykorzun.art.universe.music.data.raw.lastfm.integration.dto.artist.toptracks.ArtistTopTracksTrackArtistDto;

/**
 * Factory for creating LastfmArtist entities from ArtistTopTracksTrackArtistDto objects
 */
@Component
public class LastfmArtistTopTracksArtistFactory extends LastfmArtistEntityFactory<ArtistTopTracksTrackArtistDto> {
}
