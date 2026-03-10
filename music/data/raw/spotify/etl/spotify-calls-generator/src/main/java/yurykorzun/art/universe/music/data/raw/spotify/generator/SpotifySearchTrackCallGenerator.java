package yurykorzun.art.universe.music.data.raw.spotify.generator;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import yurykorzun.art.universe.music.data.raw.spotify.enums.SpotifyEntityType;
import yurykorzun.art.universe.music.data.raw.spotify.etl.dto.SpotifyApiCallCreateRequest;
import yurykorzun.art.universe.music.data.raw.spotify.etl.entity.*;
import yurykorzun.art.universe.music.data.raw.spotify.etl.repository.SpotifySearchAttemptRepository;
import yurykorzun.art.universe.music.data.raw.spotify.etl.service.SpotifyApiCallService;
import yurykorzun.art.universe.music.data.raw.spotify.task.call.generate.BaseSpotifyApiCallGenerator;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class SpotifySearchTrackCallGenerator extends BaseSpotifyApiCallGenerator {

    private static final String FIND_UNBOUND_TRACKS_SQL = """
            SELECT t.id, t.name, ar.name AS artist_name
            FROM mu_view.v_track t
            LEFT JOIN mu_view.v_artist ar ON ar.id = t.primary_artist_id
            WHERE NOT EXISTS (
                SELECT 1 FROM mu_view.v_track_binding b
                WHERE   b.master_id = t.id
                    AND b.data_source_id = 2
            )
            AND NOT EXISTS (
                SELECT 1 FROM search_attempt sa
                WHERE sa.entity_type = 3
                  AND sa.master_entity_id = t.id
                    AND (   sa.status IN (1, 2, 3, 5)
                        OR (sa.status = 4 AND sa.next_retry_after > now()))
            )
            LIMIT ?
            """;

    private final JdbcTemplate jdbcTemplate;
    private final SpotifyApiCallService apiCallService;
    private final SpotifySearchAttemptRepository searchAttemptRepository;

    @Value("${spotify.tasks.calls-generate.search.enabled:false}")
    private boolean enabled;

    @Value("${spotify.tasks.calls-generate.search.batch-size:100}")
    private int batchSize;

    @Value("${spotify.tasks.calls-generate.due-duration-days.search-track:7}")
    private int dueDurationDays;

    public SpotifySearchTrackCallGenerator(
        JdbcTemplate jdbcTemplate,
        SpotifyApiCallService apiCallService,
        SpotifySearchAttemptRepository searchAttemptRepository
    ) {
        this.jdbcTemplate = jdbcTemplate;
        this.apiCallService = apiCallService;
        this.searchAttemptRepository = searchAttemptRepository;
    }

    @Override
    public SpotifyApiCallType getApiCallType() {
        return SpotifyApiCallType.SEARCH_TRACK;
    }

    @Override
    @Transactional
    public void createApiCalls() {
        if (!enabled) {
            return;
        }

        List<UnboundTrack> unboundTracks = jdbcTemplate.query(
            FIND_UNBOUND_TRACKS_SQL,
            (rs, rowNum) -> new UnboundTrack(rs.getLong("id"), rs.getString("name"), rs.getString("artist_name")),
            batchSize
        );

        if (unboundTracks.isEmpty()) {
            log.debug("No unbound master tracks require search");
            return;
        }

        List<SpotifyApiCallCreateRequest> requests = new ArrayList<>();
        List<SpotifySearchAttempt> attempts = new ArrayList<>();

        for (UnboundTrack track : unboundTracks) {
            String searchString = buildSearchString(track);
            SpotifySearchAttempt attempt = SpotifySearchAttempt.builder()
                .entityType(SpotifyEntityType.TRACK)
                .masterEntityId(track.id())
                .searchString(searchString)
                .status(SearchAttemptStatus.PENDING)
                .build();
            attempts.add(attempt);

            requests.add(SpotifyApiCallCreateRequest.builder()
                .type(getApiCallType())
                .entityType(SpotifyEntityType.TRACK)
                .dueDttm(Instant.now().plus(dueDurationDays, ChronoUnit.DAYS))
                .params(Map.of("q", searchString, "type", "track", "limit", "20"))
                .build());
        }

        List<SpotifySearchAttempt> savedAttempts = searchAttemptRepository.saveAll(attempts);
        List<SpotifyApiCall> savedCalls = apiCallService.createApiCalls(requests);

        for (int i = 0; i < savedAttempts.size(); i++) {
            savedAttempts.get(i).setApiCallId(savedCalls.get(i).getId());
        }
        searchAttemptRepository.saveAll(savedAttempts);

        log.info("Created {} SEARCH_TRACK api_calls for unbound master tracks", savedCalls.size());
    }

    private String buildSearchString(UnboundTrack track) {
        if (track.artistName() != null && !track.artistName().isBlank()) {
            return track.name() + " " + track.artistName();
        }
        return track.name();
    }

    private record UnboundTrack(long id, String name, String artistName) {}
}
