package yurykorzun.art.universe.music.data.semantic.analyzer.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;
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
    }

    @Data
    public static class RateLimitConfig {
        private long minDelayMs = 0L;
        private long maxDelayMs = 60_000L;
        private double backoffMultiplier = 2.0;
    }
}
