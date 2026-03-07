package yurykorzun.art.universe.music.data.raw.spotify.task.response.process.processor;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import yurykorzun.art.universe.music.data.raw.spotify.enums.SpotifyEntityType;
import yurykorzun.art.universe.music.data.raw.spotify.etl.entity.SpotifyApiCallType;
import yurykorzun.art.universe.music.data.raw.spotify.etl.entity.SpotifyApiResponse;
import yurykorzun.art.universe.music.data.raw.spotify.etl.entity.StagingIteration;
import yurykorzun.art.universe.music.data.raw.spotify.integration.dto.SpotifyAlbumDto;
import yurykorzun.art.universe.music.data.raw.spotify.integration.dto.SpotifySimplifiedArtistDto;
import yurykorzun.art.universe.music.data.raw.spotify.staging.StagingWriter;
import yurykorzun.art.universe.music.data.raw.spotify.staging.SyntheticIdResolutionService;
import yurykorzun.art.universe.music.data.raw.spotify.task.response.process.BaseSpotifyApiResponseProcessor;

import java.io.IOException;

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

        long entityId = idResolutionService.resolveId(SpotifyEntityType.ALBUM, dto.id());

        SpotifySimplifiedArtistDto primaryArtist = dto.getPrimaryArtist();
        String primaryArtistSpotifyId = primaryArtist != null ? primaryArtist.id() : null;
        Long primaryArtistId = primaryArtistSpotifyId != null
            ? idResolutionService.resolveId(SpotifyEntityType.ARTIST, primaryArtistSpotifyId)
            : null;

        stagingWriter.insertAlbum(iteration.getId(), response.getId(), dto,
            entityId, primaryArtistId, primaryArtistSpotifyId);

        log.debug("Staged album {} into iteration {}", dto.id(), iteration.getId());
        return 1;
    }
}
