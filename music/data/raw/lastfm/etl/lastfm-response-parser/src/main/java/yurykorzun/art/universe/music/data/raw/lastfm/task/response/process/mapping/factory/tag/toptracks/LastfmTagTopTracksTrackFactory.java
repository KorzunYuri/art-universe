package yurykorzun.art.universe.music.data.raw.lastfm.task.response.process.mapping.factory.tag.toptracks;

import yurykorzun.art.universe.music.data.raw.lastfm.task.response.process.LastfmApiDtoProcessingResult;
import yurykorzun.art.universe.music.data.raw.lastfm.integration.dto.tag.toptracks.TagTopTracksTrackArtistDto;
import yurykorzun.art.universe.music.data.raw.lastfm.integration.dto.tag.toptracks.TagTopTracksTrackDto;
import yurykorzun.art.universe.music.data.raw.lastfm.task.response.process.mapping.factory.track.LastfmTrackEntityFactory;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.LastfmArtist;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.LastfmTrack;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class LastfmTagTopTracksTrackFactory extends LastfmTrackEntityFactory<TagTopTracksTrackDto> {

    private final Map<String, LastfmArtist> artistsByName;

    /**
     * Creates a factory with a map of artists by name for setting the artist field in tracks
     * 
     * @param artistsResult Result of processing artists, containing saved artists
     */
    public LastfmTagTopTracksTrackFactory(LastfmApiDtoProcessingResult<LastfmArtist, TagTopTracksTrackArtistDto> artistsResult) {
        this.artistsByName = artistsResult.actualEntities().stream()
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
            .duration(dto.getDuration());
    }
}
