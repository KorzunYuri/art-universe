package yurykorzun.art.universe.music.data.raw.spotify.etl.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import yurykorzun.art.universe.music.data.raw.spotify.etl.dto.SpotifyApiCallCreateRequest;
import yurykorzun.art.universe.music.data.raw.spotify.etl.entity.SpotifyApiCall;
import yurykorzun.art.universe.music.data.raw.spotify.etl.repository.SpotifyApiCallRepository;

import yurykorzun.art.universe.common.pgnotify.PgNotifyEventPublisher;
import yurykorzun.art.universe.music.data.raw.spotify.common.SpotifyConstants;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class SpotifyApiCallServiceImpl implements SpotifyApiCallService {

    private final SpotifyApiCallRepository apiCallRepository;
    private final PgNotifyEventPublisher pgNotifyEventPublisher;

    public SpotifyApiCallServiceImpl(
        SpotifyApiCallRepository apiCallRepository,
        PgNotifyEventPublisher pgNotifyEventPublisher
    ) {
        this.apiCallRepository = apiCallRepository;
        this.pgNotifyEventPublisher = pgNotifyEventPublisher;
    }

    @Override
    @Transactional
    public List<SpotifyApiCall> createApiCalls(List<SpotifyApiCallCreateRequest> requests) {
        List<SpotifyApiCall> calls = requests.stream()
            .map(this::toEntity)
            .toList();
        List<SpotifyApiCall> saved = apiCallRepository.saveAll(calls);
        log.debug("Created {} api_call records", saved.size());
        if (!saved.isEmpty()) {
            pgNotifyEventPublisher.notifyAfterCommit(SpotifyConstants.NOTIFY_CALLS_READY);
        }
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
