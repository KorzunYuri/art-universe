package yurykorzun.art.universe.music.data.raw.spotify.staging;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import yurykorzun.art.universe.music.data.raw.spotify.common.SyntheticIdGenerator;
import yurykorzun.art.universe.music.data.raw.spotify.enums.SpotifyEntityType;

@Service
@Slf4j
public class SyntheticIdResolutionService {

    private static final String SQL_FIND_ARTIST = "SELECT id FROM mu_raw_spotify.artist WHERE spotify_id = ?";
    private static final String SQL_FIND_ALBUM  = "SELECT id FROM mu_raw_spotify.album WHERE spotify_id = ?";
    private static final String SQL_FIND_TRACK  = "SELECT id FROM mu_raw_spotify.track WHERE spotify_id = ?";
    private static final String SQL_FIND_GENRE  = "SELECT id FROM mu_raw_spotify.genre WHERE spotify_id = ?";

    private final JdbcTemplate jdbc;

    public SyntheticIdResolutionService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /**
     * Resolves the DB id for an entity by spotifyId.
     * Returns the real id if found, or a negative synthetic id if the entity is not in DB yet.
     */
    public long resolveId(SpotifyEntityType entityType, String spotifyId) {
        String sql = switch (entityType) {
            case ARTIST -> SQL_FIND_ARTIST;
            case ALBUM  -> SQL_FIND_ALBUM;
            case TRACK  -> SQL_FIND_TRACK;
            case GENRE  -> SQL_FIND_GENRE;
            default -> throw new IllegalArgumentException("Unsupported entity type for ID resolution: " + entityType);
        };

        Long realId = jdbc.query(sql, rs -> rs.next() ? rs.getLong(1) : null, spotifyId);
        if (realId != null && realId > 0) {
            return realId;
        }

        long synthetic = SyntheticIdGenerator.generate(entityType, spotifyId);
        log.debug("Entity {}:{} not in DB, using synthetic id {}", entityType, spotifyId, synthetic);
        return synthetic;
    }
}
