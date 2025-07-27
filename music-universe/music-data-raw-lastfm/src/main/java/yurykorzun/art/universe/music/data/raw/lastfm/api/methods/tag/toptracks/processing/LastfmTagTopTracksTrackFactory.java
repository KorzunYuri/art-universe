package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.tag.toptracks.processing;

import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.LastfmApiDtoProcessingResult;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.tag.toptracks.dto.TagTopTracksTrackArtistDto;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.tag.toptracks.dto.TagTopTracksTrackDto;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.track.common.LastfmTrackEntityFactory;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.entity.LastfmArtist;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.track.entity.LastfmTrack;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

class LastfmTagTopTracksTrackFactory extends LastfmTrackEntityFactory<TagTopTracksTrackDto> {

    private final Map<String, LastfmArtist> artistsByName;

    /**
     * Creates a factory with a map of artists by name for setting the artist field in tracks
     * 
     * @param artistsResult Result of processing artists, containing saved artists
     */
    public LastfmTagTopTracksTrackFactory(LastfmApiDtoProcessingResult<LastfmArtist, TagTopTracksTrackArtistDto> artistsResult) {
        this.artistsByName = artistsResult.savedEntities().stream()
            .collect(Collectors.toMap(LastfmArtist::getName, Function.identity()));
    }

    @Override
    protected LastfmTrack.LastfmTrackBuilder<?, ?> setExtensionFields(LastfmTrack.LastfmTrackBuilder<?, ?> builder, TagTopTracksTrackDto dto) {
        // Find artist by name and set it directly in the track
        if (dto.getArtist() != null) {
            String artistName = dto.getArtist().getName();
            LastfmArtist artist = artistsByName.get(artistName);
            if (artist != null) {
                builder.artist(artist);
            }
        }
        
        return builder
            .duration(dto.getDuration())
            .isStreamable(1 == dto.getStreamableObject().getFullTrack());
    }
}
