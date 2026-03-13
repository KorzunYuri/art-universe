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
public class SpotifySearchAlbumCallGenerator extends BaseSpotifyApiCallGenerator {

    private static final String FIND_UNBOUND_ALBUMS_SQL = """
            SELECT a.id, a.name, ar.name AS artist_name
            FROM mu_view.v_album a
            LEFT JOIN mu_view.v_artist ar ON ar.id = a.primary_artist_id
            WHERE NOT EXISTS (
                SELECT 1 FROM mu_view.v_album_binding b
                WHERE   b.master_id = a.id
                    AND b.data_source_id = 2
            )
            AND NOT EXISTS (
                SELECT 1 FROM search_attempt sa
                WHERE sa.entity_type = 2
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

    public SpotifySearchAlbumCallGenerator(
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
        return SpotifyApiCallType.SEARCH_ALBUM;
    }

    @Override
    @Transactional
    public void createApiCalls() {
        int batchSize = configPropertyHolder.getInt(SpotifyGeneratorProperty.SEARCH_BATCH_SIZE);
        int dueDurationDays = configPropertyHolder.getInt(SpotifyGeneratorProperty.DUE_DURATION_SEARCH);

        List<UnboundAlbum> unboundAlbums = jdbcTemplate.query(
            FIND_UNBOUND_ALBUMS_SQL,
            (rs, rowNum) -> new UnboundAlbum(rs.getLong("id"), rs.getString("name"), rs.getString("artist_name")),
            batchSize
        );

        if (unboundAlbums.isEmpty()) {
            log.debug("No unbound master albums require search");
            return;
        }

        List<SpotifyApiCallCreateRequest> requests = new ArrayList<>();
        List<SpotifySearchAttempt> attempts = new ArrayList<>();

        for (UnboundAlbum album : unboundAlbums) {
            String searchString = buildSearchString(album);
            SpotifySearchAttempt attempt = SpotifySearchAttempt.builder()
                .entityType(SpotifyEntityType.ALBUM)
                .masterEntityId(album.id())
                .searchString(searchString)
                .status(SearchAttemptStatus.PENDING)
                .build();
            attempts.add(attempt);

            requests.add(SpotifyApiCallCreateRequest.builder()
                .type(getApiCallType())
                .entityType(SpotifyEntityType.ALBUM)
                .dueDttm(Instant.now().plus(dueDurationDays, ChronoUnit.DAYS))
                .params(Map.of("q", searchString, "type", "album", "limit", String.valueOf(SpotifyConstants.SEARCH_LIMIT_MAX)))
                .build());
        }

        List<SpotifySearchAttempt> savedAttempts = searchAttemptRepository.saveAll(attempts);
        List<SpotifyApiCall> savedCalls = apiCallService.createApiCalls(requests);

        for (int i = 0; i < savedAttempts.size(); i++) {
            savedAttempts.get(i).setApiCallId(savedCalls.get(i).getId());
        }
        searchAttemptRepository.saveAll(savedAttempts);

        log.info("Created {} SEARCH_ALBUM api_calls for unbound master albums", savedCalls.size());
    }

    private String buildSearchString(UnboundAlbum album) {
        if (album.artistName() != null && !album.artistName().isBlank()) {
            return album.name() + " " + album.artistName();
        }
        return album.name();
    }

    private record UnboundAlbum(long id, String name, String artistName) {}
}
