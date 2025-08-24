package yurykorzun.art.universe.music.data.raw.lastfm.common.config;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Gauge;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.beans.factory.annotation.Autowired;

import jakarta.annotation.PostConstruct;

import java.io.FileNotFoundException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.io.IOException;

import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class MetricsConfig {

    private final MeterRegistry registry;
    private final JdbcTemplate jdbcTemplate;
    private final ResourceLoader resourceLoader;

    // Cache for entity counts
    private final Map<String, AtomicInteger> entityCountsCache = new HashMap<>();
    
    // Cache for API call counts by type and status
    private final Map<String, AtomicInteger> apiCallCountsCache = new HashMap<>();

    @Autowired
    public MetricsConfig(MeterRegistry registry, JdbcTemplate jdbcTemplate, ResourceLoader resourceLoader) {
        this.registry = registry;
        this.jdbcTemplate = jdbcTemplate;
        this.resourceLoader = resourceLoader;
    }

    @PostConstruct
    public void init() {
        log.info("Initializing custom metrics");
        
        // Register entity count metrics
        registerEntityCountMetrics();
        
        // Register API call count metrics
        registerApiCallCountMetrics();
        
        // Initial update of metrics
        updateAllMetrics();
    }
    
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
    
    @Scheduled(fixedRateString = "${metrics.update.interval:60000}")
    public void updateAllMetrics() {
        log.debug("Updating all metrics");
        
        updateEntityCountMetrics();
        updateApiCallCountMetrics();
    }
    
    private void updateEntityCountMetrics() {
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
    
    private void updateApiCallCountMetrics() {
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

    private String loadSqlFromFile(String filename) throws IOException {
        Resource resource = resourceLoader.getResource("classpath:metrics/" + filename);
        if (resource.exists()) {
            return new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        }
        // Fallback logic if needed
        throw new FileNotFoundException("Could not find SQL file: " + filename);
    }
}
