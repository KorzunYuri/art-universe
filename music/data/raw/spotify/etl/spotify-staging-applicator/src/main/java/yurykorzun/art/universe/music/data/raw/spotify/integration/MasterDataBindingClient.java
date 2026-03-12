package yurykorzun.art.universe.music.data.raw.spotify.integration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Slf4j
public class MasterDataBindingClient {

    private final RestClient restClient;

    public MasterDataBindingClient(RestClient restClient) {
        this.restClient = restClient;
    }

    /**
     * Binds an existing master artist to a Spotify entity via the internal API.
     *
     * @param rawEntityId the mu_raw_spotify.artist.id
     * @param masterId    the mu.artist.id (master entity)
     */
    public void bindArtistToExisting(long rawEntityId, long masterId) {
        bindEntityToExisting("artists", rawEntityId, masterId);
    }

    /**
     * Binds an existing master album to a Spotify entity via the internal API.
     *
     * @param rawEntityId the mu_raw_spotify.album.id
     * @param masterId    the mu.album.id (master entity)
     */
    public void bindAlbumToExisting(long rawEntityId, long masterId) {
        bindEntityToExisting("albums", rawEntityId, masterId);
    }

    /**
     * Binds an existing master track to a Spotify entity via the internal API.
     *
     * @param rawEntityId the mu_raw_spotify.track.id
     * @param masterId    the mu.track.id (master entity)
     */
    public void bindTrackToExisting(long rawEntityId, long masterId) {
        bindEntityToExisting("tracks", rawEntityId, masterId);
    }

    private void bindEntityToExisting(String entityPath, long rawEntityId, long masterId) {
        try {
            restClient.post()
                .uri("/api/v1/internal/{entityPath}/bind/existing/spotify/{externalId}?approvalStatus=pending",
                    entityPath, rawEntityId)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("masterId", masterId))
                .retrieve()
                .toBodilessEntity();

            log.info("Created binding: {} rawEntityId={} -> masterId={}", entityPath, rawEntityId, masterId);
        } catch (Exception e) {
            log.error("Failed to create binding: {} rawEntityId={} -> masterId={}: {}",
                entityPath, rawEntityId, masterId, e.getMessage());
            throw e;
        }
    }
}
