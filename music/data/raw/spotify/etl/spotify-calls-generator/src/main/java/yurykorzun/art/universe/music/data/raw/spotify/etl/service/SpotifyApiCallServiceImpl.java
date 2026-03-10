package yurykorzun.art.universe.music.data.raw.spotify.etl.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import yurykorzun.art.universe.music.data.raw.spotify.etl.dto.SpotifyApiCallCreateRequest;
import yurykorzun.art.universe.music.data.raw.spotify.etl.entity.SpotifyApiCall;
import yurykorzun.art.universe.music.data.raw.spotify.etl.repository.SpotifyApiCallRepository;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class SpotifyApiCallServiceImpl implements SpotifyApiCallService {

    private final SpotifyApiCallRepository apiCallRepository;

    public SpotifyApiCallServiceImpl(SpotifyApiCallRepository apiCallRepository) {
        this.apiCallRepository = apiCallRepository;
    }

    @Override
    @Transactional
    public List<SpotifyApiCall> createApiCalls(List<SpotifyApiCallCreateRequest> requests) {
        List<SpotifyApiCall> calls = requests.stream()
            .map(this::toEntity)
            .toList();
        List<SpotifyApiCall> saved = apiCallRepository.saveAll(calls);
        log.debug("Created {} api_call records", saved.size());
        return saved;
    }

    private SpotifyApiCall toEntity(SpotifyApiCallCreateRequest req) {
        Map<String, String> params = req.getParams() != null
            ? req.getParams()
            : Map.of("spotify_id", req.getSpotifyId());
        return SpotifyApiCall.builder()
            .type(req.getType())
            .spotifyId(req.getSpotifyId())
            .entityType(req.getEntityType())
            .entityId(req.getEntityId())
            .dueDttm(req.getDueDttm())
            .params(params)
            .build();
    }
}
