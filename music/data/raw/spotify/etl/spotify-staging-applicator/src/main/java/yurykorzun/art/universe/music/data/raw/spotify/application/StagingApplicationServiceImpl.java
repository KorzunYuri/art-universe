package yurykorzun.art.universe.music.data.raw.spotify.application;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import yurykorzun.art.universe.music.data.raw.spotify.etl.entity.StagingIteration;
import yurykorzun.art.universe.music.data.raw.spotify.etl.entity.StagingIterationStatus;
import yurykorzun.art.universe.music.data.raw.spotify.etl.repository.SpotifyStagingIterationRepository;

import java.time.Instant;
import java.util.List;

@Service
@Slf4j
public class StagingApplicationServiceImpl implements StagingApplicationService {

    private final SpotifyStagingIterationRepository iterationRepository;
    private final JdbcTemplate jdbc;

    public StagingApplicationServiceImpl(
        SpotifyStagingIterationRepository iterationRepository,
        JdbcTemplate jdbc
    ) {
        this.iterationRepository = iterationRepository;
        this.jdbc = jdbc;
    }

    @Override
    public void applySealedIterations() {
        List<StagingIteration> sealed = iterationRepository.findAllByStatusOrderByOpenedAtAsc(StagingIterationStatus.SEALED);
        if (sealed.isEmpty()) {
            return;
        }
        log.info("Found {} sealed staging iterations to apply", sealed.size());
        for (StagingIteration iteration : sealed) {
            applyIteration(iteration);
        }
    }

    @Transactional
    protected void applyIteration(StagingIteration iteration) {
        long n = iteration.getId();
        log.info("Applying staging iteration {}", n);

        iteration.setStatus(StagingIterationStatus.APPLYING);
        iterationRepository.save(iteration);

        try {
            long applied = 0;

            // 1. Upsert artists
            applied += upsertArtists(n);

            // 2. Resolve synthetic artist IDs in stg_album and stg_entity_relation
            resolveArtistIdsInAlbum(n);
            resolveEntityIdsInRelation(n, 1, "artist"); // ARTIST type code = 1

            // 3. Upsert albums
            applied += upsertAlbums(n);

            // 4. Resolve synthetic album IDs in stg_track and stg_entity_relation
            resolveAlbumIdsInTrack(n);
            resolveArtistIdsInTrack(n);
            resolveEntityIdsInRelation(n, 2, "album"); // ALBUM type code = 2

            // 5. Upsert tracks
            applied += upsertTracks(n);

            // 6. Resolve synthetic track IDs in stg_entity_relation
            resolveEntityIdsInRelation(n, 3, "track"); // TRACK type code = 3

            // 7. Upsert entity relations (only fully resolved rows)
            applied += upsertEntityRelations(n);

            iteration.setStatus(StagingIterationStatus.COMPLETED);
            iteration.setAppliedAt(Instant.now());
            iteration.setRecordsApplied(applied);
            iterationRepository.save(iteration);

            log.info("Completed staging iteration {}: {} records applied", n, applied);

        } catch (Exception e) {
            log.error("Failed to apply staging iteration {}: {}", n, e.getMessage(), e);
            iteration.setStatus(StagingIterationStatus.FAILED);
            iteration.setErrorMessage(e.getMessage());
            iterationRepository.save(iteration);
        }
    }

    private long upsertArtists(long n) {
        int rows = jdbc.update("""
            INSERT INTO artist (spotify_id, name, spotify_url, uri)
            SELECT spotify_id, name, spotify_url, uri
            FROM stg_artist_%d
            WHERE name IS NOT NULL
            ON CONFLICT (spotify_id) DO UPDATE
              SET name        = EXCLUDED.name,
                  spotify_url = COALESCE(EXCLUDED.spotify_url, artist.spotify_url),
                  uri         = COALESCE(EXCLUDED.uri, artist.uri),
                  updated_at  = now()
            """.formatted(n));
        log.debug("Iteration {}: upserted {} artists", n, rows);
        return rows;
    }

    private long upsertAlbums(long n) {
        int rows = jdbc.update("""
            INSERT INTO album (spotify_id, name, album_type, total_tracks, release_date,
                               release_date_precision, spotify_url, uri,
                               primary_artist_id, primary_artist_spotify_id)
            SELECT spotify_id, name, album_type, total_tracks, release_date,
                   release_date_precision, spotify_url, uri,
                   NULLIF(primary_artist_id, 0), primary_artist_spotify_id
            FROM stg_album_%d
            WHERE name IS NOT NULL
            ON CONFLICT (spotify_id) DO UPDATE
              SET name                     = EXCLUDED.name,
                  album_type               = COALESCE(EXCLUDED.album_type, album.album_type),
                  total_tracks             = COALESCE(EXCLUDED.total_tracks, album.total_tracks),
                  release_date             = COALESCE(EXCLUDED.release_date, album.release_date),
                  release_date_precision   = COALESCE(EXCLUDED.release_date_precision, album.release_date_precision),
                  spotify_url              = COALESCE(EXCLUDED.spotify_url, album.spotify_url),
                  uri                      = COALESCE(EXCLUDED.uri, album.uri),
                  primary_artist_id        = COALESCE(EXCLUDED.primary_artist_id, album.primary_artist_id),
                  primary_artist_spotify_id = COALESCE(EXCLUDED.primary_artist_spotify_id, album.primary_artist_spotify_id),
                  updated_at               = now()
            """.formatted(n));
        log.debug("Iteration {}: upserted {} albums", n, rows);
        return rows;
    }

