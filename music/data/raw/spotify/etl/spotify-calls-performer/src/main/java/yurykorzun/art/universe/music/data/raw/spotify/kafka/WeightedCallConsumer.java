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
import yurykorzun.art.universe.data.raw.common.etl.entity.ApiCallStatus;
import yurykorzun.art.universe.data.raw.common.integration.AdaptiveRateLimiter;
import yurykorzun.art.universe.music.data.raw.spotify.common.SpotifyConstants;
import yurykorzun.art.universe.music.data.raw.spotify.config.SpotifyPerformerProperty;
import yurykorzun.art.universe.music.data.raw.spotify.etl.entity.SpotifyApiCall;
import yurykorzun.art.universe.music.data.raw.spotify.etl.entity.SpotifyApiCallType;
import yurykorzun.art.universe.music.data.raw.spotify.etl.messaging.SpotifyCallMessage;
import yurykorzun.art.universe.music.data.raw.spotify.etl.messaging.SpotifyKafkaTopics;
import yurykorzun.art.universe.music.data.raw.spotify.etl.service.SpotifyApiCallService;
import yurykorzun.art.universe.music.data.raw.spotify.task.call.perform.SpotifyApiCallExecutor;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@Slf4j
public class WeightedCallConsumer {

    private static final String GROUP_ID = "spotify-calls-performer";
    private static final Duration POLL_TIMEOUT = Duration.ofMillis(100);

    private static final Map<SpotifyApiCallType, SpotifyPerformerProperty> WEIGHT_PROPERTIES = Map.of(
            SpotifyApiCallType.ARTIST_GET, SpotifyPerformerProperty.QUOTA_WEIGHT_ARTIST_GET,
            SpotifyApiCallType.ARTIST_ALBUMS, SpotifyPerformerProperty.QUOTA_WEIGHT_ARTIST_ALBUMS,
            SpotifyApiCallType.ALBUM_GET, SpotifyPerformerProperty.QUOTA_WEIGHT_ALBUM_GET,
            SpotifyApiCallType.ALBUM_TRACKS, SpotifyPerformerProperty.QUOTA_WEIGHT_ALBUM_TRACKS,
            SpotifyApiCallType.TRACK_GET, SpotifyPerformerProperty.QUOTA_WEIGHT_TRACK_GET,
            SpotifyApiCallType.SEARCH_ARTIST, SpotifyPerformerProperty.QUOTA_WEIGHT_SEARCH_ARTIST,
            SpotifyApiCallType.SEARCH_ALBUM, SpotifyPerformerProperty.QUOTA_WEIGHT_SEARCH_ALBUM,
            SpotifyApiCallType.SEARCH_TRACK, SpotifyPerformerProperty.QUOTA_WEIGHT_SEARCH_TRACK
    );

    private final KafkaConsumerConfig kafkaConsumerConfig;
    private final ConfigPropertyHolder configPropertyHolder;
    private final SpotifyApiCallService apiCallService;
    private final SpotifyApiCallExecutor executor;
    private final AdaptiveRateLimiter rateLimiter;
    private final ThreadPoolTaskScheduler taskScheduler;

    private final Map<SpotifyApiCallType, Consumer<String, SpotifyCallMessage>> consumers = new EnumMap<>(SpotifyApiCallType.class);
    private final AtomicInteger cycleCount = new AtomicInteger(0);
    private volatile boolean running = false;

    public WeightedCallConsumer(
            KafkaConsumerConfig kafkaConsumerConfig,
            ConfigPropertyHolder configPropertyHolder,
            SpotifyApiCallService apiCallService,
            SpotifyApiCallExecutor executor,
            AdaptiveRateLimiter rateLimiter,
            ThreadPoolTaskScheduler taskScheduler
    ) {
        this.kafkaConsumerConfig = kafkaConsumerConfig;
        this.configPropertyHolder = configPropertyHolder;
        this.apiCallService = apiCallService;
        this.executor = executor;
        this.rateLimiter = rateLimiter;
        this.taskScheduler = taskScheduler;
    }

    public void start() {
        log.info("Starting WeightedCallConsumer (Kafka mode)");
        running = true;
        initConsumers();
        scheduleNext();
    }

    @PreDestroy
    public void stop() {
        running = false;
        consumers.values().forEach(consumer -> {
            try {
                consumer.close(Duration.ofSeconds(5));
            } catch (Exception e) {
                log.warn("Error closing Kafka consumer: {}", e.getMessage());
            }
        });
        consumers.clear();
        log.info("WeightedCallConsumer stopped");
    }

    private void initConsumers() {
        Map<String, Object> baseProps = kafkaConsumerConfig.buildConsumerProperties(GROUP_ID, SpotifyCallMessage.class);

        for (SpotifyApiCallType type : SpotifyApiCallType.values()) {
            String topic = SpotifyKafkaTopics.callTopicFor(type);
            KafkaConsumer<String, SpotifyCallMessage> consumer = new KafkaConsumer<>(baseProps);
            consumer.subscribe(List.of(topic));
            consumers.put(type, consumer);
            log.debug("Created Kafka consumer for topic {}", topic);
        }
    }

