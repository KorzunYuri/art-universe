package yurykorzun.art.universe.music.data.raw.spotify.application;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import yurykorzun.art.universe.music.data.raw.spotify.enums.SpotifyEntityType;
import yurykorzun.art.universe.music.data.raw.spotify.etl.entity.SearchAttemptStatus;
import yurykorzun.art.universe.music.data.raw.spotify.etl.entity.SpotifySearchAttempt;
import yurykorzun.art.universe.music.data.raw.spotify.etl.repository.SpotifySearchAttemptRepository;
import yurykorzun.art.universe.music.data.raw.spotify.integration.MasterDataBindingClient;

import java.time.Instant;
import java.util.List;

@Service
@Slf4j
public class SearchReconciliationService {

    private final SpotifySearchAttemptRepository searchAttemptRepository;
    private final JdbcTemplate jdbc;
    private final MasterDataBindingClient bindingClient;

    public SearchReconciliationService(
        SpotifySearchAttemptRepository searchAttemptRepository,
        JdbcTemplate jdbc,
        MasterDataBindingClient bindingClient
    ) {
        this.searchAttemptRepository = searchAttemptRepository;
        this.jdbc = jdbc;
        this.bindingClient = bindingClient;
    }

    /**
     * Processes all MATCHED search_attempts: looks up the raw Spotify entity,
     * creates a binding in music-data-master via the internal API, then marks BOUND.
     */
    public void reconcileMatchedAttempts() {
        List<SpotifySearchAttempt> matched = searchAttemptRepository.findAllByStatus(SearchAttemptStatus.MATCHED);
        if (matched.isEmpty()) {
            return;
        }

        log.info("Found {} matched search attempts to reconcile", matched.size());
        for (SpotifySearchAttempt attempt : matched) {
            reconcileAttempt(attempt);
        }
    }

    @Transactional
    protected void reconcileAttempt(SpotifySearchAttempt attempt) {
        String tableName = resolveTableName(attempt.getEntityType());
        if (tableName == null) {
            log.warn("Unsupported entity type for reconciliation: {}", attempt.getEntityType());
            return;
        }

        Long rawEntityId = jdbc.query(
            "SELECT id FROM " + tableName + " WHERE spotify_id = ?",
            rs -> rs.next() ? rs.getLong("id") : null,
            attempt.getMatchedSpotifyId()
        );

        if (rawEntityId == null) {
            log.debug("Raw entity not yet applied for matched spotifyId={}, will retry next cycle",
                attempt.getMatchedSpotifyId());
            return;
        }

        try {
            createBinding(attempt.getEntityType(), rawEntityId, attempt.getMasterEntityId());
            attempt.setStatus(SearchAttemptStatus.BOUND);
            attempt.setUpdatedAt(Instant.now());
            searchAttemptRepository.save(attempt);
            log.info("Reconciled search attempt {}: bound {} rawEntityId={} to masterId={}",
                attempt.getId(), attempt.getEntityType(), rawEntityId, attempt.getMasterEntityId());
        } catch (Exception e) {
            log.error("Failed to reconcile search attempt {}: {}", attempt.getId(), e.getMessage());
            // Leave as MATCHED — will retry next cycle
        }
    }

    private void createBinding(SpotifyEntityType entityType, long rawEntityId, long masterId) {
        switch (entityType) {
            case ARTIST -> bindingClient.bindArtistToExisting(rawEntityId, masterId);
            case ALBUM -> bindingClient.bindAlbumToExisting(rawEntityId, masterId);
            case TRACK -> bindingClient.bindTrackToExisting(rawEntityId, masterId);
            default -> throw new IllegalArgumentException("Unsupported entity type for binding: " + entityType);
        }
    }

    private String resolveTableName(SpotifyEntityType entityType) {
        return switch (entityType) {
            case ARTIST -> "artist";
            case ALBUM -> "album";
            case TRACK -> "track";
            default -> null;
        };
    }
}
