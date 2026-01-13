package yurykorzun.art.universe.music.data.raw.lastfm.maintenance.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.common.LastfmEntityType;
import yurykorzun.art.universe.music.data.raw.lastfm.maintenance.dto.MasterBatchUnbindRequestDTO;
import yurykorzun.art.universe.music.data.raw.lastfm.maintenance.dto.MasterBatchUnbindResponseDTO;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class MusicDataIntegrationService {

    private final int masterDataUnbindBatchSize;
    private final RestClient restClient;

    public MusicDataIntegrationService(
        RestClient.Builder restClientBuilder,
        @Value("${master.base-url}") String musicDataBaseUrl,
        @Value("${lastfm.tasks.maintenance.db.unbind.batch-size}") int masterDataUnbindBatchSize
    ) {
        if (masterDataUnbindBatchSize < 10) {
            throw new IllegalArgumentException(String.format("Too small batch size %d, expected at least %d", masterDataUnbindBatchSize, 10));
        }
        this.masterDataUnbindBatchSize = masterDataUnbindBatchSize;

        String fullUrl = musicDataBaseUrl.startsWith("http") ? musicDataBaseUrl : "http://" + musicDataBaseUrl;
        this.restClient = restClientBuilder
            .baseUrl(fullUrl)
            .build();

    }

    /**
     * Unbind entities of specific type from music-data module
     */
    public void unbindEntities(LastfmEntityType entityType, List<Long> entityIds) {
        if (entityIds == null || entityIds.isEmpty()) {
            log.debug("No entities of type {} to unbind", entityType);
            return;
        }

        log.info("Unbinding {} entities of type {} from music-data", entityIds.size(), entityType);

        // Split into batches of MAX_BATCH_SIZE
        List<List<Long>> batches = splitIntoBatches(entityIds, masterDataUnbindBatchSize);
        log.debug("Split {} entity IDs into {} batches of max {} items each", 
            entityIds.size(), batches.size(), masterDataUnbindBatchSize);

        int totalProcessed = 0;
        int totalSuccessCount = 0;
        int totalNotFoundCount = 0;

        for (int i = 0; i < batches.size(); i++) {
            List<Long> batch = batches.get(i);
            log.debug("Processing batch {}/{} with {} entity IDs", i + 1, batches.size(), batch.size());
            
            MasterBatchUnbindResponseDTO response = unbindBatch(entityType, batch);
            if (response != null) {
                totalProcessed += response.getTotalProcessed();
                totalSuccessCount += response.getSuccessCount();
                totalNotFoundCount += response.getNotFoundCount();
            }
        }

        log.info("Batch unbinding completed for {}: {} total processed, {} unbound, {} not found",
            entityType, totalProcessed, totalSuccessCount, totalNotFoundCount);
    }

    /**
     * Split list into batches of specified size
     */
    private List<List<Long>> splitIntoBatches(List<Long> entityIds, int batchSize) {
        List<List<Long>> batches = new ArrayList<>();
        for (int i = 0; i < entityIds.size(); i += batchSize) {
            int endIndex = Math.min(i + batchSize, entityIds.size());
            batches.add(new ArrayList<>(entityIds.subList(i, endIndex)));
        }
        return batches;
    }

    /**
     * Unbind a single batch of entities
     */
    private MasterBatchUnbindResponseDTO unbindBatch(LastfmEntityType entityType, List<Long> entityIds) {
        String entityTypePath = mapEntityTypeToPath(entityType);
        String url = String.format("/api/v1/%s/unbind/lastfm/batch", entityTypePath);

        MasterBatchUnbindRequestDTO request = new MasterBatchUnbindRequestDTO(entityIds);

        try {
            MasterBatchUnbindResponseDTO response = restClient.method(HttpMethod.DELETE)
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .toEntity(MasterBatchUnbindResponseDTO.class)
                .getBody();
            
            if (response == null) {
                throw new IllegalArgumentException("Null response from master batch unbind has been received");
            }

            log.debug("Batch unbinding result for {} batch: {} processed, {} unbound, {} not found",
                entityType, response.getTotalProcessed(), response.getSuccessCount(), response.getNotFoundCount());
            
            return response;
            
        } catch (Exception e) {
            log.error("Failed to unbind batch of {} entities of type {}: {}", 
                entityIds.size(), entityType, e.getMessage(), e);
            // Don't rethrow - we want cleanup to continue even if unbinding fails
            return null;
        }
    }

    private String mapEntityTypeToPath(LastfmEntityType entityType) {
        return switch (entityType) {
            case LastfmEntityType.ARTIST -> "artists";
            case LastfmEntityType.ALBUM -> "albums";
            case LastfmEntityType.TRACK -> "tracks";
            case LastfmEntityType.TAG -> "categories"; // tags are mapped to categories in music-data
        };
    }
}
