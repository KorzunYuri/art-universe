package yurykorzun.art.universe.music.data.raw.spotify.task.response.process.processor;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import yurykorzun.art.universe.music.data.raw.spotify.enums.SpotifyEntityType;
import yurykorzun.art.universe.music.data.raw.spotify.etl.entity.SpotifyApiCallType;
import yurykorzun.art.universe.music.data.raw.spotify.etl.entity.SpotifyApiResponse;
import yurykorzun.art.universe.music.data.raw.spotify.etl.entity.StagingIteration;
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

        long entityId = idResolutionService.resolveId(SpotifyEntityType.TRACK, dto.id());

        SpotifySimplifiedArtistDto primaryArtist = dto.getPrimaryArtist();
        String primaryArtistSpotifyId = primaryArtist != null ? primaryArtist.id() : null;
        Long primaryArtistId = primaryArtistSpotifyId != null
            ? idResolutionService.resolveId(SpotifyEntityType.ARTIST, primaryArtistSpotifyId)
            : null;

        stagingWriter.insertTrack(iteration.getId(), response.getId(), dto,
            entityId, primaryArtistId, primaryArtistSpotifyId, null, null);

        log.debug("Staged track {} into iteration {}", dto.id(), iteration.getId());
        return 1;
    }
}
