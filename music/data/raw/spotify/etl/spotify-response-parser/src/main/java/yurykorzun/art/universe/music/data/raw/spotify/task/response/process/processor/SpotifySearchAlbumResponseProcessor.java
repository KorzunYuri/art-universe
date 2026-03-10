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
import yurykorzun.art.universe.music.data.raw.spotify.integration.dto.SpotifySearchResultDto;
import yurykorzun.art.universe.music.data.raw.spotify.integration.dto.SpotifySimplifiedArtistDto;
import yurykorzun.art.universe.music.data.raw.spotify.staging.SearchMatchScoringService;
import yurykorzun.art.universe.music.data.raw.spotify.staging.StagingWriter;
import yurykorzun.art.universe.music.data.raw.spotify.staging.SyntheticIdResolutionService;
import yurykorzun.art.universe.music.data.raw.spotify.task.response.process.BaseSpotifyApiResponseProcessor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class SpotifySearchAlbumResponseProcessor extends BaseSpotifyApiResponseProcessor {

    private final ObjectMapper objectMapper;
    private final StagingWriter stagingWriter;
    private final SyntheticIdResolutionService idResolutionService;
    private final SearchMatchScoringService searchMatchScoringService;

    public SpotifySearchAlbumResponseProcessor(
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
        return SpotifyApiCallType.SEARCH_ALBUM;
    }

    @Override
    public int process(SpotifyApiResponse response, StagingIteration iteration) throws IOException {
        SpotifySearchResultDto searchResult = objectMapper.readValue(response.getResponseBody(), SpotifySearchResultDto.class);

        if (searchResult.albums() == null || searchResult.albums().items() == null) {
            searchMatchScoringService.scoreAndUpdate(
                response.getApiCall().getId(), List.of(), List.of());
            return 0;
        }

        List<SpotifyAlbumDto> albums = searchResult.albums().items();
        List<String> candidateNames = new ArrayList<>();
        List<String> candidateSpotifyIds = new ArrayList<>();

        int count = 0;
        for (SpotifyAlbumDto album : albums) {
            long albumEntityId = idResolutionService.resolveId(SpotifyEntityType.ALBUM, album.id());
            SpotifySimplifiedArtistDto primaryArtist = album.getPrimaryArtist();
            String primaryArtistSpotifyId = primaryArtist != null ? primaryArtist.id() : null;
            Long primaryArtistId = primaryArtistSpotifyId != null
                ? idResolutionService.resolveId(SpotifyEntityType.ARTIST, primaryArtistSpotifyId)
                : null;

            stagingWriter.insertAlbum(iteration.getId(), response.getId(), album,
                albumEntityId, primaryArtistId, primaryArtistSpotifyId);
            count++;

            // Stage album artists + ARTIST_ALBUM relations
            if (album.artists() != null) {
                for (SpotifySimplifiedArtistDto artist : album.artists()) {
                    long artistEntityId = idResolutionService.resolveId(SpotifyEntityType.ARTIST, artist.id());
                    stagingWriter.insertSimplifiedArtist(iteration.getId(), response.getId(), artist, artistEntityId);
                    stagingWriter.insertEntityRelation(iteration.getId(), response.getId(),
                        SpotifyEntityType.ARTIST.getCode(), artistEntityId,
                        SpotifyEntityType.ALBUM.getCode(), albumEntityId,
                        SpotifyRelationType.ARTIST_ALBUM.getCode());
                    count += 2;
                }
            }

            candidateNames.add(album.name());
            candidateSpotifyIds.add(album.id());
        }

        searchMatchScoringService.scoreAndUpdate(
            response.getApiCall().getId(), candidateNames, candidateSpotifyIds);

        log.debug("Staged {} albums from search response into iteration {}", albums.size(), iteration.getId());
        return count;
    }
}
