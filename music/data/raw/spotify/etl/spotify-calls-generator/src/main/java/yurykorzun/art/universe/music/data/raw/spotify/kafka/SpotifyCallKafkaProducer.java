package yurykorzun.art.universe.music.data.raw.spotify.kafka;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import yurykorzun.art.universe.music.data.raw.spotify.etl.entity.SpotifyApiCall;
import yurykorzun.art.universe.music.data.raw.spotify.etl.messaging.SpotifyCallMessage;
import yurykorzun.art.universe.music.data.raw.spotify.etl.messaging.SpotifyKafkaTopics;
import yurykorzun.art.universe.music.data.raw.spotify.etl.service.SpotifyApiCallService;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@Slf4j
public class SpotifyCallKafkaProducer {

    private static final int CIRCUIT_BREAKER_THRESHOLD = 5;

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final SpotifyApiCallService apiCallService;
    private final AtomicInteger consecutiveFailures = new AtomicInteger(0);
    @Getter
    private volatile boolean available = true;

    public SpotifyCallKafkaProducer(
            KafkaTemplate<String, Object> kafkaTemplate,
            SpotifyApiCallService apiCallService
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.apiCallService = apiCallService;
    }

    public void produceAll(List<SpotifyApiCall> calls) {
        if (!available) {
            log.warn("Kafka producer circuit breaker open — skipping production for {} calls", calls.size());
            return;
        }

        for (SpotifyApiCall call : calls) {
            produce(call);
        }
    }

    private void produce(SpotifyApiCall call) {
        String topic = SpotifyKafkaTopics.callTopicFor(call.getType());
        String key = call.getSpotifyId();

        SpotifyCallMessage message = toKafkaMessage(call);

        kafkaTemplate.send(topic, key, message)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        handleFailure(call, topic, ex);
                    } else {
                        handleSuccess(call, topic);
                    }
                });
    }

    private static SpotifyCallMessage toKafkaMessage(SpotifyApiCall call) {
        return new SpotifyCallMessage(
                call.getId(),
                call.getType().getCode(),
                call.getSpotifyId(),
                call.getEntityType() != null ? call.getEntityType().getCode() : null,
                call.getEntityId(),
                Instant.now()
        );
    }

    private void handleSuccess(SpotifyApiCall call, String topic) {
        consecutiveFailures.set(0);
        available = true;
        apiCallService.markAsProduced(call.getId(), topic);
        log.debug("Produced api_call {} to topic {}", call.getId(), topic);
    }

    private void handleFailure(SpotifyApiCall call, String topic, Throwable ex) {
        int failures = consecutiveFailures.incrementAndGet();
        if (failures >= CIRCUIT_BREAKER_THRESHOLD) {
            available = false;
            log.error("Kafka producer circuit breaker opened after {} consecutive failures", failures);
        }
        log.error("Failed to produce api_call {} to topic {}: {}", call.getId(), topic, ex.getMessage());
    }

}