    private long upsertTracks(long n) {
        int rows = jdbc.update("""
            INSERT INTO track (spotify_id, name, duration_ms, track_number, disc_number,
                               has_explicit_lyrics, is_playable, spotify_url, uri,
                               isrc, ean, upc,
                               primary_artist_id, primary_artist_spotify_id,
                               album_id, album_spotify_id)
            SELECT spotify_id, name, duration_ms, track_number, disc_number,
                   has_explicit_lyrics, is_playable, spotify_url, uri,
                   isrc, ean, upc,
                   NULLIF(primary_artist_id, 0), primary_artist_spotify_id,
                   NULLIF(album_id, 0), album_spotify_id
            FROM stg_track_%d
            WHERE name IS NOT NULL
            ON CONFLICT (spotify_id) DO UPDATE
              SET name                     = EXCLUDED.name,
                  duration_ms              = COALESCE(EXCLUDED.duration_ms, track.duration_ms),
                  track_number             = COALESCE(EXCLUDED.track_number, track.track_number),
                  disc_number              = COALESCE(EXCLUDED.disc_number, track.disc_number),
                  has_explicit_lyrics      = COALESCE(EXCLUDED.has_explicit_lyrics, track.has_explicit_lyrics),
                  is_playable              = COALESCE(EXCLUDED.is_playable, track.is_playable),
                  spotify_url              = COALESCE(EXCLUDED.spotify_url, track.spotify_url),
                  uri                      = COALESCE(EXCLUDED.uri, track.uri),
                  isrc                     = COALESCE(EXCLUDED.isrc, track.isrc),
                  primary_artist_id        = COALESCE(EXCLUDED.primary_artist_id, track.primary_artist_id),
                  primary_artist_spotify_id = COALESCE(EXCLUDED.primary_artist_spotify_id, track.primary_artist_spotify_id),
                  album_id                 = COALESCE(EXCLUDED.album_id, track.album_id),
                  album_spotify_id         = COALESCE(EXCLUDED.album_spotify_id, track.album_spotify_id),
                  updated_at               = now()
            """.formatted(n));
        log.debug("Iteration {}: upserted {} tracks", n, rows);
        return rows;
    }

    private long upsertEntityRelations(long n) {
        // Only insert rows where IDs are fully resolved (no synthetic/negative IDs remain)
        int rows = jdbc.update("""
            INSERT INTO entity_relation (source_entity_type, source_entity_id,
                                         target_entity_type, target_entity_id,
                                         relation_type)
            SELECT source_entity_type, source_entity_id,
                   target_entity_type, target_entity_id,
                   relation_type
            FROM stg_entity_relation_%d
            WHERE source_entity_id > 0
              AND target_entity_id > 0
            ON CONFLICT (source_entity_type, source_entity_id,
                          target_entity_type, target_entity_id, relation_type) DO NOTHING
            """.formatted(n));
        log.debug("Iteration {}: upserted {} entity relations", n, rows);
        return rows;
    }

    // --- Synthetic ID resolution ---

    private void resolveArtistIdsInAlbum(long n) {
        jdbc.update("""
            UPDATE stg_album_%d s
            SET primary_artist_id = a.id
            FROM artist a
            WHERE a.spotify_id = s.primary_artist_spotify_id
              AND s.primary_artist_id < 0
            """.formatted(n));
    }

    private void resolveArtistIdsInTrack(long n) {
        jdbc.update("""
            UPDATE stg_track_%d s
            SET primary_artist_id = a.id
            FROM artist a
            WHERE a.spotify_id = s.primary_artist_spotify_id
              AND s.primary_artist_id < 0
            """.formatted(n));
    }

    private void resolveAlbumIdsInTrack(long n) {
        jdbc.update("""
            UPDATE stg_track_%d s
            SET album_id = al.id
            FROM album al
            WHERE al.spotify_id = s.album_spotify_id
              AND s.album_id < 0
            """.formatted(n));
    }

    /**
     * Resolves synthetic IDs in stg_entity_relation for a given entity type.
     *
     * @param entityTypeCode numeric code (1=ARTIST, 2=ALBUM, 3=TRACK)
     * @param tableName      target table name (artist / album / track)
     */
    private void resolveEntityIdsInRelation(long n, int entityTypeCode, String tableName) {
        // Resolve source entity IDs
        jdbc.update("""
            UPDATE stg_entity_relation_%d er
            SET source_entity_id = t.id
            FROM stg_%s_%d s
            JOIN %s t ON t.spotify_id = s.spotify_id
            WHERE er.source_entity_type = %d
              AND er.source_entity_id   = s.entity_id
              AND er.source_entity_id   < 0
            """.formatted(n, tableName, n, tableName, entityTypeCode));

        // Resolve target entity IDs
        jdbc.update("""
            UPDATE stg_entity_relation_%d er
            SET target_entity_id = t.id
            FROM stg_%s_%d s
            JOIN %s t ON t.spotify_id = s.spotify_id
            WHERE er.target_entity_type = %d
              AND er.target_entity_id   = s.entity_id
              AND er.target_entity_id   < 0
            """.formatted(n, tableName, n, tableName, entityTypeCode));
    }
}
