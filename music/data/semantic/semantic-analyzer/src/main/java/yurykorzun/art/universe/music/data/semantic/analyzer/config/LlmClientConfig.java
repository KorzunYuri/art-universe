package yurykorzun.art.universe.music.data.semantic.analyzer.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import yurykorzun.art.universe.data.raw.common.integration.AdaptiveRateLimiter;
import yurykorzun.art.universe.music.data.semantic.analyzer.llm.LlmClient;
import yurykorzun.art.universe.music.data.semantic.analyzer.llm.openai.OpenAiLlmClient;
import yurykorzun.art.universe.music.data.semantic.analyzer.llm.resilience.LlmClientCircuitBreaker;
import yurykorzun.art.universe.music.data.semantic.analyzer.llm.resilience.LlmFailureClassifier;
import yurykorzun.art.universe.music.data.semantic.analyzer.llm.resilience.LlmFailureClassifier.CompiledBanPattern;
import yurykorzun.art.universe.music.data.semantic.model.AnalysisMode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@Configuration
public class LlmClientConfig {

    private static final Logger log = LoggerFactory.getLogger(LlmClientConfig.class);

    @Bean
    public LlmClientRegistry llmClientRegistry(
        LlmClientProperties clientProps,
        AnalysisModeProperties modeProps,
        ObjectMapper objectMapper
    ) {
        Map<String, LlmClient> namedClients = new HashMap<>();
        Map<String, LlmClientCircuitBreaker> circuitBreakers = new HashMap<>();
        Map<String, LlmFailureClassifier> classifiers = new HashMap<>();

        for (Map.Entry<String, LlmClientProperties.ClientConfig> entry : clientProps.getClients().entrySet()) {
            String name = entry.getKey();
            LlmClientProperties.ClientConfig cfg = entry.getValue();

            namedClients.put(name, createClient(objectMapper, cfg));
            circuitBreakers.put(name, createCircuitBreaker(name, cfg.getCircuitBreaker()));
            classifiers.put(name, createClassifier(name, cfg.getBanPatterns()));

            log.info("Created LLM client '{}': provider={}, model={}, temperature={}, ban-patterns={}",
                    name, cfg.getProvider(), cfg.getModel(), cfg.getTemperature(),
                    cfg.getBanPatterns() == null ? 0 : cfg.getBanPatterns().size());
        }

        Map<AnalysisMode, List<String>> modeClientOrder = new HashMap<>();
        for (Map.Entry<String, List<String>> entry : modeProps.getModes().entrySet()) {
            AnalysisMode mode = AnalysisMode.fromString(entry.getKey());
            List<String> clientNames = entry.getValue();
            for (String clientName : clientNames) {
                if (!namedClients.containsKey(clientName)) {
                    throw new IllegalStateException(
                            "Analysis mode '" + mode.getName() + "' references unknown client '"
                                    + clientName + "'. Available: " + namedClients.keySet());
                }
            }
            modeClientOrder.put(mode, clientNames);
            log.info("Mode '{}' client priority: {}", mode.getName(), clientNames);
        }

        return new LlmClientRegistry(namedClients, circuitBreakers, classifiers, modeClientOrder);
    }

    private LlmClient createClient(ObjectMapper objectMapper, LlmClientProperties.ClientConfig cfg) {
        AdaptiveRateLimiter rateLimiter = new AdaptiveRateLimiter(
            cfg.getRateLimit().getMinDelayMs(),
            cfg.getRateLimit().getMaxDelayMs(),
            cfg.getRateLimit().getBackoffMultiplier()
        );
        return switch (cfg.getProvider()) {
            case "openai" -> new OpenAiLlmClient(
                objectMapper,
                cfg.getApiKey(),
                cfg.getBaseUrl(),
                cfg.getModel(),
                cfg.getTemperature(),
                cfg.getConnectTimeout(),
                cfg.getReadTimeout(),
                rateLimiter
            );
            default -> throw new IllegalArgumentException("Unknown LLM provider: " + cfg.getProvider());
        };
    }

    private LlmClientCircuitBreaker createCircuitBreaker(
        String clientName,
        LlmClientProperties.CircuitBreakerConfig cfg
    ) {
        return new LlmClientCircuitBreaker(
            clientName,
            cfg.getRateLimitThreshold(),
            cfg.getInitialCooldown(),
            cfg.getMaxCooldown(),
            cfg.getCooldownMultiplier()
        );
    }

    private LlmFailureClassifier createClassifier(
        String clientName,
        List<LlmClientProperties.BanPatternConfig> patternConfigs
    ) {
        List<CompiledBanPattern> compiled = new ArrayList<>();
        if (patternConfigs != null) {
            for (LlmClientProperties.BanPatternConfig pc : patternConfigs) {
                Pattern bodyRegex = pc.getBodyPattern() == null
                    ? null
                    : Pattern.compile(pc.getBodyPattern(), Pattern.CASE_INSENSITIVE);
                compiled.add(new CompiledBanPattern(pc.getStatus(), bodyRegex));
            }
        }
        return new LlmFailureClassifier(clientName, compiled);
    }
}
