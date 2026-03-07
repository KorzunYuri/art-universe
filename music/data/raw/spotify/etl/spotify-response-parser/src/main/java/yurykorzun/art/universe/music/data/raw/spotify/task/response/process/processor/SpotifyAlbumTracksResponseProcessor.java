package yurykorzun.art.universe.music.data.raw.spotify.task.response.process.processor;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import yurykorzun.art.universe.data.raw.common.etl.entity.ApiCallStatus;
import yurykorzun.art.universe.music.data.raw.spotify.enums.SpotifyEntityType;
import yurykorzun.art.universe.music.data.raw.spotify.enums.SpotifyRelationType;
import yurykorzun.art.universe.music.data.raw.spotify.etl.entity.SpotifyApiCall;
import yurykorzun.art.universe.music.data.raw.spotify.etl.entity.SpotifyApiCallType;
import yurykorzun.art.universe.music.data.raw.spotify.etl.entity.SpotifyApiResponse;
import yurykorzun.art.universe.music.data.raw.spotify.etl.entity.StagingIteration;
import yurykorzun.art.universe.music.data.raw.spotify.etl.repository.SpotifyApiCallRepository;
import yurykorzun.art.universe.music.data.raw.spotify.integration.dto.SpotifyPagingDto;
import yurykorzun.art.universe.music.data.raw.spotify.integration.dto.SpotifySimplifiedArtistDto;
import yurykorzun.art.universe.music.data.raw.spotify.integration.dto.SpotifyTrackDto;
import yurykorzun.art.universe.music.data.raw.spotify.staging.StagingWriter;
import yurykorzun.art.universe.music.data.raw.spotify.staging.SyntheticIdResolutionService;
import yurykorzun.art.universe.music.data.raw.spotify.task.response.process.BaseSpotifyApiResponseProcessor;

import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class SpotifyAlbumTracksResponseProcessor extends BaseSpotifyApiResponseProcessor {

    private final ObjectMapper objectMapper;
    private final StagingWriter stagingWriter;
    private final SyntheticIdResolutionService idResolutionService;
    private final SpotifyApiCallRepository apiCallRepository;

    @Value("${spotify.tasks.calls-generate.due-duration-days.artist-get}")
    private int artistGetDueDays;

    public SpotifyAlbumTracksResponseProcessor(
        ObjectMapper objectMapper,
        StagingWriter stagingWriter,
        SyntheticIdResolutionService idResolutionService,
        SpotifyApiCallRepository apiCallRepository
    ) {
        this.objectMapper = objectMapper;
        this.stagingWriter = stagingWriter;
        this.idResolutionService = idResolutionService;
        this.apiCallRepository = apiCallRepository;
    }

    @Override
    public SpotifyApiCallType getApiCallType() {
        return SpotifyApiCallType.ALBUM_TRACKS;
    }

    @Override
    public int process(SpotifyApiResponse response, StagingIteration iteration) throws IOException {
        SpotifyPagingDto<SpotifyTrackDto> paging = objectMapper.readValue(
            response.getResponseBody(), new TypeReference<>() {});

        List<SpotifyTrackDto> tracks = paging.items();
        if (tracks == null || tracks.isEmpty()) {
            return 0;
        }

        String albumSpotifyId = response.getApiCall().getSpotifyId();
        long albumEntityId = idResolutionService.resolveId(SpotifyEntityType.ALBUM, albumSpotifyId);

        int count = 0;
        for (SpotifyTrackDto track : tracks) {
            long trackEntityId = idResolutionService.resolveId(SpotifyEntityType.TRACK, track.id());

            SpotifySimplifiedArtistDto primaryArtist = track.getPrimaryArtist();
            String primaryArtistSpotifyId = primaryArtist != null ? primaryArtist.id() : null;
            Long primaryArtistId = primaryArtistSpotifyId != null
                ? idResolutionService.resolveId(SpotifyEntityType.ARTIST, primaryArtistSpotifyId)
                : null;

            stagingWriter.insertTrack(iteration.getId(), response.getId(), track,
                trackEntityId, primaryArtistId, primaryArtistSpotifyId, albumEntityId, albumSpotifyId);

            stagingWriter.insertEntityRelation(iteration.getId(), response.getId(),
                SpotifyEntityType.ALBUM.getCode(), albumEntityId,
                SpotifyEntityType.TRACK.getCode(), trackEntityId,
                SpotifyRelationType.ALBUM_TRACK.getCode());

            count += 2;

            // Create ARTIST_GET calls for featured artists not already in DB
            if (track.artists() != null && track.artists().size() > 1) {
                for (int i = 1; i < track.artists().size(); i++) {
                    SpotifySimplifiedArtistDto featuredArtist = track.artists().get(i);
                    stagingWriter.insertEntityRelation(iteration.getId(), response.getId(),
                        SpotifyEntityType.TRACK.getCode(), trackEntityId,
                        SpotifyEntityType.ARTIST.getCode(),
                        idResolutionService.resolveId(SpotifyEntityType.ARTIST, featuredArtist.id()),
                        SpotifyRelationType.TRACK_ARTIST.getCode());
                    count++;
                    createArtistGetCallIfMissing(featuredArtist);
                }
            }
        }

        log.debug("Staged {} tracks for album {} into iteration {}", tracks.size(), albumSpotifyId, iteration.getId());
        return count;
    }

    private void createArtistGetCallIfMissing(SpotifySimplifiedArtistDto artist) {
        // Only create if artist is not in DB (resolveId returns negative synthetic if missing)
        long resolvedId = idResolutionService.resolveId(SpotifyEntityType.ARTIST, artist.id());
        if (resolvedId < 0) {
            SpotifyApiCall call = SpotifyApiCall.builder()
                .type(SpotifyApiCallType.ARTIST_GET)
                .spotifyId(artist.id())
                .entityType(SpotifyEntityType.ARTIST)
                .dueDttm(Instant.now().plus(artistGetDueDays, ChronoUnit.DAYS))
                .params(Map.of("spotify_id", artist.id()))
                .build();
            apiCallRepository.save(call);
            log.debug("Created ARTIST_GET call for featured artist {}", artist.id());
        }
    }
}
