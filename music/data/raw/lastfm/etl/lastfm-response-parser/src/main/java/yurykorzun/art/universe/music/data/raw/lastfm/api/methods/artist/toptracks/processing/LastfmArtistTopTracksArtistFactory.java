package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.artist.toptracks.processing;

import org.springframework.stereotype.Component;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.artist.common.processing.LastfmArtistEntityFactory;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.artist.toptracks.dto.ArtistTopTracksTrackArtistDto;

/**
 * Factory for creating LastfmArtist entities from ArtistTopTracksTrackArtistDto objects
 */
@Component
public class LastfmArtistTopTracksArtistFactory extends LastfmArtistEntityFactory<ArtistTopTracksTrackArtistDto> {
}