    private void scheduleNext() {
        if (!running) return;
        long delaySecs = configPropertyHolder.getInt(SpotifyPerformerProperty.SCHEDULE_DELAY_SECS);
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
        Map<SpotifyApiCallType, Integer> tokens = replenishTokens();

        for (SpotifyApiCallType type : SpotifyApiCallType.values()) {
            int budget = tokens.getOrDefault(type, 0);
            if (budget <= 0) continue;

            Consumer<String, SpotifyCallMessage> consumer = consumers.get(type);
            if (consumer == null) continue;

            ConsumerRecords<String, SpotifyCallMessage> records = consumer.poll(POLL_TIMEOUT);

            int consumed = 0;
            for (ConsumerRecord<String, SpotifyCallMessage> record : records) {
                if (consumed >= budget) break;
                try {
                    processMessage(record.value());
                    consumed++;
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                } catch (Exception e) {
                    log.error("Error processing Kafka message for api_call {}: {}",
                            record.value().apiCallId(), e.getMessage());
                }
            }

            if (records.isEmpty() && budget > 0) {
                redistributeTokens(type, budget, tokens);
            }

            consumer.commitSync();
        }

        // Periodic DB sweeps
        int retrySweepCycles = configPropertyHolder.getInt(SpotifyPerformerProperty.KAFKA_RETRY_SWEEP_CYCLES);
        int orphanSweepCycles = configPropertyHolder.getInt(SpotifyPerformerProperty.KAFKA_ORPHAN_SWEEP_CYCLES);

        if (cycle % retrySweepCycles == 0) {
            sweepRetries();
        }
        if (cycle % orphanSweepCycles == 0) {
            sweepOrphans();
        }
    }

    private void processMessage(SpotifyCallMessage msg) throws InterruptedException {
        Optional<SpotifyApiCall> callOpt = apiCallService.findById(msg.apiCallId());
        if (callOpt.isEmpty()) {
            log.debug("Skipping api_call {} — not found in DB", msg.apiCallId());
            return;
        }

        SpotifyApiCall call = callOpt.get();
        if (call.getStatus() != ApiCallStatus.PENDING) {
            log.debug("Skipping api_call {} — status is {} (not PENDING)", call.getId(), call.getStatus());
            return;
        }

        rateLimiter.acquire();
        executor.execute(call);
    }

    private Map<SpotifyApiCallType, Integer> replenishTokens() {
        int batchPerType = configPropertyHolder.getInt(SpotifyPerformerProperty.KAFKA_BATCH_PER_TYPE);
        int totalWeight = 0;

        Map<SpotifyApiCallType, Integer> weights = new EnumMap<>(SpotifyApiCallType.class);
        for (var entry : WEIGHT_PROPERTIES.entrySet()) {
            int weight = configPropertyHolder.getInt(entry.getValue());
            weights.put(entry.getKey(), weight);
            totalWeight += weight;
        }

        int totalBudget = batchPerType * SpotifyApiCallType.values().length;
        Map<SpotifyApiCallType, Integer> tokens = new EnumMap<>(SpotifyApiCallType.class);

        if (totalWeight == 0) return tokens;

        for (var entry : weights.entrySet()) {
            int budget = (int) Math.ceil((double) entry.getValue() / totalWeight * totalBudget);
            tokens.put(entry.getKey(), budget);
        }

        return tokens;
    }

    private void redistributeTokens(SpotifyApiCallType emptyType, int unusedTokens,
                                     Map<SpotifyApiCallType, Integer> tokens) {
        int remainingWeight = 0;
        for (var entry : WEIGHT_PROPERTIES.entrySet()) {
            if (entry.getKey() != emptyType) {
                remainingWeight += configPropertyHolder.getInt(entry.getValue());
            }
        }

        if (remainingWeight == 0) return;

        for (var entry : WEIGHT_PROPERTIES.entrySet()) {
            if (entry.getKey() == emptyType) continue;
            int weight = configPropertyHolder.getInt(entry.getValue());
            int extra = (int) ((double) weight / remainingWeight * unusedTokens);
            tokens.merge(entry.getKey(), extra, Integer::sum);
        }
    }

    private void sweepRetries() {
        List<SpotifyApiCall> retryCalls = apiCallService.findDueToRetry(SpotifyConstants.HIBERNATE_BATCH_SIZE);
        if (!retryCalls.isEmpty()) {
            log.info("Sweeping {} DUE_TO_RETRY calls from DB", retryCalls.size());
            for (SpotifyApiCall call : retryCalls) {
                try {
                    rateLimiter.acquire();
                    executor.execute(call);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }
    }

    private void sweepOrphans() {
        List<SpotifyApiCall> orphanCalls = apiCallService.findCreatedNotProduced(SpotifyConstants.HIBERNATE_BATCH_SIZE);
        if (!orphanCalls.isEmpty()) {
            log.info("Sweeping {} orphaned CREATED calls from DB", orphanCalls.size());
            for (SpotifyApiCall call : orphanCalls) {
                try {
                    rateLimiter.acquire();
                    executor.execute(call);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }
    }
}
