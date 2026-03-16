package yurykorzun.art.universe.music.data.raw.spotify.kafka;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import yurykorzun.art.universe.music.data.raw.spotify.etl.entity.SpotifyApiCall;
import yurykorzun.art.universe.music.data.raw.spotify.etl.messaging.SpotifyKafkaTopics;
import yurykorzun.art.universe.music.data.raw.spotify.etl.messaging.SpotifyResponseMessage;

import java.time.Instant;

@Component
@Slf4j
public class SpotifyResponseKafkaProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public SpotifyResponseKafkaProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void produce(long apiResponseId, SpotifyApiCall call) {
        SpotifyResponseMessage message = new SpotifyResponseMessage(
                apiResponseId,
                call.getId(),
                call.getType().getCode(),
                Instant.now()
        );

        kafkaTemplate.send(SpotifyKafkaTopics.RESPONSES_TOPIC, String.valueOf(call.getId()), message)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.warn("Failed to produce response message for api_response {} to Kafka: {}",
                                apiResponseId, ex.getMessage());
                    } else {
                        log.debug("Produced response message for api_response {} to {}",
                                apiResponseId, SpotifyKafkaTopics.RESPONSES_TOPIC);
                    }
                });
    }
}
