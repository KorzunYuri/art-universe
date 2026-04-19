package yurykorzun.art.universe.music.data.semantic.analyzer.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
@Component
@ConfigurationProperties(prefix = "semantic.llm")
public class LlmClientProperties {

    private Map<String, ClientConfig> clients;

    @Data
    public static class ClientConfig {
        private String provider = "openai";
        private String apiKey;
        private String baseUrl = "https://api.openai.com/v1";
        private String model = "gpt-4o";
        private Double temperature;
        private Duration connectTimeout = Duration.ofSeconds(10);
        private Duration readTimeout = Duration.ofMinutes(2);
        private RateLimitConfig rateLimit = new RateLimitConfig();
        private CircuitBreakerConfig circuitBreaker = new CircuitBreakerConfig();
        private List<BanPatternConfig> banPatterns = new ArrayList<>();
    }

    @Data
    public static class RateLimitConfig {
        private long minDelayMs = 0L;
        private long maxDelayMs = 60_000L;
        private double backoffMultiplier = 2.0;
    }

    @Data
    public static class CircuitBreakerConfig {
        /** Consecutive 429s before the client enters COOLING state. */
        private int rateLimitThreshold = 2;
        private Duration initialCooldown = Duration.ofMinutes(1);
        private Duration maxCooldown = Duration.ofHours(12);
        private double cooldownMultiplier = 5.0;
    }

    /**
     * Declarative ban detection rule for a client. A response is classified
     * as BANNED when BOTH (non-null) conditions match:
     * <ul>
     *   <li>{@code status} — required HTTP status (null = any status)</li>
     *   <li>{@code bodyPattern} — case-insensitive regex applied via {@link java.util.regex.Pattern#find}
     *                              to the error body (null = no body check)</li>
     * </ul>
     * At least one of the two must be set.
     */
    @Data
    public static class BanPatternConfig {
        private Integer status;
        private String bodyPattern;
    }
}
