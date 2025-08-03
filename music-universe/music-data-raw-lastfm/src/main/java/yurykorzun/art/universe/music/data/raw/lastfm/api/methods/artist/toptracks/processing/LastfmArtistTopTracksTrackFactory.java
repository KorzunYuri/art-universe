package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.artist.toptracks.processing;

import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.artist.toptracks.dto.ArtistTopTracksTrackArtistDto;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.artist.toptracks.dto.ArtistTopTracksTrackDto;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.LastfmApiDtoProcessingResult;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.mapping.EntityMapping;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.track.common.processing.LastfmTrackEntityFactory;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.entity.LastfmArtist;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.track.entity.LastfmTrack;

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
            String artistName = dto.getArtist().getName();
            EntityMapping<LastfmArtist, ArtistTopTracksTrackArtistDto> artistMapping = 
                artistMappingResult.entityMapping().get(artistName);
            
            if (artistMapping != null && artistMapping.getNewEntity() != null) {
                builder.artist(artistMapping.getNewEntity());
            }
        }
        
        return builder;
    }
}
