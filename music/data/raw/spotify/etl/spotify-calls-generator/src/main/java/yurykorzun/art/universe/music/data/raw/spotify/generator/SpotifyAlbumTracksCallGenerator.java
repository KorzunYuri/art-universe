package yurykorzun.art.universe.music.data.raw.spotify.generator;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import yurykorzun.art.universe.common.config.client.ConfigPropertyHolder;
import yurykorzun.art.universe.music.data.raw.spotify.config.SpotifyGeneratorProperty;
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
    private final ConfigPropertyHolder configPropertyHolder;

    public SpotifyAlbumTracksCallGenerator(
        SpotifyApiCallEntityService entityService,
        SpotifyApiCallService apiCallService,
        ConfigPropertyHolder configPropertyHolder
    ) {
        this.entityService = entityService;
        this.apiCallService = apiCallService;
        this.configPropertyHolder = configPropertyHolder;
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

        int dueDurationDays = configPropertyHolder.getInt(SpotifyGeneratorProperty.DUE_DURATION_ALBUM_TRACKS);
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
