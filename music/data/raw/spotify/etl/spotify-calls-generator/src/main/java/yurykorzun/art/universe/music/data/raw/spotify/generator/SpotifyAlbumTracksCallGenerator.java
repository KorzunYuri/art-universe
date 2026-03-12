package yurykorzun.art.universe.music.data.raw.spotify.generator;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import yurykorzun.art.universe.music.data.raw.spotify.domain.entity.SpotifyAlbum;
import yurykorzun.art.universe.music.data.raw.spotify.enums.SpotifyEntityType;
import yurykorzun.art.universe.music.data.raw.spotify.etl.dto.SpotifyApiCallCreateRequest;
import yurykorzun.art.universe.music.data.raw.spotify.etl.entity.SpotifyApiCallType;
import yurykorzun.art.universe.music.data.raw.spotify.etl.service.SpotifyApiCallService;
import yurykorzun.art.universe.music.data.raw.spotify.task.call.generate.BaseSpotifyApiCallGenerator;
import yurykorzun.art.universe.music.data.raw.spotify.task.call.generate.SpotifyApiCallEntityService;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
@Slf4j
public class SpotifyAlbumTracksCallGenerator extends BaseSpotifyApiCallGenerator {

    private final SpotifyApiCallEntityService entityService;
    private final SpotifyApiCallService apiCallService;

    @Value("${spotify.tasks.calls-generate.due-duration-days.album-tracks}")
    private int dueDurationDays;

    public SpotifyAlbumTracksCallGenerator(
        SpotifyApiCallEntityService entityService,
        SpotifyApiCallService apiCallService
    ) {
        this.entityService = entityService;
        this.apiCallService = apiCallService;
    }

    @Override
    public SpotifyApiCallType getApiCallType() {
        return SpotifyApiCallType.ALBUM_TRACKS;
    }

    @Override
    @Transactional
    public void createApiCalls() {
        List<SpotifyAlbum> albums = entityService.findAllWithoutActiveCalls(SpotifyAlbum.class, getApiCallType());
        if (albums.isEmpty()) {
            log.debug("No albums require {} call", getApiCallType().getMethod());
            return;
        }

        List<SpotifyApiCallCreateRequest> requests = albums.stream()
            .map(album -> SpotifyApiCallCreateRequest.builder()
                .type(getApiCallType())
                .spotifyId(album.getSpotifyId())
                .entityType(SpotifyEntityType.ALBUM)
                .entityId(album.getId())
                .dueDttm(Instant.now().plus(dueDurationDays, ChronoUnit.DAYS))
                .build())
            .toList();

        apiCallService.createApiCalls(requests);
        log.info("Created {} {} api_call records", requests.size(), getApiCallType().getMethod());
    }
}
