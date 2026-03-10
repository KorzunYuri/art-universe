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
import yurykorzun.art.universe.music.data.raw.spotify.integration.dto.SpotifyTrackDto;
import yurykorzun.art.universe.music.data.raw.spotify.staging.SearchMatchScoringService;
import yurykorzun.art.universe.music.data.raw.spotify.staging.StagingWriter;
import yurykorzun.art.universe.music.data.raw.spotify.staging.SyntheticIdResolutionService;
import yurykorzun.art.universe.music.data.raw.spotify.task.response.process.BaseSpotifyApiResponseProcessor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class SpotifySearchTrackResponseProcessor extends BaseSpotifyApiResponseProcessor {

    private final ObjectMapper objectMapper;
    private final StagingWriter stagingWriter;
    private final SyntheticIdResolutionService idResolutionService;
    private final SearchMatchScoringService searchMatchScoringService;

    public SpotifySearchTrackResponseProcessor(
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
        return SpotifyApiCallType.SEARCH_TRACK;
    }

    @Override
    public int process(SpotifyApiResponse response, StagingIteration iteration) throws IOException {
        SpotifySearchResultDto searchResult = objectMapper.readValue(response.getResponseBody(), SpotifySearchResultDto.class);

        if (searchResult.tracks() == null || searchResult.tracks().items() == null) {
            searchMatchScoringService.scoreAndUpdate(
                response.getApiCall().getId(), List.of(), List.of());
            return 0;
        }

        List<SpotifyTrackDto> tracks = searchResult.tracks().items();
        List<String> candidateNames = new ArrayList<>();
        List<String> candidateSpotifyIds = new ArrayList<>();

        int count = 0;
        for (SpotifyTrackDto track : tracks) {
            long trackEntityId = idResolutionService.resolveId(SpotifyEntityType.TRACK, track.id());

            SpotifySimplifiedArtistDto primaryArtist = track.getPrimaryArtist();
            String primaryArtistSpotifyId = primaryArtist != null ? primaryArtist.id() : null;
            Long primaryArtistId = primaryArtistSpotifyId != null
                ? idResolutionService.resolveId(SpotifyEntityType.ARTIST, primaryArtistSpotifyId)
                : null;

            SpotifyAlbumDto album = track.album();
            String albumSpotifyId = album != null ? album.id() : null;
            Long albumEntityId = albumSpotifyId != null
                ? idResolutionService.resolveId(SpotifyEntityType.ALBUM, albumSpotifyId)
                : null;

            stagingWriter.insertTrack(iteration.getId(), response.getId(), track,
                trackEntityId, primaryArtistId, primaryArtistSpotifyId, albumEntityId, albumSpotifyId);
            count++;

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
            if (track.artists() != null && track.artists().size() > 1) {
                for (int i = 1; i < track.artists().size(); i++) {
                    SpotifySimplifiedArtistDto featuredArtist = track.artists().get(i);
                    long featuredArtistId = idResolutionService.resolveId(SpotifyEntityType.ARTIST, featuredArtist.id());
                    stagingWriter.insertSimplifiedArtist(iteration.getId(), response.getId(), featuredArtist, featuredArtistId);
                    stagingWriter.insertEntityRelation(iteration.getId(), response.getId(),
                        SpotifyEntityType.TRACK.getCode(), trackEntityId,
                        SpotifyEntityType.ARTIST.getCode(), featuredArtistId,
                        SpotifyRelationType.TRACK_ARTIST.getCode());
                    count += 2;
                }
            }

            candidateNames.add(track.name());
            candidateSpotifyIds.add(track.id());
        }

        searchMatchScoringService.scoreAndUpdate(
            response.getApiCall().getId(), candidateNames, candidateSpotifyIds);

        log.debug("Staged {} tracks from search response into iteration {}", tracks.size(), iteration.getId());
        return count;
    }
}
