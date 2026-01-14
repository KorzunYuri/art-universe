package yurykorzun.art.universe.music.data.raw.lastfm.task.response.process.mapping.factory.album.getinfo;

import yurykorzun.art.universe.music.data.raw.lastfm.integration.dto.album.getinfo.AlbumGetInfoTrackDto;
import yurykorzun.art.universe.music.data.raw.lastfm.task.response.process.mapping.factory.track.LastfmTrackEntityFactory;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.LastfmArtist;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.LastfmTrack;

import java.util.Map;

public class LastfmAlbumGetInfoTrackFactory extends LastfmTrackEntityFactory<AlbumGetInfoTrackDto> {

    private final Map<String, LastfmArtist> artistsByName;

    public LastfmAlbumGetInfoTrackFactory(Map<String, LastfmArtist> artistsByName) {
        this.artistsByName = artistsByName;
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
        if (dto.getArtist() != null) {
            String artistName = dto.getArtist().getName();
            LastfmArtist artist = artistsByName.get(artistName);
            if (artist != null) {
                builder.artist(artist);
            }
        }
        
        return builder;
    }
}
