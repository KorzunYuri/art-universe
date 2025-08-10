package yurykorzun.art.universe.music.data.raw.lastfm.maintenance.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.entity.LastfmEntityType;
import yurykorzun.art.universe.music.data.raw.lastfm.maintenance.dto.MasterBatchUnbindRequestDTO;
import yurykorzun.art.universe.music.data.raw.lastfm.maintenance.dto.MasterBatchUnbindResponseDTO;

import java.util.List;

@Service
@Slf4j
public class MusicDataIntegrationService {

    private final RestClient restClient;

    public MusicDataIntegrationService(RestClient.Builder restClientBuilder, @Value("${music-data-master.base-url}") String musicDataBaseUrl) {
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

        String entityTypePath = mapEntityTypeToPath(entityType);
        String url = String.format("/api/v1/%s/unbind/lastfm/batch", entityTypePath);

        MasterBatchUnbindRequestDTO request = new MasterBatchUnbindRequestDTO(entityIds);

        try {
            log.info("Unbinding {} entities of type {} from music-data", entityIds.size(), entityType);

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

            log.info("Batch unbinding result for {} : {} processed, {} unbound, {} not found",
                entityType, response.getTotalProcessed(), response.getSuccessCount(), response.getNotFoundCount());
            
        } catch (Exception e) {
            log.error("Failed to unbind entities of type {}: {}", 
                entityType, e.getMessage(), e);
            // Don't rethrow - we want cleanup to continue even if unbinding fails
        }
    }

    private String mapEntityTypeToPath(LastfmEntityType entityType) {
        return switch (entityType) {
            case ARTIST -> "artists";
            case ALBUM -> "albums";
            case TRACK -> "tracks";
            case TAG -> "categories"; // tags are mapped to categories in music-data
        };
    }
}
