package yurykorzun.art.universe.music.data.raw.spotify.generator;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import yurykorzun.art.universe.common.config.client.ConfigPropertyHolder;
import yurykorzun.art.universe.music.data.raw.spotify.common.SpotifyConstants;
import yurykorzun.art.universe.music.data.raw.spotify.config.SpotifyGeneratorProperty;
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
public class SpotifySearchArtistCallGenerator extends BaseSpotifyApiCallGenerator {

    private static final String FIND_UNBOUND_ARTISTS_SQL = """
            SELECT a.id, a.name
            FROM mu_view.v_artist a
            WHERE NOT EXISTS (
                SELECT 1 FROM mu_view.v_artist_binding b
                WHERE b.master_id = a.id
                  AND b.data_source_id = 2
            )
            AND NOT EXISTS (
                SELECT 1 FROM search_attempt sa
                WHERE   sa.entity_type = 1
                    AND sa.master_entity_id = a.id
                    AND (   sa.status IN (1, 2, 3, 5)
                        OR (sa.status = 4 AND sa.next_retry_after > now()))
            )
            LIMIT ?
            """;

    private final JdbcTemplate jdbcTemplate;
    private final SpotifyApiCallService apiCallService;
    private final SpotifySearchAttemptRepository searchAttemptRepository;
    private final ConfigPropertyHolder configPropertyHolder;

    public SpotifySearchArtistCallGenerator(
        JdbcTemplate jdbcTemplate,
        SpotifyApiCallService apiCallService,
        SpotifySearchAttemptRepository searchAttemptRepository,
        ConfigPropertyHolder configPropertyHolder
    ) {
        this.jdbcTemplate = jdbcTemplate;
        this.apiCallService = apiCallService;
        this.searchAttemptRepository = searchAttemptRepository;
        this.configPropertyHolder = configPropertyHolder;
    }

    @Override
    public SpotifyApiCallType getApiCallType() {
        return SpotifyApiCallType.SEARCH_ARTIST;
    }

    @Override
    @Transactional
    public void createApiCalls() {
        int batchSize = configPropertyHolder.getInt(SpotifyGeneratorProperty.SEARCH_BATCH_SIZE);
        int dueDurationDays = configPropertyHolder.getInt(SpotifyGeneratorProperty.DUE_DURATION_SEARCH);

        List<UnboundEntity> unboundArtists = jdbcTemplate.query(
            FIND_UNBOUND_ARTISTS_SQL,
            (rs, rowNum) -> new UnboundEntity(rs.getLong("id"), rs.getString("name")),
            batchSize
        );

        if (unboundArtists.isEmpty()) {
            log.debug("No unbound master artists require search");
            return;
        }

        List<SpotifyApiCallCreateRequest> requests = new ArrayList<>();
        List<SpotifySearchAttempt> attempts = new ArrayList<>();

        for (UnboundEntity artist : unboundArtists) {
            SpotifySearchAttempt attempt = SpotifySearchAttempt.builder()
                .entityType(SpotifyEntityType.ARTIST)
                .masterEntityId(artist.id())
                .searchString(artist.name())
                .status(SearchAttemptStatus.PENDING)
                .build();
            attempts.add(attempt);

            requests.add(SpotifyApiCallCreateRequest.builder()
                .type(getApiCallType())
                .entityType(SpotifyEntityType.ARTIST)
                .dueDttm(Instant.now().plus(dueDurationDays, ChronoUnit.DAYS))
                .params(Map.of("q", artist.name(), "type", "artist", "limit", String.valueOf(SpotifyConstants.SEARCH_LIMIT_MAX)))
                .build());
        }

        List<SpotifySearchAttempt> savedAttempts = searchAttemptRepository.saveAll(attempts);
        List<SpotifyApiCall> savedCalls = apiCallService.createApiCalls(requests);

        for (int i = 0; i < savedAttempts.size(); i++) {
            savedAttempts.get(i).setApiCallId(savedCalls.get(i).getId());
        }
        searchAttemptRepository.saveAll(savedAttempts);

        log.info("Created {} SEARCH_ARTIST api_calls for unbound master artists", savedCalls.size());
    }

    private record UnboundEntity(long id, String name) {}
}
