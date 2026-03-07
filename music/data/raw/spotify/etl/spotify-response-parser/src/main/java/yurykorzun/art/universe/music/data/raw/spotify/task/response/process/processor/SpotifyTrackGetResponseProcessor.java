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

@Component
@Slf4j
public class SpotifyTrackGetResponseProcessor extends BaseSpotifyApiResponseProcessor {

    private final ObjectMapper objectMapper;
    private final StagingWriter stagingWriter;
    private final SyntheticIdResolutionService idResolutionService;

    public SpotifyTrackGetResponseProcessor(
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
        return SpotifyApiCallType.TRACK_GET;
    }

    @Override
    public int process(SpotifyApiResponse response, StagingIteration iteration) throws IOException {
        SpotifyTrackDto dto = objectMapper.readValue(response.getResponseBody(), SpotifyTrackDto.class);

        long trackEntityId = idResolutionService.resolveId(SpotifyEntityType.TRACK, dto.id());

        SpotifySimplifiedArtistDto primaryArtist = dto.getPrimaryArtist();
        String primaryArtistSpotifyId = primaryArtist != null ? primaryArtist.id() : null;

        // Resolve album context from embedded album object
        SpotifyAlbumDto album = dto.album();
        String albumSpotifyId = album != null ? album.id() : null;
        Long albumEntityId = albumSpotifyId != null
            ? idResolutionService.resolveId(SpotifyEntityType.ALBUM, albumSpotifyId)
            : null;

        Long primaryArtistId = primaryArtistSpotifyId != null
            ? idResolutionService.resolveId(SpotifyEntityType.ARTIST, primaryArtistSpotifyId)
            : null;

        stagingWriter.insertTrack(iteration.getId(), response.getId(), dto,
            trackEntityId, primaryArtistId, primaryArtistSpotifyId, albumEntityId, albumSpotifyId);
        int count = 1;

        // Stage embedded album + ALBUM_TRACK relation
        if (album != null) {
            SpotifySimplifiedArtistDto albumPrimaryArtist = album.getPrimaryArtist();
            String albumPrimaryArtistSpotifyId = albumPrimaryArtist != null ? albumPrimaryArtist.id() : null;
            Long albumPrimaryArtistId = albumPrimaryArtistSpotifyId != null
                ? idResolutionService.resolveId(SpotifyEntityType.ARTIST, albumPrimaryArtistSpotifyId)
                : null;

            stagingWriter.insertAlbum(iteration.getId(), response.getId(), album,
                albumEntityId, albumPrimaryArtistId, albumPrimaryArtistSpotifyId);

            stagingWriter.insertEntityRelation(iteration.getId(), response.getId(),
                SpotifyEntityType.ALBUM.getCode(), albumEntityId,
                SpotifyEntityType.TRACK.getCode(), trackEntityId,
                SpotifyRelationType.ALBUM_TRACK.getCode());
            count += 2;
        }

        // Stage TRACK_ARTIST relations for featured artists (index > 0)
        if (dto.artists() != null && dto.artists().size() > 1) {
            for (int i = 1; i < dto.artists().size(); i++) {
                SpotifySimplifiedArtistDto featuredArtist = dto.artists().get(i);
                long featuredArtistId = idResolutionService.resolveId(SpotifyEntityType.ARTIST, featuredArtist.id());
                stagingWriter.insertEntityRelation(iteration.getId(), response.getId(),
                    SpotifyEntityType.TRACK.getCode(), trackEntityId,
                    SpotifyEntityType.ARTIST.getCode(), featuredArtistId,
                    SpotifyRelationType.TRACK_ARTIST.getCode());
                count++;
            }
        }

        log.debug("Staged track {} with {} records into iteration {}", dto.id(), count, iteration.getId());
        return count;
    }
}
