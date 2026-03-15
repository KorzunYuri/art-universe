package yurykorzun.art.universe.music.data.raw.lastfm.config;

import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import yurykorzun.art.universe.common.config.client.ConfigPropertyHolder;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Configuration
@Profile("!test")
@Slf4j
public class MetricsConfig {

    private final MeterRegistry registry;
    private final JdbcTemplate jdbcTemplate;
    private final ResourceLoader resourceLoader;
    private final ConfigPropertyHolder configPropertyHolder;
    private final ThreadPoolTaskScheduler taskScheduler;

    // Metrics cache
    private final Map<String, AtomicInteger> entityCountsCache = new HashMap<>();
    private final Map<String, AtomicInteger> apiCallCountsCache = new HashMap<>();
    private final Map<String, AtomicInteger> apiResponseCountsCache = new HashMap<>();
    private final Map<String, AtomicInteger> tableSizesCache = new HashMap<>();

    public MetricsConfig(
            MeterRegistry registry,
            JdbcTemplate jdbcTemplate,
            ResourceLoader resourceLoader,
            ConfigPropertyHolder configPropertyHolder,
            ThreadPoolTaskScheduler taskScheduler
    ) {
        this.registry = registry;
        this.jdbcTemplate = jdbcTemplate;
        this.resourceLoader = resourceLoader;
        this.configPropertyHolder = configPropertyHolder;
        this.taskScheduler = taskScheduler;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void initMetrics() {
        log.info("Initializing custom metrics");

        // Register metrics
        registerEntityCountMetrics();
        registerApiCallCountMetrics();
        registerApiResponseCountMetrics();
        registerTableSizeMetrics();

        // Initial update of metrics
        updateEntityCountMetrics();
        updateApiCallCountMetrics();
        updateApiResponseCountMetrics();
        updateTableSizeMetrics();

        // Start self-rescheduling loops
        scheduleEntityCountsUpdate();
        scheduleApiCallCountsUpdate();
        scheduleApiResponseCountsUpdate();
        scheduleTableSizesUpdate();
    }

    // ========== Scheduling ==========

    private void scheduleEntityCountsUpdate() {
        long intervalMs = configPropertyHolder.getInt(LastfmMaintenanceProperty.METRICS_UPDATE_ENTITY_COUNTS_INTERVAL_MS);
        taskScheduler.schedule(() -> {
            updateEntityCountMetrics();
            scheduleEntityCountsUpdate();
        }, Instant.now().plusMillis(intervalMs));
    }

    private void scheduleApiCallCountsUpdate() {
        long intervalMs = configPropertyHolder.getInt(LastfmMaintenanceProperty.METRICS_UPDATE_API_CALL_COUNTS_INTERVAL_MS);
        taskScheduler.schedule(() -> {
            updateApiCallCountMetrics();
            scheduleApiCallCountsUpdate();
        }, Instant.now().plusMillis(intervalMs));
    }

    private void scheduleApiResponseCountsUpdate() {
        long intervalMs = configPropertyHolder.getInt(LastfmMaintenanceProperty.METRICS_UPDATE_API_RESPONSE_COUNTS_INTERVAL_MS);
        taskScheduler.schedule(() -> {
            updateApiResponseCountMetrics();
            scheduleApiResponseCountsUpdate();
        }, Instant.now().plusMillis(intervalMs));
    }

    private void scheduleTableSizesUpdate() {
        long intervalMs = configPropertyHolder.getInt(LastfmMaintenanceProperty.METRICS_UPDATE_TABLE_SIZES_INTERVAL_MS);
        taskScheduler.schedule(() -> {
            updateTableSizeMetrics();
            scheduleTableSizesUpdate();
        }, Instant.now().plusMillis(intervalMs));
    }

    // ========== Registration ==========

    private void registerEntityCountMetrics() {
        List<String> entityTypes = List.of("artist", "album", "track", "tag");

        for (String entityType : entityTypes) {
            AtomicInteger entityCount = new AtomicInteger(0);
            entityCountsCache.put(entityType, entityCount);

            Gauge.builder("lastfm.entity.count", entityCount::get)
                .tag("entity_type", entityType)
                .description("Number of " + entityType + " entities in the database")
                .register(registry);
        }
    }

    private void registerApiCallCountMetrics() {
        try {
            // Get all possible API call types and statuses from dictionary
            String apiCallTypesSql = "SELECT name FROM mu_raw_lastfm.dictionary WHERE domain = 'ApiCallType'    ORDER BY name";
            String statusesSql =     "SELECT name FROM mu_raw_lastfm.dictionary WHERE domain = 'ApiCallStatus'  ORDER BY name";

            List<String> apiCallTypes = jdbcTemplate.queryForList(apiCallTypesSql, String.class);
            List<String> statuses = jdbcTemplate.queryForList(statusesSql, String.class);

            // Register metrics for all possible combinations
            for (String apiCallType : apiCallTypes) {
                for (String status : statuses) {
                    String key = apiCallType + ":" + status;

                    AtomicInteger count = new AtomicInteger(0);
                    apiCallCountsCache.put(key, count);

                    Gauge.builder("lastfm.api_call.count", count::get)
                        .tag("api_call_type", apiCallType)
                        .tag("status", status)
                        .description("Number of API calls of type " + apiCallType + " with status " + status)
                        .register(registry);
                }
            }
        } catch (Exception e) {
            log.error("Error registering API call count metrics", e);
        }
    }

    private void registerApiResponseCountMetrics() {
        try {
            // Get all possible API call types and response statuses from dictionary
            String apiCallTypesSql = "SELECT name FROM mu_raw_lastfm.dictionary WHERE domain = 'ApiCallType' ORDER BY name";
            String statusesSql = "SELECT name FROM mu_raw_lastfm.dictionary WHERE domain = 'ApiResponseStatus' ORDER BY name";

            List<String> apiCallTypes = jdbcTemplate.queryForList(apiCallTypesSql, String.class);
            List<String> statuses = jdbcTemplate.queryForList(statusesSql, String.class);

            // Register metrics for all possible combinations
            for (String apiCallType : apiCallTypes) {
                for (String status : statuses) {
                    String key = apiCallType + ":" + status;

                    AtomicInteger count = new AtomicInteger(0);
                    apiResponseCountsCache.put(key, count);

                    Gauge.builder("lastfm.api_response.count", count::get)
                        .tag("api_call_type", apiCallType)
                        .tag("status", status)
                        .description("Number of API responses of type " + apiCallType + " with status " + status)
                        .register(registry);
                }
            }
        } catch (Exception e) {
            log.error("Error registering API response count metrics", e);
        }
    }

    private void registerTableSizeMetrics() {
        try {
            // Register schema total size metric
            AtomicInteger schemaTotalSize = new AtomicInteger(0);
            tableSizesCache.put("schema_total", schemaTotalSize);

            Gauge.builder("lastfm.database.schema_size_bytes", schemaTotalSize::get)
                .description("Total size of mu_raw_lastfm schema in bytes")
                .register(registry);

            // Register category size metrics
            List<String> categories = List.of("core_entities", "relationships", "attributes", "api_processing", "maintenance", "other");

            for (String category : categories) {
                AtomicInteger categorySize = new AtomicInteger(0);
                tableSizesCache.put("category:" + category, categorySize);

                Gauge.builder("lastfm.database.category_size_bytes", categorySize::get)
                    .tag("category", category)
                    .description("Size of " + category + " tables in bytes")
                    .register(registry);
            }

            // Individual table metrics will be registered dynamically during updates

        } catch (Exception e) {
            log.error("Error registering table size metrics", e);
        }
    }

    // ========== Update methods ==========

    @Timed(value = "music.data.raw.lastfm.metrics.update", extraTags = {"category", "entities"})
    public void updateEntityCountMetrics() {
        log.debug("Updating entity count metrics");
        try {
            String sql = loadSqlFromFile("entity_counts.sql");
            List<Map<String, Object>> results = jdbcTemplate.queryForList(sql);

            for (Map<String, Object> row : results) {
                String entityType = (String) row.get("entity_type");
                Number count = (Number) row.get("count");

                AtomicInteger atomicCount = entityCountsCache.get(entityType);
                if (atomicCount != null) {
                    atomicCount.set(count.intValue());
                }
            }
        } catch (Exception e) {
            log.error("Error updating entity count metrics", e);
        }
    }

    @Timed(value = "music.data.raw.lastfm.metrics.update", extraTags = {"category", "api_calls"})
    public void updateApiCallCountMetrics() {
        log.debug("Updating API call count metrics");
        try {
            String sql = loadSqlFromFile("api_call_counts.sql");
            List<Map<String, Object>> results = jdbcTemplate.queryForList(sql);

            for (Map<String, Object> row : results) {
                String apiCallType = (String) row.get("api_call_type");
                String status = (String) row.get("status");
                Number count = (Number) row.get("count");
                String key = apiCallType + ":" + status;

                AtomicInteger atomicCount = apiCallCountsCache.get(key);
                if (atomicCount != null) {
                    atomicCount.set(count.intValue());
                }
            }
        } catch (Exception e) {
            log.error("Error updating API call count metrics", e);
        }
    }

    @Timed(value = "music.data.raw.lastfm.metrics.update", extraTags = {"category", "api_responses"})
    public void updateApiResponseCountMetrics() {
        log.debug("Updating API response count metrics");
        try {
            String sql = loadSqlFromFile("api_response_counts.sql");
            List<Map<String, Object>> results = jdbcTemplate.queryForList(sql);

            for (Map<String, Object> row : results) {
                String apiCallType = (String) row.get("api_call_type");
                String status = (String) row.get("status");
                Number count = (Number) row.get("count");
                String key = apiCallType + ":" + status;

                AtomicInteger atomicCount = apiResponseCountsCache.get(key);
                if (atomicCount != null) {
                    atomicCount.set(count.intValue());
                }
            }
        } catch (Exception e) {
            log.error("Error updating API response count metrics", e);
        }
    }

    @Timed(value = "music.data.raw.lastfm.metrics.update", extraTags = {"category", "table_sizes"})
    public void updateTableSizeMetrics() {
        log.debug("Updating table size metrics");
        try {
            String sql = loadSqlFromFile("table_sizes.sql");
            List<Map<String, Object>> results = jdbcTemplate.queryForList(sql);

            for (Map<String, Object> row : results) {
                String tableCategory = (String) row.get("table_category");
                String tableName = (String) row.get("table_name");
                Number sizeBytes = (Number) row.get("size_bytes");
                String metricType = (String) row.get("metric_type");

                if ("schema".equals(metricType)) {
                    // Schema total size
                    AtomicInteger atomicSize = tableSizesCache.get("schema_total");
                    if (atomicSize != null) {
                        atomicSize.set(sizeBytes.intValue());
                    }
                } else if ("category".equals(metricType)) {
                    // Category totals
                    String key = "category:" + tableCategory;
                    AtomicInteger atomicSize = tableSizesCache.get(key);
                    if (atomicSize != null) {
                        atomicSize.set(sizeBytes.intValue());
                    }
                } else if ("table".equals(metricType) && tableName != null) {
                    // Individual table sizes - register dynamically if not exists
                    String key = "table:" + tableCategory + ":" + tableName;
                    AtomicInteger atomicSize = tableSizesCache.get(key);
                    if (atomicSize == null) {
                        atomicSize = new AtomicInteger(0);
                        tableSizesCache.put(key, atomicSize);

                        // Register new gauge for this table
                        Gauge.builder("lastfm.database.table_size_bytes", atomicSize::get)
                            .tag("category", tableCategory)
                            .tag("table", tableName)
                            .description("Size of table " + tableName + " in bytes")
                            .register(registry);
                    }
                    atomicSize.set(sizeBytes.intValue());
                }
            }
        } catch (Exception e) {
            log.error("Error updating table size metrics", e);
        }
    }

    private String loadSqlFromFile(String filename) throws IOException {
        Resource resource = resourceLoader.getResource("classpath:metrics/" + filename);
        if (resource.exists()) {
            return new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        }
        // Fallback logic if needed
        throw new FileNotFoundException("Could not find SQL file: " + filename);
    }
}
