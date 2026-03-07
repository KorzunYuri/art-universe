package yurykorzun.art.universe.music.data.raw.spotify.generator;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import yurykorzun.art.universe.music.data.raw.spotify.domain.entity.SpotifyArtist;
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
public class SpotifyArtistGetCallGenerator extends BaseSpotifyApiCallGenerator {

    private final SpotifyApiCallEntityService entityService;
    private final SpotifyApiCallService apiCallService;

    @Value("${spotify.tasks.calls-generate.due-duration-days.artist-get}")
    private int dueDurationDays;

    public SpotifyArtistGetCallGenerator(
        SpotifyApiCallEntityService entityService,
        SpotifyApiCallService apiCallService
    ) {
        this.entityService = entityService;
        this.apiCallService = apiCallService;
    }

    @Override
    public SpotifyApiCallType getApiCallType() {
        return SpotifyApiCallType.ARTIST_GET;
    }

    @Override
    @Transactional
    public void createApiCalls() {
        List<SpotifyArtist> artists = entityService.findAllWithoutActiveCalls(SpotifyArtist.class, getApiCallType());
        if (artists.isEmpty()) {
            log.debug("No artists require {} call", getApiCallType().getMethod());
            return;
        }

        List<SpotifyApiCallCreateRequest> requests = artists.stream()
            .map(artist -> SpotifyApiCallCreateRequest.builder()
                .type(getApiCallType())
                .spotifyId(artist.getSpotifyId())
                .entityType(SpotifyEntityType.ARTIST)
                .entityId(artist.getId())
                .dueDttm(Instant.now().plus(dueDurationDays, ChronoUnit.DAYS))
                .build())
            .toList();

        apiCallService.createApiCalls(requests);
        log.info("Created {} {} api_call records", requests.size(), getApiCallType().getMethod());
    }
}
