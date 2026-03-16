package yurykorzun.art.universe.music.data.raw.spotify.kafka;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;
import yurykorzun.art.universe.common.config.client.ConfigPropertyHolder;
import yurykorzun.art.universe.common.messaging.kafka.KafkaConsumerConfig;
import yurykorzun.art.universe.music.data.raw.spotify.config.SpotifyParserProperty;
import yurykorzun.art.universe.music.data.raw.spotify.etl.entity.SpotifyApiResponse;
import yurykorzun.art.universe.music.data.raw.spotify.etl.messaging.SpotifyKafkaTopics;
import yurykorzun.art.universe.music.data.raw.spotify.etl.messaging.SpotifyResponseMessage;
import yurykorzun.art.universe.music.data.raw.spotify.etl.repository.SpotifyApiResponseRepository;
import yurykorzun.art.universe.music.data.raw.spotify.task.response.process.SpotifyApiResponseProcessingOrchestrator;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@Slf4j
public class ResponseKafkaConsumer {

    private static final String GROUP_ID = "spotify-response-parser";
    private static final Duration POLL_TIMEOUT = Duration.ofMillis(100);

    private final KafkaConsumerConfig kafkaConsumerConfig;
    private final ConfigPropertyHolder configPropertyHolder;
    private final SpotifyApiResponseRepository apiResponseRepository;
    private final SpotifyApiResponseProcessingOrchestrator orchestrator;
    private final ThreadPoolTaskScheduler taskScheduler;

    private Consumer<String, SpotifyResponseMessage> consumer;
    private final AtomicInteger cycleCount = new AtomicInteger(0);
    private volatile boolean running = false;

    public ResponseKafkaConsumer(
            KafkaConsumerConfig kafkaConsumerConfig,
            ConfigPropertyHolder configPropertyHolder,
            SpotifyApiResponseRepository apiResponseRepository,
            SpotifyApiResponseProcessingOrchestrator orchestrator,
            ThreadPoolTaskScheduler taskScheduler
    ) {
        this.kafkaConsumerConfig = kafkaConsumerConfig;
        this.configPropertyHolder = configPropertyHolder;
        this.apiResponseRepository = apiResponseRepository;
        this.orchestrator = orchestrator;
        this.taskScheduler = taskScheduler;
    }

    public void start() {
        log.info("Starting ResponseKafkaConsumer (Kafka mode)");
        running = true;
        initConsumer();
        scheduleNext();
    }

    @PreDestroy
    public void stop() {
        running = false;
        if (consumer != null) {
            try {
                consumer.close(Duration.ofSeconds(5));
            } catch (Exception e) {
                log.warn("Error closing Kafka consumer: {}", e.getMessage());
            }
        }
        log.info("ResponseKafkaConsumer stopped");
    }

    private void initConsumer() {
        Map<String, Object> props = kafkaConsumerConfig.buildConsumerProperties(GROUP_ID, SpotifyResponseMessage.class);
        consumer = new KafkaConsumer<>(props);
        consumer.subscribe(List.of(SpotifyKafkaTopics.RESPONSES_TOPIC));
        log.debug("Created Kafka consumer for topic {}", SpotifyKafkaTopics.RESPONSES_TOPIC);
    }

    private void scheduleNext() {
        if (!running) return;
        long delaySecs = configPropertyHolder.getInt(SpotifyParserProperty.SCHEDULE_DELAY_SECS);
        taskScheduler.schedule(this::consumeCycleAndReschedule, Instant.now().plusSeconds(delaySecs));
    }

    private void consumeCycleAndReschedule() {
        try {
            consumeCycle();
        } catch (Exception e) {
            log.error("Error in Kafka consume cycle", e);
        } finally {
            scheduleNext();
        }
    }

    void consumeCycle() {
        int cycle = cycleCount.incrementAndGet();
        int batchSize = configPropertyHolder.getInt(SpotifyParserProperty.KAFKA_BATCH_SIZE);

        ConsumerRecords<String, SpotifyResponseMessage> records = consumer.poll(POLL_TIMEOUT);

        int consumed = 0;
        for (ConsumerRecord<String, SpotifyResponseMessage> record : records) {
            if (consumed >= batchSize) break;
            try {
                processMessage(record.value());
                consumed++;
            } catch (Exception e) {
                log.error("Error processing Kafka response message for api_response {}: {}",
                        record.value().apiResponseId(), e.getMessage());
            }
        }

        consumer.commitSync();

        // Periodic orphan sweep
        int orphanSweepCycles = configPropertyHolder.getInt(SpotifyParserProperty.KAFKA_ORPHAN_SWEEP_CYCLES);
        if (cycle % orphanSweepCycles == 0) {
            sweepOrphans();
        }
    }

    private void processMessage(SpotifyResponseMessage msg) {
        Optional<SpotifyApiResponse> responseOpt = apiResponseRepository.findById(msg.apiResponseId());
        if (responseOpt.isEmpty()) {
            log.debug("Skipping api_response {} — not found in DB", msg.apiResponseId());
            return;
        }

        SpotifyApiResponse response = responseOpt.get();
        orchestrator.processSingleResponse(response);
    }

    private void sweepOrphans() {
        orchestrator.processResponses();
    }
}
