package yurykorzun.art.universe.music.data.raw.lastfm.api.client.repository;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import yurykorzun.art.universe.common.persistence.converter.GzipBase64StringConverter;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.config.LastfmMigrationConfig;
import yurykorzun.art.universe.music.data.raw.lastfm.common.LastfmConstants;

import java.util.List;

@Component
@Slf4j
public class LastfmApiResponseBodyGzipBase64Migrator {

    private final EntityManager em;
    private final GzipBase64StringConverter converter;
    private final LastfmMigrationConfig migrationConfig;

    private static final int BATCH_SIZE = LastfmConstants.HIBERNATE_BATCH_SIZE;

    public LastfmApiResponseBodyGzipBase64Migrator(EntityManager em, LastfmMigrationConfig migrationConfig) {
        this.em = em;
        this.migrationConfig = migrationConfig;
        this.converter = new GzipBase64StringConverter();
    }

    @Transactional
    @EventListener(ApplicationReadyEvent.class)
    @SuppressWarnings("unchecked")
    public void migrateApiResponseBody() {
        log.info("Starting API response compression");
        migrationConfig.setApiResponseBodyMigrationInProgress(true);

        int migratedRowsCount = 0;
        List<Object[]> rows = null;
        do {
            rows = em.createNativeQuery("""
                SELECT  id, response_body_json
                FROM    api_response
                WHERE   is_response_encoded = false
                LIMIT   :batchSize
            """)
                .setParameter("batchSize", BATCH_SIZE)
                .getResultList();

            if (rows.isEmpty()) {
                log.info("No responses to migrate");
                break;
            }

            for (Object[] row : rows) {
                Long id = ((Number) row[0]).longValue();
                String json = (String) row[1];
                String compressed = converter.convertToDatabaseColumn(json);

                em.createNativeQuery("""
                    UPDATE api_response
                    SET response_body = :responseBody,
                        is_response_encoded = true
                    WHERE id = :id
                """)
                    .setParameter("responseBody", compressed)
                    .setParameter("id", id)
                    .executeUpdate();
            }

            migratedRowsCount += rows.size();
            log.info("Migrated responses: {}", migratedRowsCount);

        } while (!rows.isEmpty());

        log.info("Migrated {} responses in total", migratedRowsCount);
        migrationConfig.setApiResponseBodyMigrationInProgress(false);
    }
}
