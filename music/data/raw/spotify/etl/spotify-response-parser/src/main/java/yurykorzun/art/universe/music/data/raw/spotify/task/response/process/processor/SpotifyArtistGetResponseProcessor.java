package yurykorzun.art.universe.music.data.raw.spotify.task.response.process.processor;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import yurykorzun.art.universe.music.data.raw.spotify.enums.SpotifyEntityType;
import yurykorzun.art.universe.music.data.raw.spotify.enums.SpotifyRelationType;
import yurykorzun.art.universe.music.data.raw.spotify.etl.entity.SpotifyApiCallType;
import yurykorzun.art.universe.music.data.raw.spotify.etl.entity.SpotifyApiResponse;
import yurykorzun.art.universe.music.data.raw.spotify.etl.entity.StagingIteration;
import yurykorzun.art.universe.music.data.raw.spotify.integration.dto.SpotifyArtistDto;
import yurykorzun.art.universe.music.data.raw.spotify.staging.StagingWriter;
import yurykorzun.art.universe.music.data.raw.spotify.staging.SyntheticIdResolutionService;
import yurykorzun.art.universe.music.data.raw.spotify.task.response.process.BaseSpotifyApiResponseProcessor;

import java.io.IOException;
import java.util.List;

@Component
@Slf4j
public class SpotifyArtistGetResponseProcessor extends BaseSpotifyApiResponseProcessor {

    private final ObjectMapper objectMapper;
    private final StagingWriter stagingWriter;
    private final SyntheticIdResolutionService idResolutionService;

    public SpotifyArtistGetResponseProcessor(
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
        return SpotifyApiCallType.ARTIST_GET;
    }

    @Override
    public int process(SpotifyApiResponse response, StagingIteration iteration) throws IOException {
        SpotifyArtistDto dto = objectMapper.readValue(response.getResponseBody(), SpotifyArtistDto.class);

        long entityId = idResolutionService.resolveId(SpotifyEntityType.ARTIST, dto.id());
        stagingWriter.insertArtist(iteration.getId(), response.getId(), dto, entityId);
        int count = 1;

        List<String> genres = dto.genres();
        if (genres != null) {
            for (String genreName : genres) {
                long genreEntityId = idResolutionService.resolveId(SpotifyEntityType.GENRE, genreName);
                stagingWriter.insertGenre(iteration.getId(), response.getId(), genreName, genreEntityId);
                stagingWriter.insertEntityRelation(iteration.getId(), response.getId(),
                    SpotifyEntityType.ARTIST.getCode(), entityId,
                    SpotifyEntityType.GENRE.getCode(), genreEntityId,
                    SpotifyRelationType.ARTIST_GENRE.getCode());
                count += 2;
            }
        }

        log.debug("Staged artist {} with {} records into iteration {}", dto.id(), count, iteration.getId());
        return count;
    }
}
