package yurykorzun.art.universe.music.data.raw.spotify.task.response.process.processor;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import yurykorzun.art.universe.music.data.raw.spotify.enums.SpotifyEntityType;
import yurykorzun.art.universe.music.data.raw.spotify.enums.SpotifyRelationType;
import yurykorzun.art.universe.music.data.raw.spotify.etl.entity.SpotifyApiCallType;
import yurykorzun.art.universe.music.data.raw.spotify.etl.entity.SpotifyApiResponse;
import yurykorzun.art.universe.music.data.raw.spotify.etl.entity.StagingIteration;
import yurykorzun.art.universe.music.data.raw.spotify.integration.dto.SpotifyAlbumDto;
import yurykorzun.art.universe.music.data.raw.spotify.integration.dto.SpotifyPagingDto;
import yurykorzun.art.universe.music.data.raw.spotify.integration.dto.SpotifySimplifiedArtistDto;
import yurykorzun.art.universe.music.data.raw.spotify.staging.StagingWriter;
import yurykorzun.art.universe.music.data.raw.spotify.staging.SyntheticIdResolutionService;
import yurykorzun.art.universe.music.data.raw.spotify.task.response.process.BaseSpotifyApiResponseProcessor;

import java.io.IOException;
import java.util.List;

@Component
@Slf4j
public class SpotifyArtistAlbumsResponseProcessor extends BaseSpotifyApiResponseProcessor {

    private final ObjectMapper objectMapper;
    private final StagingWriter stagingWriter;
    private final SyntheticIdResolutionService idResolutionService;

    public SpotifyArtistAlbumsResponseProcessor(
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
        return SpotifyApiCallType.ARTIST_ALBUMS;
    }

    @Override
    public int process(SpotifyApiResponse response, StagingIteration iteration) throws IOException {
        SpotifyPagingDto<SpotifyAlbumDto> paging = objectMapper.readValue(
            response.getResponseBody(), new TypeReference<>() {});

        List<SpotifyAlbumDto> albums = paging.items();
        if (albums == null || albums.isEmpty()) {
            return 0;
        }

        String artistSpotifyId = response.getApiCall().getSpotifyId();
        long artistEntityId = idResolutionService.resolveId(SpotifyEntityType.ARTIST, artistSpotifyId);

        int count = 0;
        for (SpotifyAlbumDto album : albums) {
            long albumEntityId = idResolutionService.resolveId(SpotifyEntityType.ALBUM, album.id());
            String primaryArtistSpotifyId = album.getPrimaryArtist() != null ? album.getPrimaryArtist().id() : artistSpotifyId;
            Long primaryArtistId = idResolutionService.resolveId(SpotifyEntityType.ARTIST, primaryArtistSpotifyId);

            stagingWriter.insertAlbum(iteration.getId(), response.getId(), album,
                albumEntityId, primaryArtistId, primaryArtistSpotifyId);

            // ARTIST_ALBUM relation for the queried artist
            stagingWriter.insertEntityRelation(iteration.getId(), response.getId(),
                SpotifyEntityType.ARTIST.getCode(), artistEntityId,
                SpotifyEntityType.ALBUM.getCode(), albumEntityId,
                SpotifyRelationType.ARTIST_ALBUM.getCode());
            count += 2;

            // Stage all album artists + their ARTIST_ALBUM relations
            if (album.artists() != null) {
                for (SpotifySimplifiedArtistDto albumArtist : album.artists()) {
                    long albumArtistEntityId = idResolutionService.resolveId(SpotifyEntityType.ARTIST, albumArtist.id());
                    stagingWriter.insertSimplifiedArtist(iteration.getId(), response.getId(), albumArtist, albumArtistEntityId);
                    stagingWriter.insertEntityRelation(iteration.getId(), response.getId(),
                        SpotifyEntityType.ARTIST.getCode(), albumArtistEntityId,
                        SpotifyEntityType.ALBUM.getCode(), albumEntityId,
                        SpotifyRelationType.ARTIST_ALBUM.getCode());
                    count += 2;
                }
            }
        }

        log.debug("Staged {} albums for artist {} into iteration {}", albums.size(), artistSpotifyId, iteration.getId());
        return count;
    }
}
