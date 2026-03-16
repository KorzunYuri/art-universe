package yurykorzun.art.universe.music.data.raw.spotify.etl.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import yurykorzun.art.universe.common.config.client.ConfigPropertyHolder;
import yurykorzun.art.universe.music.data.raw.spotify.config.SpotifyCommonProperty;
import yurykorzun.art.universe.music.data.raw.spotify.etl.dto.SpotifyApiCallCreateRequest;
import yurykorzun.art.universe.music.data.raw.spotify.etl.entity.SpotifyApiCall;
import yurykorzun.art.universe.music.data.raw.spotify.etl.repository.SpotifyApiCallRepository;
import yurykorzun.art.universe.music.data.raw.spotify.kafka.SpotifyCallKafkaProducer;

import yurykorzun.art.universe.data.raw.common.etl.entity.ApiCallStatus;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class SpotifyApiCallServiceImpl implements SpotifyApiCallService {

    private final SpotifyApiCallRepository apiCallRepository;
    private final SpotifyCallKafkaProducer kafkaProducer;
    private final ConfigPropertyHolder configPropertyHolder;

    public SpotifyApiCallServiceImpl(
            SpotifyApiCallRepository apiCallRepository,
            SpotifyCallKafkaProducer kafkaProducer,
            ConfigPropertyHolder configPropertyHolder
    ) {
        this.apiCallRepository = apiCallRepository;
        this.kafkaProducer = kafkaProducer;
        this.configPropertyHolder = configPropertyHolder;
    }

    @Override
    @Transactional
    public List<SpotifyApiCall> createApiCalls(List<SpotifyApiCallCreateRequest> requests) {
        List<SpotifyApiCall> calls = requests.stream()
            .map(this::toEntity)
            .toList();
        List<SpotifyApiCall> saved = apiCallRepository.saveAll(calls);
        log.debug("Created {} api_call records", saved.size());

        boolean kafkaEnabled = configPropertyHolder.getBoolean(SpotifyCommonProperty.KAFKA_ENABLED);
        if (kafkaEnabled && kafkaProducer.isAvailable()) {
            kafkaProducer.produceAll(saved);
        } else if (kafkaEnabled) {
            log.warn("Kafka enabled but producer unavailable — {} calls will remain CREATED for DB polling fallback",
                    saved.size());
        }

        return saved;
    }

    @Override
    @Transactional
    public void markAsProduced(long callId, String kafkaTopic) {
        apiCallRepository.findById(callId).ifPresent(call -> {
            call.setKafkaProduced(true);
            call.setKafkaTopic(kafkaTopic);
            call.setStatus(ApiCallStatus.PENDING);
            apiCallRepository.save(call);
        });
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
