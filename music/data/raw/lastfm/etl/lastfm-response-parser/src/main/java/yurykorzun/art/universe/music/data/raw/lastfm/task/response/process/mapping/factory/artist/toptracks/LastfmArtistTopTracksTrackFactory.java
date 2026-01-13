package yurykorzun.art.universe.music.data.raw.lastfm.task.response.process.mapping.factory.artist.toptracks;

import yurykorzun.art.universe.music.data.raw.lastfm.integration.dto.artist.toptracks.ArtistTopTracksTrackArtistDto;
import yurykorzun.art.universe.music.data.raw.lastfm.integration.dto.artist.toptracks.ArtistTopTracksTrackDto;
import yurykorzun.art.universe.music.data.raw.lastfm.task.response.process.LastfmApiDtoProcessingResult;
import yurykorzun.art.universe.music.data.raw.lastfm.task.response.process.mapping.EntityMapping;
import yurykorzun.art.universe.music.data.raw.lastfm.task.response.process.mapping.factory.track.LastfmTrackEntityFactory;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.LastfmArtist;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.LastfmTrack;

/**
 * Factory for creating LastfmTrack entities from ArtistTopTracksTrackDto objects
 * Uses artist information from track metadata
 */
public class LastfmArtistTopTracksTrackFactory extends LastfmTrackEntityFactory<ArtistTopTracksTrackDto> {

    private final LastfmApiDtoProcessingResult<LastfmArtist, ArtistTopTracksTrackArtistDto> artistMappingResult;

    /**
     * Creates a factory with artist mapping results for setting the artist field in tracks
     * 
     * @param artistMappingResult Result of processing artists, containing saved artists
     */
    public LastfmArtistTopTracksTrackFactory(
        LastfmApiDtoProcessingResult<LastfmArtist, ArtistTopTracksTrackArtistDto> artistMappingResult
    ) {
        this.artistMappingResult = artistMappingResult;
    }

    @Override
    protected LastfmTrack.LastfmTrackBuilder<?, ?> setExtensionFields(LastfmTrack.LastfmTrackBuilder<?, ?> builder, ArtistTopTracksTrackDto dto) {
        // Set basic track properties
        builder
            .playCount(dto.getPlayCount())
            .listenersCount(dto.getListenersCount());
        
        // Find artist by name from the mapping result and set it in the track
        if (dto.getArtist() != null) {
            var artistDto = dto.getArtist();
            EntityMapping<LastfmArtist, ArtistTopTracksTrackArtistDto> artistMapping = 
                artistMappingResult.entityMapping().get(artistDto);
            
            if (artistMapping != null && artistMapping.getNewEntity() != null) {
                builder.artist(artistMapping.getNewEntity());
            }
        }
        
        return builder;
    }
}
