package yurykorzun.art.universe.music.data.raw.spotify.task.response.process.processor;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import yurykorzun.art.universe.music.data.raw.spotify.enums.SpotifyEntityType;
import yurykorzun.art.universe.music.data.raw.spotify.enums.SpotifyRelationType;
import yurykorzun.art.universe.music.data.raw.spotify.etl.entity.SpotifyApiCallType;
import yurykorzun.art.universe.music.data.raw.spotify.etl.entity.SpotifyApiResponse;
import yurykorzun.art.universe.music.data.raw.spotify.etl.entity.StagingIteration;
import yurykorzun.art.universe.music.data.raw.spotify.integration.dto.SpotifyAlbumDto;
import yurykorzun.art.universe.music.data.raw.spotify.integration.dto.SpotifySimplifiedArtistDto;
import yurykorzun.art.universe.music.data.raw.spotify.integration.dto.SpotifyTrackDto;
import yurykorzun.art.universe.music.data.raw.spotify.staging.StagingWriter;
import yurykorzun.art.universe.music.data.raw.spotify.staging.SyntheticIdResolutionService;
import yurykorzun.art.universe.music.data.raw.spotify.task.response.process.BaseSpotifyApiResponseProcessor;

import java.io.IOException;
import java.util.List;

@Component
@Slf4j
public class SpotifyAlbumGetResponseProcessor extends BaseSpotifyApiResponseProcessor {

    private final ObjectMapper objectMapper;
    private final StagingWriter stagingWriter;
    private final SyntheticIdResolutionService idResolutionService;

    public SpotifyAlbumGetResponseProcessor(
        ObjectMapper objectMapper,
        StagingWriter stagingWriter,
        SyntheticIdResolutionService idResolutionService
    ) {
        this.objectMapper = objectMapper;
        this.stagingWriter = stagingWriter;
        this.idResolutionService = idResolutionService;
    }

    @Override
    public SpotifyApiCallType getApiCallType() {
        return SpotifyApiCallType.ALBUM_GET;
    }

    @Override
    public int process(SpotifyApiResponse response, StagingIteration iteration) throws IOException {
        SpotifyAlbumDto dto = objectMapper.readValue(response.getResponseBody(), SpotifyAlbumDto.class);

        long albumEntityId = idResolutionService.resolveId(SpotifyEntityType.ALBUM, dto.id());

        SpotifySimplifiedArtistDto primaryArtist = dto.getPrimaryArtist();
        String primaryArtistSpotifyId = primaryArtist != null ? primaryArtist.id() : null;
        Long primaryArtistId = primaryArtistSpotifyId != null
            ? idResolutionService.resolveId(SpotifyEntityType.ARTIST, primaryArtistSpotifyId)
            : null;

        stagingWriter.insertAlbum(iteration.getId(), response.getId(), dto,
            albumEntityId, primaryArtistId, primaryArtistSpotifyId);
        int count = 1;

        // Stage all album artists + ARTIST_ALBUM relations
        if (dto.artists() != null) {
            for (SpotifySimplifiedArtistDto artist : dto.artists()) {
                long artistEntityId = idResolutionService.resolveId(SpotifyEntityType.ARTIST, artist.id());
                stagingWriter.insertSimplifiedArtist(iteration.getId(), response.getId(), artist, artistEntityId);
                stagingWriter.insertEntityRelation(iteration.getId(), response.getId(),
                    SpotifyEntityType.ARTIST.getCode(), artistEntityId,
                    SpotifyEntityType.ALBUM.getCode(), albumEntityId,
                    SpotifyRelationType.ARTIST_ALBUM.getCode());
                count += 2;
            }
        }

        // Stage embedded tracks + ALBUM_TRACK relations (may be a partial page; ALBUM_TRACKS call gets the full set)
        if (dto.tracks() != null && dto.tracks().items() != null) {
            List<SpotifyTrackDto> tracks = dto.tracks().items();
            for (SpotifyTrackDto track : tracks) {
                long trackEntityId = idResolutionService.resolveId(SpotifyEntityType.TRACK, track.id());

                SpotifySimplifiedArtistDto trackPrimaryArtist = track.getPrimaryArtist();
                String trackPrimaryArtistSpotifyId = trackPrimaryArtist != null ? trackPrimaryArtist.id() : null;
                Long trackPrimaryArtistId = trackPrimaryArtistSpotifyId != null
                    ? idResolutionService.resolveId(SpotifyEntityType.ARTIST, trackPrimaryArtistSpotifyId)
                    : null;

                stagingWriter.insertTrack(iteration.getId(), response.getId(), track,
                    trackEntityId, trackPrimaryArtistId, trackPrimaryArtistSpotifyId,
                    albumEntityId, dto.id());

                stagingWriter.insertEntityRelation(iteration.getId(), response.getId(),
                    SpotifyEntityType.ALBUM.getCode(), albumEntityId,
                    SpotifyEntityType.TRACK.getCode(), trackEntityId,
                    SpotifyRelationType.ALBUM_TRACK.getCode());
                count += 2;

                // Featured track artists
                if (track.artists() != null && track.artists().size() > 1) {
                    for (int i = 1; i < track.artists().size(); i++) {
                        SpotifySimplifiedArtistDto featuredArtist = track.artists().get(i);
                        long featuredArtistId = idResolutionService.resolveId(SpotifyEntityType.ARTIST, featuredArtist.id());
                        stagingWriter.insertEntityRelation(iteration.getId(), response.getId(),
                            SpotifyEntityType.TRACK.getCode(), trackEntityId,
                            SpotifyEntityType.ARTIST.getCode(), featuredArtistId,
                            SpotifyRelationType.TRACK_ARTIST.getCode());
                        count++;
                    }
                }
            }
        }

        log.debug("Staged album {} with {} records into iteration {}", dto.id(), count, iteration.getId());
        return count;
    }
}
