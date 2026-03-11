package yurykorzun.art.universe.music.data.raw.spotify.staging;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import yurykorzun.art.universe.music.data.raw.spotify.integration.dto.SpotifyAlbumDto;
import yurykorzun.art.universe.music.data.raw.spotify.integration.dto.SpotifyArtistDto;
import yurykorzun.art.universe.music.data.raw.spotify.integration.dto.SpotifySimplifiedArtistDto;
import yurykorzun.art.universe.music.data.raw.spotify.integration.dto.SpotifyTrackDto;

@Service
@Slf4j
public class StagingWriter {

    private final JdbcTemplate jdbc;

    public StagingWriter(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public void createTablesForIteration(long iterationId) {
        for (String template : new String[]{"stg_artist", "stg_genre", "stg_album", "stg_track", "stg_entity_relation"}) {
            String tableName = "mu_raw_spotify_staging." + template + "_" + iterationId;
            String templateName = "mu_raw_spotify." + template + "_template";
            jdbc.execute(String.format(
                "CREATE TABLE IF NOT EXISTS %s (LIKE %s INCLUDING ALL)", tableName, templateName));
        }
        log.debug("Created staging tables for iteration {}", iterationId);
    }

    public void insertGenre(long iterationId, long responseId, String genreName, long entityId) {
        String table = "mu_raw_spotify_staging.stg_genre_" + iterationId;
        jdbc.update(
            "INSERT INTO " + table + " (api_response_id, entity_id, spotify_id, name) " +
            "VALUES (?, ?, ?, ?) ON CONFLICT (spotify_id) DO NOTHING",
            responseId, entityId, genreName, genreName
        );
    }

    public void insertArtist(long iterationId, long responseId, SpotifyArtistDto dto, long entityId) {
        String table = "mu_raw_spotify_staging.stg_artist_" + iterationId;
        jdbc.update(
            "INSERT INTO " + table + " (api_response_id, entity_id, spotify_id, name, spotify_url, uri) " +
            "VALUES (?, ?, ?, ?, ?, ?) ON CONFLICT (spotify_id) DO NOTHING",
            responseId, entityId, dto.id(), dto.name(), dto.getSpotifyUrl(), dto.uri()
        );
    }

    public void insertSimplifiedArtist(long iterationId, long responseId, SpotifySimplifiedArtistDto dto, long entityId) {
        String table = "mu_raw_spotify_staging.stg_artist_" + iterationId;
        jdbc.update(
            "INSERT INTO " + table + " (api_response_id, entity_id, spotify_id, name, spotify_url, uri) " +
            "VALUES (?, ?, ?, ?, ?, ?) ON CONFLICT (spotify_id) DO NOTHING",
            responseId, entityId, dto.id(), dto.name(), dto.getSpotifyUrl(), dto.uri()
        );
    }

    public void insertAlbum(long iterationId, long responseId, SpotifyAlbumDto dto,
                            long entityId, Long primaryArtistId, String primaryArtistSpotifyId) {
        String table = "mu_raw_spotify_staging.stg_album_" + iterationId;
        Integer albumTypeCode = resolveAlbumTypeCode(dto.albumType());
        Integer datePrecisionCode = resolveDatePrecisionCode(dto.releaseDatePrecision());
        jdbc.update(
            "INSERT INTO " + table + " (api_response_id, entity_id, spotify_id, name, album_type, " +
            "total_tracks, release_date, release_date_precision, spotify_url, uri, " +
            "primary_artist_id, primary_artist_spotify_id) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) ON CONFLICT (spotify_id) DO NOTHING",
            responseId, entityId, dto.id(), dto.name(), albumTypeCode,
            dto.totalTracks(), dto.releaseDate(), datePrecisionCode, dto.getSpotifyUrl(), dto.uri(),
            primaryArtistId, primaryArtistSpotifyId
        );
    }

    public void insertTrack(long iterationId, long responseId, SpotifyTrackDto dto,
                            long entityId, Long primaryArtistId, String primaryArtistSpotifyId,
                            Long albumId, String albumSpotifyId) {
        String table = "mu_raw_spotify_staging.stg_track_" + iterationId;
        jdbc.update(
            "INSERT INTO " + table + " (api_response_id, entity_id, spotify_id, name, duration_ms, " +
            "track_number, disc_number, has_explicit_lyrics, is_playable, spotify_url, uri, " +
            "isrc, ean, upc, primary_artist_id, primary_artist_spotify_id, album_id, album_spotify_id) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) ON CONFLICT (spotify_id) DO NOTHING",
            responseId, entityId, dto.id(), dto.name(), dto.durationMs(),
            dto.trackNumber(), dto.discNumber(), dto.explicit(), dto.isPlayable(),
            dto.getSpotifyUrl(), dto.uri(), dto.getIsrc(), dto.getEan(), dto.getUpc(),
            primaryArtistId, primaryArtistSpotifyId, albumId, albumSpotifyId
        );
    }

    public void insertEntityRelation(long iterationId, long responseId,
                                     int sourceEntityType, long sourceEntityId,
                                     int targetEntityType, long targetEntityId,
                                     int relationType) {
        String table = "mu_raw_spotify_staging.stg_entity_relation_" + iterationId;
        jdbc.update(
            "INSERT INTO " + table + " (api_response_id, source_entity_type, source_entity_id, " +
            "target_entity_type, target_entity_id, relation_type) " +
            "VALUES (?, ?, ?, ?, ?, ?) ON CONFLICT (source_entity_type, source_entity_id, " +
            "target_entity_type, target_entity_id, relation_type) DO NOTHING",
            responseId, sourceEntityType, sourceEntityId, targetEntityType, targetEntityId, relationType
        );
    }

    private Integer resolveAlbumTypeCode(String albumType) {
        if (albumType == null) return null;
        return switch (albumType.toLowerCase()) {
            case "album"       -> 1;
            case "single"      -> 2;
            case "compilation" -> 3;
            case "ep"          -> 4;
            default            -> null;
        };
    }

    private Integer resolveDatePrecisionCode(String precision) {
        if (precision == null) return null;
        return switch (precision.toLowerCase()) {
            case "year"  -> 1;
            case "month" -> 2;
            case "day"   -> 3;
            default      -> null;
        };
    }
}
