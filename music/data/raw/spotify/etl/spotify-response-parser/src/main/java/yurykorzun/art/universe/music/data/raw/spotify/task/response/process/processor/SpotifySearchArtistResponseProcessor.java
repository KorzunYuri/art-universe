package yurykorzun.art.universe.music.data.raw.spotify.task.response.process.processor;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import yurykorzun.art.universe.music.data.raw.spotify.enums.SpotifyEntityType;
import yurykorzun.art.universe.music.data.raw.spotify.etl.entity.SpotifyApiCallType;
import yurykorzun.art.universe.music.data.raw.spotify.etl.entity.SpotifyApiResponse;
import yurykorzun.art.universe.music.data.raw.spotify.etl.entity.StagingIteration;
import yurykorzun.art.universe.music.data.raw.spotify.integration.dto.SpotifyArtistDto;
import yurykorzun.art.universe.music.data.raw.spotify.integration.dto.SpotifySearchResultDto;
import yurykorzun.art.universe.music.data.raw.spotify.staging.SearchMatchScoringService;
import yurykorzun.art.universe.music.data.raw.spotify.staging.StagingWriter;
import yurykorzun.art.universe.music.data.raw.spotify.staging.SyntheticIdResolutionService;
import yurykorzun.art.universe.music.data.raw.spotify.task.response.process.BaseSpotifyApiResponseProcessor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class SpotifySearchArtistResponseProcessor extends BaseSpotifyApiResponseProcessor {

    private final ObjectMapper objectMapper;
    private final StagingWriter stagingWriter;
    private final SyntheticIdResolutionService idResolutionService;
    private final SearchMatchScoringService searchMatchScoringService;

    public SpotifySearchArtistResponseProcessor(
        ObjectMapper objectMapper,
        StagingWriter stagingWriter,
        SyntheticIdResolutionService idResolutionService,
        SearchMatchScoringService searchMatchScoringService
    ) {
        this.objectMapper = objectMapper;
        this.stagingWriter = stagingWriter;
        this.idResolutionService = idResolutionService;
        this.searchMatchScoringService = searchMatchScoringService;
    }

    @Override
    public SpotifyApiCallType getApiCallType() {
        return SpotifyApiCallType.SEARCH_ARTIST;
    }

    @Override
    public int process(SpotifyApiResponse response, StagingIteration iteration) throws IOException {
        SpotifySearchResultDto searchResult = objectMapper.readValue(response.getResponseBody(), SpotifySearchResultDto.class);

        if (searchResult.artists() == null || searchResult.artists().items() == null) {
            searchMatchScoringService.scoreAndUpdate(
                response.getApiCall().getId(), List.of(), List.of());
            return 0;
        }

        List<SpotifyArtistDto> artists = searchResult.artists().items();
        List<String> candidateNames = new ArrayList<>();
        List<String> candidateSpotifyIds = new ArrayList<>();

        for (SpotifyArtistDto artist : artists) {
            long entityId = idResolutionService.resolveId(SpotifyEntityType.ARTIST, artist.id());
            stagingWriter.insertArtist(iteration.getId(), response.getId(), artist, entityId);
            candidateNames.add(artist.name());
            candidateSpotifyIds.add(artist.id());
        }

        searchMatchScoringService.scoreAndUpdate(
            response.getApiCall().getId(), candidateNames, candidateSpotifyIds);

        log.debug("Staged {} artists from search response into iteration {}", artists.size(), iteration.getId());
        return artists.size();
    }
}
