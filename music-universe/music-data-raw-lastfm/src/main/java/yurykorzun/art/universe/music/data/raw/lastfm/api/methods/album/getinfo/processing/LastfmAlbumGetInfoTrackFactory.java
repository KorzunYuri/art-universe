package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.album.getinfo.processing;

import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.album.getinfo.dto.AlbumGetInfoTrackDto;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.track.common.processing.LastfmTrackEntityFactory;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.entity.LastfmArtist;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.track.entity.LastfmTrack;

import java.util.Map;

public class LastfmAlbumGetInfoTrackFactory extends LastfmTrackEntityFactory<AlbumGetInfoTrackDto> {

    private final Map<String, LastfmArtist> artistsByUrl;

    public LastfmAlbumGetInfoTrackFactory(Map<String, LastfmArtist> artistsByUrl) {
        this.artistsByUrl = artistsByUrl;
    }

    @Override
    protected LastfmTrack.LastfmTrackBuilder<?, ?> setExtensionFields(
        LastfmTrack.LastfmTrackBuilder<?, ?> builder, 
        AlbumGetInfoTrackDto dto
    ) {
        // Set duration if available
        if (dto.getDuration() > 0) {
            builder.duration(dto.getDuration());
        }
        
        // Set artist reference if available
        if (dto.getArtist() != null && dto.getArtist().getUrl() != null) {
            String artistUrl = dto.getArtist().getUrl();
            LastfmArtist artist = artistsByUrl.get(artistUrl);
            if (artist != null) {
                builder.artist(artist);
            }
        }
        
        return builder;
    }
}
