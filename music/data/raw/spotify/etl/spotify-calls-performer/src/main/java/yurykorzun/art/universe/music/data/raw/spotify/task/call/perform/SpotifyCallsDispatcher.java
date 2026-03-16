package yurykorzun.art.universe.music.data.raw.spotify.task.call.perform;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import yurykorzun.art.universe.common.config.client.ConfigPropertyHolder;
import yurykorzun.art.universe.music.data.raw.spotify.config.SpotifyCommonProperty;
import yurykorzun.art.universe.music.data.raw.spotify.kafka.WeightedCallConsumer;

@Component
@Slf4j
public class SpotifyCallsDispatcher {

    private final ConfigPropertyHolder configPropertyHolder;
    private final WeightedCallConsumer weightedCallConsumer;
    private final SpotifyApiCallExecutionScheduler legacyScheduler;

    public SpotifyCallsDispatcher(
            ConfigPropertyHolder configPropertyHolder,
            WeightedCallConsumer weightedCallConsumer,
            SpotifyApiCallExecutionScheduler legacyScheduler
    ) {
        this.configPropertyHolder = configPropertyHolder;
        this.weightedCallConsumer = weightedCallConsumer;
        this.legacyScheduler = legacyScheduler;
    }

    @PostConstruct
    public void start() {
        boolean kafkaEnabled = configPropertyHolder.getBoolean(SpotifyCommonProperty.KAFKA_ENABLED);
        if (kafkaEnabled) {
            log.info("Kafka mode enabled — starting WeightedCallConsumer");
            weightedCallConsumer.start();
        } else {
            log.info("DB-polling mode — starting legacy SpotifyApiCallExecutionScheduler");
            legacyScheduler.start();
        }
    }
}
