package yurykorzun.art.universe.music.data.raw.lastfm.task.response.process.mapping.factory.album.getinfo;

import org.springframework.stereotype.Component;
import yurykorzun.art.universe.music.data.raw.lastfm.integration.dto.album.getinfo.AlbumGetInfoTrackArtistDto;
import yurykorzun.art.universe.music.data.raw.lastfm.task.response.process.mapping.factory.artist.LastfmArtistEntityFactory;

@Component
public class LastfmAlbumGetInfoTrackArtistFactory extends LastfmArtistEntityFactory<AlbumGetInfoTrackArtistDto> {
}
