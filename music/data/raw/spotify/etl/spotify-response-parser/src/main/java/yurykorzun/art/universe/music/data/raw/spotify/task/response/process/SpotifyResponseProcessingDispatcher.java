package yurykorzun.art.universe.music.data.raw.spotify.task.response.process;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import yurykorzun.art.universe.common.config.client.ConfigPropertyHolder;
import yurykorzun.art.universe.music.data.raw.spotify.config.SpotifyCommonProperty;
import yurykorzun.art.universe.music.data.raw.spotify.kafka.ResponseKafkaConsumer;

@Component
@Slf4j
public class SpotifyResponseProcessingDispatcher {

    private final ConfigPropertyHolder configPropertyHolder;
    private final ResponseKafkaConsumer responseKafkaConsumer;
    private final SpotifyApiResponseProcessingScheduler legacyScheduler;

    public SpotifyResponseProcessingDispatcher(
            ConfigPropertyHolder configPropertyHolder,
            ResponseKafkaConsumer responseKafkaConsumer,
            SpotifyApiResponseProcessingScheduler legacyScheduler
    ) {
        this.configPropertyHolder = configPropertyHolder;
        this.responseKafkaConsumer = responseKafkaConsumer;
        this.legacyScheduler = legacyScheduler;
    }

    @PostConstruct
    public void start() {
        boolean kafkaEnabled = configPropertyHolder.getBoolean(SpotifyCommonProperty.KAFKA_ENABLED);
        if (kafkaEnabled) {
            log.info("Kafka mode enabled — starting ResponseKafkaConsumer");
            responseKafkaConsumer.start();
        } else {
            log.info("DB-polling mode — starting legacy SpotifyApiResponseProcessingScheduler");
            legacyScheduler.start();
        }
    }
}
